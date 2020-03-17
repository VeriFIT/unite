#!/bin/bash
# deletes all Lyo generated files
# except the AdapterManager as that is the one with manual changes

cd "${BASH_SOURCE%/*}"

rm -r ../analysis/src/main/webapp/static
rm -r ../analysis/src/main/webapp/verifit
rm -r ../analysis/src/main/webapp/delegatedUI.js
rm -r ../analysis/src/main/webapp/index.jsp

rm -r ../compilation/src/main/webapp/static
rm -r ../compilation/src/main/webapp/verifit
rm -r ../compilation/src/main/webapp/delegatedUI.js
rm -r ../compilation/src/main/webapp/index.jsp
