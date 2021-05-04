Write-Output "Dummy tool!"
Start-Sleep -Seconds 5
Write-Output ("Your argument: " + $args[0])
Write-Output ("SUT: " + $args[1])
Set-Content -Path ./file1 -Value 'test file'
mkdir -Force dir1 > $null
Set-Content -Path ./dir1/file2 -Value 'test file'
Write-Output ("EnvVar1: " + $env:EnvVar1)
Write-Output ("EnvVar2: " + $env:EnvVar2)