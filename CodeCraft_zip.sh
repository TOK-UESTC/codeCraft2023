#!/bin/bash

SCRIPT=$(readlink -f "$0")
BASEDIR=$(dirname "$SCRIPT")
cd $BASEDIR

if [ ! -d src ]
then
    echo "ERROR: $BASEDIR is not a valid CMake file of SDK_JAVA for CodeCraft-2023."
    echo "  Please run this script in a regular directory of SDK_JAVA."
    exit -1
fi

rm -f CodeCraft-2023.zip
zip -9 -r CodeCraft-2023.zip src