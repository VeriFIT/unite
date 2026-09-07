/*
 * Copyright (C) 2026 Jan Fiedor <fiedorjan@centrum.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Text.RegularExpressions;

namespace UniteService;

/// <summary>
/// Starts and monitors the external PowerShell-based Unite script and exposes the
/// readiness signal used to determine when the script has successfully initialized.
/// </summary>
public class UniteRunner(
  ILogger<UniteRunner> logger,
  IConfiguration configuration) : IDisposable
{
  /// <summary>
  /// The process representing the running Unite script.
  /// </summary>
  private Process? _process;
  /// <summary>
  /// Task completion source used to signal when the Unite script is ready.
  /// </summary>
  private TaskCompletionSource? _ready;

  private static readonly EventId UniteScriptExecution =
    new(1002, "UniteScriptExecution");

  // Required configuration values for the Unite script execution
  private readonly string _powerShellExecutable = GetRequiredConfigurationValue(
    configuration, "UniteScript:PowerShellExecutable");
  private readonly string _scriptPath = GetRequiredConfigurationValue(
    configuration, "UniteScript:ScriptPath");
  private readonly string _workingDirectory = GetRequiredConfigurationValue(
    configuration, "UniteScript:WorkingDirectory");

  // The script prints this marker once it is ready to accept work
  private const string ReadySignal = "Ready to go!";

  // Constants used for interacting with the console input of the Unite script process
  private const int StdInputHandle = -10;
  private const short KeyEvent = 0x0001;
  private const ushort VirtualKeyC = 0x43;
  private const uint LeftCtrlPressed = 0x0008;

  // External functions for interacting with the console input of the Unite script process
  [DllImport("kernel32.dll", SetLastError = true)]
  private static extern bool AttachConsole(uint processId);
  [DllImport("kernel32.dll", SetLastError = true)]
  private static extern IntPtr GetStdHandle(int standardHandle);
  [DllImport("kernel32.dll", SetLastError = true)]
  private static extern bool WriteConsoleInputW(
    IntPtr consoleInput, InputRecord[] buffer, uint length,
    out uint eventsWritten);
  [DllImport("kernel32.dll", SetLastError = true)]
  private static extern bool FreeConsole();

  // Structures used for representing console input events for the Unite script process
  [StructLayout(LayoutKind.Explicit, CharSet = CharSet.Unicode)]
  private struct InputRecord
  {
    [FieldOffset(0)] public short EventType;
    [FieldOffset(4)] public KeyEventRecord KeyEvent;
  }

  [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
  private struct KeyEventRecord
  {
    [MarshalAs(UnmanagedType.Bool)] public bool KeyDown;
    public ushort RepeatCount;
    public ushort VirtualKeyCode;
    public ushort VirtualScanCode;
    public char UnicodeChar;
    public uint ControlKeyState;
  }

  /// <summary>
  /// Starts the external Unite script, waits until it reports readiness,
  /// and monitors its exit status until teardown or cancellation.
  /// </summary>
  /// <param name="stoppingToken">Cancellation token used to stop the script.</param>
  public async Task ExecuteAsync(CancellationToken stoppingToken)
  {
    try
    { // Task representing the readiness of the Unite script
      var ready = new TaskCompletionSource(
        TaskCreationOptions.RunContinuationsAsynchronously);

      // Make it accessible for callback methods that need to signal readiness
      _ready = ready;

      var startInfo = new ProcessStartInfo
      {
        FileName = _powerShellExecutable,
        Arguments = $"-NoLogo -NoProfile -NonInteractive " +
          $"-ExecutionPolicy Bypass -File \"{_scriptPath}\" ",
        WorkingDirectory = _workingDirectory,
        UseShellExecute = false,
        CreateNoWindow = false,
        RedirectStandardOutput = true,
        RedirectStandardError = true
      };

      _process = new Process
      {
        StartInfo = startInfo,
        EnableRaisingEvents = true
      };
      _process.OutputDataReceived += HandleOutputDataReceived;
      _process.ErrorDataReceived += HandleErrorDataReceived;

      if (!_process.Start())
      {
        throw new InvalidOperationException("Unite script could not be started.");
      }

      _process.BeginOutputReadLine();
      _process.BeginErrorReadLine();

      logger.LogInformation(
        $"Unite script started with process ID {_process.Id}.");

      using CancellationTokenRegistration registration =
        stoppingToken.Register(RequestScriptStop);

      // Wait for either the script to report readiness or the process to exit
      var processExited = _process.WaitForExitAsync(CancellationToken.None);
      var completedStartupTask = await Task.WhenAny(ready.Task, processExited);

      if (completedStartupTask == processExited &&
        !ready.Task.IsCompletedSuccessfully)
      {
        throw new InvalidOperationException(
          $"Unite script exited before reporting readiness with code {_process.ExitCode}.");
      }

      // Await the readiness signal from the script and the process exit task
      await ready.Task.WaitAsync(stoppingToken);
      await processExited;

      if (!stoppingToken.IsCancellationRequested && _process.ExitCode != 0)
      {
        throw new InvalidOperationException(
          $"Unite script exited with code {_process.ExitCode}.");
      }

      logger.LogInformation(
        $"Unite script exited with code {_process.ExitCode}.");
    }
    finally
    {
      await StopChildProcessAsync();

      _ready = null;
    }
  }

  /// <summary>
  /// Processes output emitted by the Unite script process and completes the readiness
  /// signal when the script reports it has finished initializing.
  /// </summary>
  private void HandleOutputDataReceived(
    object sender, DataReceivedEventArgs eventArgs)
  {
    if (string.IsNullOrWhiteSpace(eventArgs.Data) || _ready is null)
    {
      return;
    }

    if (eventArgs.Data.Trim() == ReadySignal)
    {
      _ready.TrySetResult();

      logger.LogInformation("Unite started successfully.");
    }

    // Strip ANSI escape sequences from warning output to keep the log readable
    if (_ready.Task.IsCompleted && eventArgs.Data.Contains("WARNING:"))
    {
      logger.LogWarning(
        UniteScriptExecution, "{Message}",
        Regex.Replace(eventArgs.Data, @"\x1B\[[0-?]*[ -/]*[@-~]", "")[9..]);
    }
  }

  /// <summary>
  /// Captures stderr output from the script so any PowerShell failures remain in
  /// the service logs.
  /// </summary>
  private void HandleErrorDataReceived(
    object sender, DataReceivedEventArgs eventArgs)
  {
    if (!string.IsNullOrWhiteSpace(eventArgs.Data))
    {
      logger.LogError("Unite script error: {Output}", eventArgs.Data);
    }
  }

  /// <summary>
  /// Stops the child Unite script process gracefully and falls back to termination if
  /// it does not exit within the configured timeout.
  /// </summary>
  private async Task StopChildProcessAsync()
  {
    if (_process is null || _process.HasExited)
    {
      return;
    }

    using var timeout = new CancellationTokenSource(TimeSpan.FromSeconds(30));

    try
    { // Attempt to wait for the Unite script process to exit gracefully
      await _process.WaitForExitAsync(timeout.Token);

      logger.LogInformation("Unite script stopped");
    }
    catch (OperationCanceledException)
    { // Fallback to termination if the Unite script does not exit gracefully
      logger.LogWarning("PowerShell did not stop gracefully; terminating it.");

      _process.Kill(entireProcessTree: true);

      await _process.WaitForExitAsync();
    }
  }

  /// <summary>
  /// Sends a CTRL+C to the script's console, which is the Windows-safe way to stop
  /// a foreground PowerShell process without killing the whole service host.
  /// </summary>
  private void RequestScriptStop()
  {
    if (!OperatingSystem.IsWindows() || _process is null || _process.HasExited)
    {
      return;
    }

    logger.LogInformation("Requested script stop.");

    if (!AttachConsole((uint)_process.Id))
    {
      logger.LogWarning(
        "Could not attach to PowerShell console: {ErrorCode}.",
        Marshal.GetLastWin32Error());

      return;
    }

    try
    { // Attempt to send CTRL+C to the Unite script's console
      var consoleInput = GetStdHandle(StdInputHandle);
      var inputRecords = new[]
      {
        CreateCtrlCInputRecord(keyDown: true),
        CreateCtrlCInputRecord(keyDown: false)
      };
      uint eventsWritten = 0;

      if (consoleInput == IntPtr.Zero || consoleInput == new IntPtr(-1) ||
        !WriteConsoleInputW(
          consoleInput, inputRecords, (uint)inputRecords.Length,
          out eventsWritten) || eventsWritten != inputRecords.Length)
      {
        logger.LogWarning(
          "Could not inject Ctrl+C into the PowerShell console input buffer: " +
          "{EventsWritten}/{EventCount} events written, error {ErrorCode}.",
          eventsWritten, inputRecords.Length, Marshal.GetLastWin32Error());
      }
    }
    finally
    {
      FreeConsole();
    }
  }

  /// <summary>
  /// Builds the CTRL+C input record injected into the PowerShell process console.
  /// </summary>
  private static InputRecord CreateCtrlCInputRecord(bool keyDown) => new()
  {
    EventType = KeyEvent,
    KeyEvent = new KeyEventRecord
    {
      KeyDown = keyDown,
      RepeatCount = 1,
      VirtualKeyCode = VirtualKeyC,
      UnicodeChar = '\u0003',
      ControlKeyState = LeftCtrlPressed
    }
  };

  /// <summary>
  /// Reads a required configuration setting and expands environment variables inside it.
  /// </summary>
  private static string GetRequiredConfigurationValue(
    IConfiguration configuration, string key)
  {
    var value = configuration[key];

    if (string.IsNullOrWhiteSpace(value))
    {
      throw new InvalidOperationException(
        $"Configuration value '{key}' is required.");
    }

    return Environment.ExpandEnvironmentVariables(value);
  }

  /// <summary>
  /// Releases the underlying process handle when the runner is disposed.
  /// </summary>
  public void Dispose()
  {
    _process?.Dispose();
  }
}

/* End of UniteRunner.cs */
