@echo off

echo Dummy tool!
ping -n %1 127.0.0.1 > nul
echo Your argument: %2
echo SUT: %3
echo test file > file1
mkdir dir1 > nul 2> nul
echo test file > dir1/file2
echo EnvVar1: %EnvVar1%
echo EnvVar2: %EnvVar2%
