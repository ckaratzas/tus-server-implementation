#!/bin/bash

set -e
source $(dirname "$(readlink -f "$0")")/run_helper.sh

java_pid=

stop () {
    [ "$java_pid" ] && kill $java_pid
    exit
}

bounce () {
    [ "$java_pid" ] && kill $java_pid
    trap bounce HUP
}

trap stop   INT TERM
trap bounce HUP

while : ; do

    run_me "$1" &
    java_pid=$!

    wait $java_pid

done