#!/bin/bash

# e.g.: ./test.sh 2
name=1
if [ $# -eq 1 ]
then
    name=$1
fi

../robot.exe "java -classpath ./bin --enable-preview com.huawei.codecraft.Main" -f -m ../maps/$name.txt