#!/bin/bash

work_dir=tmp
projects=("Chart" "Closure" "Lang" "Math" "Time")

# Set up defects4j repo
cd ..

if [ ! -d "defects4j" ]; then

	git clone https://github.com/rjust/defects4j
	cd defects4j
	./init.sh
	export PATH=$PATH:./framework/bin
else
	cd defects4j
fi

if [ ! -d "tmp" ]; then
	mkdir $work_dir
fi

for project in $projects
do
	defects4j checkout -p $project -v 1b -w $work_dir
	defects4j compile -w $work_dir
	defects4j coverage -w $work_dir
done

#java -ea -classpath 