#!/bin/bash

# e.g.: ./test.sh 2
name=1
if [ $# -eq 1 ]
then
    name=$1
fi

# 与答题器交互
# interact with discriminator

$current = pwd;
cd ..
./robot_gui.exe "java -Xverify:none -agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=y -classpath ./codeCraft2023/bin com.huawei.codecraft.Main restart" -f -d -m ./maps/$name.txt
cd current