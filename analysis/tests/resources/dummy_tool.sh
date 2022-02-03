#/bin/sh

echo "Dummy tool!"
sleep $1 1> /dev/null 2> /dev/null
echo "Your argument: $2"
echo "SUT: $3"
echo "test file" > file1
mkdir -p dir1
echo "test file" > dir1/file2
echo "EnvVar1: $EnvVar1"
echo "EnvVar2: $EnvVar2"