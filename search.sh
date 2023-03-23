#!/bin/bash

# 编译项目, 将用到的项目文件重新编译
# compile project
cd ./src/
javac -encoding UTF-8 com/huawei/codecraft/Main.java -g:lines,vars,source

# 整理项目, 将二进制文件移动到bin目录下
# move .class file to bin/
find ./ -name "*.class" -exec cp --parents {} ../bin/ \;
find ./ -name "*.class" -exec rm {} \;
cd ..


count=0
max_processes=24

if [ $# -eq 1 ]
then
    name=$1
    echo "" > score$name.txt
    for kpd in $(seq 4.2 0.1 4.3)
    do
        for kid in $(seq 0.1 0.1 0.2)
        do
            for kdd in $(seq 1 0.1 1.1)
            do
                ../robot.exe "java -classpath ./bin com.huawei.codecraft.Main restart $kpd $kid $kdd" -f -d -m  ../maps/$name.txt | sed "s/$/ $kpd $kid $kdd/"  >> score$name.txt &
                let count+=1
                while [ $(jobs -p | wc -l) -ge $max_processes ] ; do
                    sleep 1
                done
                shift
            done
        done
    done
fi