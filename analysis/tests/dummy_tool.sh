#/bin/sh

if [ "$#" -ne 2 ]; then
  echo "Needs two arguments"
  exit 1
fi

echo "Dummy tool!"
sleep 5
echo "Your argument: $1"
echo "SUT: $2"
echo "test file" > file1
mkdir dir1
echo "test file" > dir1/file2