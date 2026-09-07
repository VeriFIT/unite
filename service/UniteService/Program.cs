/*
 * Copyright (C) 2026 Jan Fiedor <fiedorjan@centrum.cz>
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

using Microsoft.Extensions.Logging.EventLog;

using UniteService;

const string UniteHomeEnvironmentVariable = "UNITE_HOME";
const string UniteServiceConfigEnvironmentVariable = "UNITE_SERVICE_CONFIG";

var builder = Host.CreateApplicationBuilder(args);
var uniteHome = Environment.GetEnvironmentVariable(UniteHomeEnvironmentVariable);
var uniteServiceConfig = Environment.GetEnvironmentVariable(
  UniteServiceConfigEnvironmentVariable);

// Make sure the UNITE_HOME environment variable is set before proceeding
if (string.IsNullOrWhiteSpace(uniteHome))
{
  throw new InvalidOperationException(
    $"Environment variable '{UniteHomeEnvironmentVariable}' is required.");
}

// Default configuration must always be available
builder.Configuration.AddYamlFile(
  Path.Combine(uniteHome, "service", "conf", "default.config.yaml"),
  optional: false,
  reloadOnChange: true);
// Optional user-specific configuration file (overrides default settings)
builder.Configuration.AddYamlFile(
  Path.Combine(uniteHome, "conf", "service", "service.config.yaml"),
  optional: true,
  reloadOnChange: true);

// Optional user-specified configuration file via the UNITE_SERVICE_CONFIG
// environment variable
if (!string.IsNullOrWhiteSpace(uniteServiceConfig))
{
  builder.Configuration.AddYamlFile(
    Environment.ExpandEnvironmentVariables(uniteServiceConfig),
    optional: true,
    reloadOnChange: true);
}

// Register the application as a Windows service and, on Windows, configure the
// event log source used by the host for service-level logging
builder.Services.AddWindowsService(options =>
{
  options.ServiceName = "Unite Service";
});
if (OperatingSystem.IsWindows())
{
  builder.Services.Configure<EventLogSettings>(options =>
  {
    if (OperatingSystem.IsWindows())
    {
      options.SourceName = "Unite Service";
    }
  });
}

// The runner owns the external PowerShell process, while the worker owns the
// process lifetime loop and restart policy for the hosted service lifecycle
builder.Services.AddSingleton<UniteRunner>();
builder.Services.AddHostedService<Worker>();

var host = builder.Build();
host.Run();

/* End of Program.cs */
