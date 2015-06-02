#!/bin/bash

# This script makes three versions of predator in three different directories

# Prerequisities:
# Ubuntu 12.04 LTS (amd64) expected
# sudo apt-get install cmake g++ gcc-4.6-multilib gcc-4.6-plugin-dev libboost-dev make

# move if directories exist
if [ -d predator-backup ]
then
  rm -rf predator predator-bfs predator-dfs
  mv predator-backup predator
fi

# copy
cp -a predator predator-bfs
cp -a predator predator-dfs
cp -a predator predator-backup

(
DIR=predator
cd $DIR
git apply ../${DIR}.patch
)

(
DIR=predator-bfs
cd $DIR
cp ../xmltrace.hh sl
git apply ../${DIR}.patch
)

(
DIR=predator-dfs
cd $DIR
cp ../xmltrace.hh sl
git apply ../${DIR}.patch
)

#make predator
cd_make () {
  ( cd $1 ; ./switch-host-gcc.sh /usr/bin/gcc-4.6 ) &
}

cd_make predator

cd_make predator-bfs

cd_make predator-dfs

wait

echo "Installation completed."

