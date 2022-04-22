Write-Output "Dummy tool!"
try {
    Start-Sleep -Seconds $args[0] > $null
} catch { }
Write-Output ("Your argument: " + $args[1])
Write-Output ("SUT: " + $args[2])
Set-Content -Path ./file1 -Value 'test file'
mkdir -Force dir1 > $null
Set-Content -Path ./dir1/file2 -Value 'test file'
Write-Output ("EnvVar1: " + $env:EnvVar1)
Write-Output ("EnvVar2: " + $env:EnvVar2)