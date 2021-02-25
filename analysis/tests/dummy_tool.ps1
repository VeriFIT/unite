Write-Output "Dummy tool!"
Start-Sleep -Seconds 5
Write-Output ("Your argument: " + $args[0])
Write-Output ("SUT: " + $args[1])
Write-Output "test file" > file1
mkdir -Force dir1 > $null
echo "test file" > dir1/file2