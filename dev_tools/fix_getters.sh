#!/bin/bash
# fixes a Lyo bug (probably a bug) - getters return a HashSet instead of a Set

cd "${BASH_SOURCE%/*}"

cd ../analysis/src/main/java/org/eclipse/lyo/oslc/domains
find . -type f -exec sed -i "s/public HashSet/public Set/g" {} \;
cd - 1> /dev/null
cd ../analysis/src/main/java/verifit/analysis/resources
find . -type f -exec sed -i "s/public HashSet/public Set/g" {} \;
cd - 1> /dev/null
cd ../compilation/src/main/java/org/eclipse/lyo/oslc/domains
find . -type f -exec sed -i "s/public HashSet/public Set/g" {} \;
cd - 1> /dev/null
cd ../compilation/src/main/java/verifit/compilation/resources
find . -type f -exec sed -i "s/public HashSet/public Set/g" {} \;
