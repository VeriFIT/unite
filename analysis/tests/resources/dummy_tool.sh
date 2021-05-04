#/bin/sh

echo "Dummy tool!"
sleep 5
echo "Your argument: $1"
echo "SUT: $2"
echo "test file" > file1
mkdir -p dir1
echo "test file" > dir1/file2
echo "EnvVar1: $EnvVar1"
echo "EnvVar2: $EnvVar2"