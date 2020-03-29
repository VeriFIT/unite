#!/bin/bash
# fixes a Lyo bug (probably a bug) - getters return a HashSet instead of a Set

cd "${BASH_SOURCE%/*}"

./move_src.sh
./fix_getters.sh
