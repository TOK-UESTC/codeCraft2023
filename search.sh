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

function search()
{
    name=$1
    if [$name -eq 2]
    then
        kpdl=6.2
        kidl=0.06
        kddl=1.5
    else
        kpdl=7.6
        kidl=0.04
        kddl=0.5
    fi
    echo "" > score$name.txt
    for kpd in $(seq 5.5 0.1 8.0)
    do
        for kid in $(seq 0.0 0.01 0.2)
        do
            for kdd in $(seq 0.7 0.1 1.3)
            do
                ../robot.exe "java -classpath ./bin com.huawei.codecraft.Main restart $kpd $kid $kdd $kpdl $kidl $kddl" -f -d -m  ../maps/$name.txt |sed  ':a;N;$!ba;s/\n/ /g'|sed s/[[:space:]]//g | sed "s/$/ $kpd, $kid, $kdd, $kpdl, $kidl, $kddl/"  >> score$name.txt &
                let count+=1
                while [ $(jobs -p | wc -l) -ge $max_processes ] ; do
                    sleep 1
                done
                shift
            done
        done
    done
}

count=0
max_processes=12

if [ $# -eq 1 ]
then
    name=$1
    search $name
else
    for name in $(seq 1 1 4)
    do
        search $name
    done
fi