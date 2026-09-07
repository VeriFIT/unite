#
# Copyright (C) 2026 Jan Fiedor <fiedorjan@centrum.cz>
#
# This program and the accompanying materials are made available under
# the terms of the Eclipse Public License 2.0 which is available at
# https://www.eclipse.org/legal/epl-2.0
#
# SPDX-License-Identifier: EPL-2.0
#

<#
  .Synopsis
  This script manages the Unite service, allowing it to be built, created, and deleted.

  .Description
  This script allows you to build, create, and delete the Unite service on a Windows system.

  .Parameter [string]$Action
    The action to perform on the Unite service. Valid values are 'build', 'create', and 'delete'.
#>
param(
  [Parameter(Mandatory = $true, Position = 0)]
  [ValidateSet('build', 'create', 'delete')]
  [string]$Action
)

$ErrorActionPreference = 'Stop'
$serviceName = 'UniteService'

<#
  .Synopsis
  Stops the registered service if it is running.

  .Description
  This function checks if the Unite service is registered and running. If it is running, it stops the service. It returns $true if the service was stopped, and $false otherwise.

  .Outputs
    $true if the service was stopped, $false otherwise.
#>
function Stop-RegisteredService {
  $service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue

  if ($null -eq $service) {
    return $false
  }

  if ($service.Status -ne [ServiceProcess.ServiceControllerStatus]::Stopped) {
    Stop-Service -Name $serviceName -Force

    $service.WaitForStatus([ServiceProcess.ServiceControllerStatus]::Stopped, [TimeSpan]::FromSeconds(30))

    return $true
  }

  return $false
}

<#
  .Synopsis
  Builds and publishes the Unite service using the dotnet CLI.

  .Description
  This function runs the 'dotnet publish' command to build and publish the Unite service to the 'bin' directory under the script root. It throws an exception if the publish process fails.

  .Outputs
    None. Throws an exception if the publish process fails.
#>
function Publish-Service {
  dotnet publish (Join-Path $PSScriptRoot 'UniteService') `
    --configuration Release `
    --runtime win-x64 `
    --self-contained true `
    -p:PublishSingleFile=true `
    --output (Join-Path $PSScriptRoot 'bin')

  if ($LASTEXITCODE -ne 0) {
    throw "Failed to build and publish service (dotnet publish exit code $LASTEXITCODE)."
  }
}

if ($Action -eq 'build') {
  Publish-Service

  exit 0
}

$currentIdentity = [Security.Principal.WindowsIdentity]::GetCurrent()
$principal = [Security.Principal.WindowsPrincipal]::new($currentIdentity)
$isAdministrator = $principal.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

# Service creation and deletion requires administrative privileges, elevate the
# script if it is not running with administrative privileges already
if (-not $isAdministrator) {
  $powershell = (Get-Process -Id $PID).Path
  $elevatedProcess = Start-Process -FilePath $powershell `
    -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', "`"$PSCommandPath`"", '-Action', $Action) `
    -Verb RunAs `
    -Wait `
    -PassThru
  exit $elevatedProcess.ExitCode
}

if ($Action -eq 'create') {
  $displayName = 'Unite Service'
  $serviceExecutable = Join-Path $PSScriptRoot 'bin\UniteService.exe'
  $binPath = '"{0}"' -f $serviceExecutable

  if (-not (Test-Path -LiteralPath $serviceExecutable -PathType Leaf)) {
    throw "Published service executable was not found: $serviceExecutable. Run .\service.ps1 build first."
  }

  sc.exe create $serviceName 'binPath=' $binPath 'start=' 'auto' 'DisplayName=' $displayName

  if ($LASTEXITCODE -ne 0) {
    throw "Failed to register service '$serviceName' (sc.exe exit code $LASTEXITCODE)."
  }

  Write-Output "Service '$serviceName' was registered."

  exit 0
}

$service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue

if ($null -eq $service) {
  Write-Output "Service '$serviceName' is not registered."

  exit 0
}

Stop-RegisteredService | Out-Null

sc.exe delete $serviceName

if ($LASTEXITCODE -ne 0) {
  throw "Failed to unregister service '$serviceName' (sc.exe exit code $LASTEXITCODE)."
}

Write-Output "Service '$serviceName' was unregistered."

# End of service.ps1
