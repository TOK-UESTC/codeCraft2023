#!/bin/bash

# e.g.: ./test.sh 2
name=1
if [ $# -eq 1 ]
then
    name=$1
fi

# 编译项目, 将用到的项目文件重新编译
# compile project
cd ./src/
javac -encoding UTF-8 com/huawei/codecraft/Main.java

# 整理项目, 将二进制文件移动到bin目录下
# move .class file to bin/
find ./ -name "*.class" -exec cp --parents {} ../bin/ \;
find ./ -name "*.class" -exec rm {} \;
cd ..

# 与答题器交互
# interact with discriminator
../robot.exe "java -agentlib:jdwp=transport=dt_socket,address=5005,server=y,suspend=y -classpath ./bin com.huawei.codecraft.Main" -f -d -m ../maps/$name.txt
