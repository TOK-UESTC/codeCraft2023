#!/bin/bash

# e.g.: ./test.sh 2
name=1
if [ $# -eq 1 ]
then
    name=$1
fi

../robot.exe "java -agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=y -classpath ./bin com.huawei.codecraft.Main" -d -m ../maps/$name.txt