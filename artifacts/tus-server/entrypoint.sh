#!/bin/sh
set -e
set -o xtrace
source $(dirname "$(readlink -f "$0")")/run_helper.sh
printenv
run_me $1