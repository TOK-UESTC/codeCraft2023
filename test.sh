#!/bin/bash

# e.g.: ./test.sh 2

# 编译项目, 将用到的项目文件重新编译
# compile project
cd ./src/
javac -encoding UTF-8 com/huawei/codecraft/Main.java -g:lines,vars,source

# 打一个jar包, 方便调试
# make a jar package for debugging
jar cvf ../main.jar com/huawei/codecraft/Main.class

# 整理项目, 将二进制文件移动到bin目录下
# move .class file to bin/
find ./ -name "*.class" -exec cp --parents {} ../bin/ \;
find ./ -name "*.class" -exec rm {} \;
cd ..


if [ $# -eq 1 ]
then
    name=$1
    # 与答题器交互
    # interact with discriminator
    # ../robot.exe "java -Xmn512m -Xms1024m -Xmx1024m -XX:MaxGCPauseMillis=1 -Xloggc:./log/gc.log -classpath ./bin com.huawei.codecraft.Main restart" -f -m ../maps/$name.txt
    ../robot.exe "java -jar -Xmn512m -Xms1024m -Xmx1024m -XX:MaxGCPauseMillis=1 -classpath ./bin com.huawei.codecraft.Main  restart" -f -m ../maps/$name.txt
else
    # 与答题器交互
    # interact with discriminator
    # 清空error.txt文件的内容
    # clear error.txt
    echo "" > error.txt
    ../robot.exe "java -Xmn512m -Xms1024m -Xmx1024m -XX:MaxGCPauseMillis=1 -classpath ./bin com.huawei.codecraft.Main restart" -f -m  ../maps/1.txt >> error.txt
    ../robot.exe "java -Xmn512m -Xms1024m -Xmx1024m -XX:MaxGCPauseMillis=1 -classpath ./bin com.huawei.codecraft.Main restart" -f -m  ../maps/2.txt >> error.txt
    ../robot.exe "java -Xmn512m -Xms1024m -Xmx1024m -XX:MaxGCPauseMillis=1 -classpath ./bin com.huawei.codecraft.Main restart" -f -m  ../maps/3.txt >> error.txt
    ../robot.exe "java -Xmn512m -Xms1024m -Xmx1024m -XX:MaxGCPauseMillis=1 -classpath ./bin com.huawei.codecraft.Main restart" -f -m  ../maps/4.txt >> error.txt
    # 读取error.txt文件的内容并输出其中的score行
    # read error.txt and output score line
    awk '{if(match($0, /"score":[0-9]+/)){sum+=substr($0,RSTART+8,RLENGTH-8)}} END {print sum}' error.txt

fi

