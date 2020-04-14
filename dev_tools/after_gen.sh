#!/bin/bash
# run this after generating a new model from Lyo to make the code compilable (fix generation bugs or invalid config etc..)

cd "${BASH_SOURCE%/*}"

./fix_move_domains.sh
./fix_getters.sh
./fix_redo_modifs.sh