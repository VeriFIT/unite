/*
 * Copyright (C) 2026 Jan Fiedor <fiedorjan@centrum.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

namespace UniteService;

/// <summary>
/// Hosts the long-running Unite execution loop and restarts it if the underlying
/// PowerShell script exits unexpectedly.
/// </summary>
public class Worker(
  ILogger<Worker> logger,
  UniteRunner uniteRunner) : BackgroundService
{
  /// <summary>
  /// Runs the Unite worker loop until the host is stopping.
  /// </summary>
  /// <param name="stoppingToken">
  /// Cancellation request triggered by the host when the service is shutting down.
  /// </param>
  protected override async Task ExecuteAsync(CancellationToken stoppingToken)
  {
    // The Unite Runner takes time to start, so yield to allow the host to
    // complete startup and flag the service as running
    await Task.Yield();

    while (!stoppingToken.IsCancellationRequested)
    {
      logger.LogInformation("Starting Unite");

      // Execute the external Unite script. If it exits early, the loop below will
      // restart it unless the service is already stopping.
      await uniteRunner.ExecuteAsync(stoppingToken);

      if (!stoppingToken.IsCancellationRequested)
      {
        logger.LogWarning("Unite crashed, attepting to restart it.");

        await Task.Delay(1000, stoppingToken);
      }
    }
  }
}

/* End of Worker.cs */
