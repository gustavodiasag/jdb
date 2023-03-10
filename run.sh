#!/bin/sh

if [ $# != 1 ]; then
	echo "Usage: sh $0 <binary-filename>"
	exit 1
fi

# Directory where the jar file is located
dir=./bin

# jar file name
jar=project.jar

java -jar $dir/$jar $1

rm tmp*