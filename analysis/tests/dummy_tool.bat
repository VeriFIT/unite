@echo off

echo Dummy tool!
ping -n 5 127.0.0.1>nul
echo Your argument: %1
echo SUT: %2
echo test file > file1
mkdir dir1
echo test file > dir1/file2