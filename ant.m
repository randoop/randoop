#!/usr/bin/env bash

BASE="$(dirname "$0")/.."

BUILD_HOME="$BASE/../../"

java \
    -XX:ReservedCodeCacheSize=256M \
    -Xbootclasspath/a:"$BASE/lib/major-rt.jar:$BASE/../replacecall-4.3.3.jar" \
    -javaagent:"$BASE/../replacecall-4.3.3.jar" \
    -javaagent:"$BASE/../jacocoagent.jar=destfile=$BASE/../../results/jacoco.exec" \
    -jar "$BASE/lib/ant/ant-launcher.jar" $*

