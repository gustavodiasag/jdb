#!/bin/sh

if [ $# != 0 ]; then
    echo "Usage: sh $0"
    exit 1
fi

# Directory where the jar file is located
dir=./bin

# jar file name
jar=project.jar

java -jar $dir/$jar