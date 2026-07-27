#!/bin/bash

# Copy jarfiles from Randoop to the current directory.
# Copies from the Randoop in which this script resides.
# The optional first argument is a suffix for each jarfile in the current
# directory (e.g., use "-current" if the randoop jarfiles are named
# "randoop-current.jar", "replacecall-current.jar", etc.).

# shellcheck disable=SC2012

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &> /dev/null && pwd)"
RANDOOP_DIR=$(dirname "${SCRIPT_DIR}")
SUFFIX=$1

# Move old versions of files to "*-ORIG", or delete if those files already exist.
if [ -f "randoop${SUFFIX}.jar-ORIG" ]; then
  rm -f "randoop${SUFFIX}.jar" "replacecall${SUFFIX}.jar" "covered-class${SUFFIX}.jar"
else
  mv -f "randoop${SUFFIX}.jar" "randoop${SUFFIX}.jar-ORIG" 2> /dev/null
  mv -f "replacecall${SUFFIX}.jar" "replacecall${SUFFIX}.jar-ORIG" 2> /dev/null
  mv -f "covered-class${SUFFIX}.jar" "covered-class${SUFFIX}.jar-ORIG" 2> /dev/null
fi

# Get the most recently built version of each file (a directory might hold
# multiple versions).  Sort by modification time rather than by name, because
# name order is not version order: "randoop-all-4.3.9.jar" sorts after
# "randoop-all-4.3.10.jar".
# Directory build/libs accumulates the jar files of every version that has been
# built.  Directory build/distlibs is written by Gradle's `copyJars` task, which
# is a Sync task, so build/distlibs holds only the current version's jar files.
RANDOOP_ALL_JAR="$(ls -t "${RANDOOP_DIR}"/build/libs/randoop-all*.jar | head -n1)"
REPLACECALL_JAR="$(ls -t "${RANDOOP_DIR}"/build/distlibs/replacecall*.jar | head -n1)"
COVERED_CLASS_JAR="$(ls -t "${RANDOOP_DIR}"/build/distlibs/covered-class*.jar | head -n1)"

# Install new versions
ln -sf "$RANDOOP_ALL_JAR" .
ln -sf "$REPLACECALL_JAR" .
ln -sf "$COVERED_CLASS_JAR" .
ln -sf "$(basename -- "$RANDOOP_ALL_JAR")" "randoop${SUFFIX}.jar"
ln -sf "$(basename -- "$REPLACECALL_JAR")" "replacecall${SUFFIX}.jar"
ln -sf "$(basename -- "$COVERED_CLASS_JAR")" "covered-class${SUFFIX}.jar"
