#!/bin/bash -x

colls='/
/dfc1
/dfc1/home
/dfc1/home/public
/dfc1/home/rods
/dfc1/home/rods#dfc2
/dfc1/trash
/dfc1/trash/home
/dfc1/trash/home/public
/dfc1/trash/home/rods
/dfc1/trash/home/rods#dfc2
/dfc2'

for c in $colls
do
    imeta set -c "$c" ipc_UUID $(uuidgen -t)
done

