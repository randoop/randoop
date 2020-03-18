#!/bin/bash

# Copy jarfiles from Randoop to the current directory.
# Copies from the Randoop in which this script resides.
# The optional first argument is a suffix for each jarfile in the current
# directory (e.g., use "-current" if the randoop jarfiles are named
# "randoop-current.jar", "replacecall-current.jar", etc.).

# shellcheck disable=SC2012

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
RANDOOP_DIR=$(dirname "${SCRIPTDIR}")
SUFFIX=$1

# Move old versions of files to "*-ORIG", or delete if those files already exist.
if [ -f "randoop${SUFFIX}.jar-ORIG" ] ; then
  rm -f "randoop${SUFFIX}.jar" "replacecall${SUFFIX}.jar" "covered-class${SUFFIX}.jar"
else
  mv -f "randoop${SUFFIX}.jar" "randoop${SUFFIX}.jar-ORIG" 2> /dev/null
  mv -f "replacecall${SUFFIX}.jar" "replacecall${SUFFIX}.jar-ORIG" 2> /dev/null
  mv -f "covered-class${SUFFIX}.jar" "covered-class${SUFFIX}.jar-ORIG" 2> /dev/null
fi

# Get the most recent version of each file (a directory might hold multiple versions).
RANDOOP_ALL_JAR="$(ls "${RANDOOP_DIR}"/build/libs/randoop-all*.jar | tail -n1)"
REPLACECALL_JAR="$(ls "${RANDOOP_DIR}"/build/libs/replacecall*.jar | tail -n1)"
COVERED_CLASS_JAR="$(ls "${RANDOOP_DIR}"/build/libs/covered-class*.jar | tail -n1)"

# Install new versions
ln -sf "$RANDOOP_ALL_JAR" .
ln -sf "$REPLACECALL_JAR" .
ln -sf "$COVERED_CLASS_JAR" .
ln -sf "$(basename -- "$RANDOOP_ALL_JAR")" "randoop${SUFFIX}.jar"
ln -sf "$(basename -- "$REPLACECALL_JAR")" "replacecall${SUFFIX}.jar"
ln -sf "$(basename -- "$COVERED_CLASS_JAR")" "covered-class${SUFFIX}.jar"
