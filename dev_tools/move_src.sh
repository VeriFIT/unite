#!/bin/bash
# a workaround for the oslc package being generated only once and in the core directory

cd "${BASH_SOURCE%/*}"

cp -r ../src ../compilation
cp -r ../src ../analysis
rm -r ../src
