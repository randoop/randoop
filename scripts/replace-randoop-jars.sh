#!/bin/bash

# Copy jarfiles from Randoop to the current directory.
# Copies from the Randoop in which this script resides.
# The optional first argument is a suffix for each jarfile in the current
# directory (e.g., use "-current" if the randoop jarfiles are named
# "randoop-current.jar", "replacecall-current.jar", etc.).

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" &> /dev/null && pwd)"
RANDOOP_DIR=$(dirname "${SCRIPT_DIR}")
SUFFIX=${1:-}

# Move old versions of files to "*-ORIG", or delete if those files already exist.
# Each `mv` may fail because the file does not exist, which is not an error here.
if [ -f "randoop${SUFFIX}.jar-ORIG" ]; then
  rm -f "randoop${SUFFIX}.jar" "replacecall${SUFFIX}.jar" "covered-class${SUFFIX}.jar"
else
  mv -f "randoop${SUFFIX}.jar" "randoop${SUFFIX}.jar-ORIG" 2> /dev/null || true
  mv -f "replacecall${SUFFIX}.jar" "replacecall${SUFFIX}.jar-ORIG" 2> /dev/null || true
  mv -f "covered-class${SUFFIX}.jar" "covered-class${SUFFIX}.jar-ORIG" 2> /dev/null || true
fi

# Outputs the most recently built jar file matching $1, which is a glob.
# Sorts by modification time rather than by name, because name order is not
# version order: "randoop-all-4.3.9.jar" sorts after "randoop-all-4.3.10.jar".
newest_jar() {
  local result
  # shellcheck disable=SC2012 # `ls -t` sorts by modification time; `find` does not.
  # shellcheck disable=SC2086 # $1 is a glob that must be expanded.
  result="$(ls -t $1 2> /dev/null | head -n1)"
  if [ -z "${result}" ]; then
    echo "$0: no file matches $1" >&2
    echo "$0: run \`./gradlew assemble\` in ${RANDOOP_DIR}" >&2
    exit 1
  fi
  echo "${result}"
}

# Directory build/libs accumulates the jar files of every version that has been
# built.  Directory build/distlibs is written by Gradle's `copyJars` task, which
# is a Sync task, so build/distlibs holds only the current version's jar files.
RANDOOP_ALL_JAR="$(newest_jar "${RANDOOP_DIR}/build/libs/randoop-all*.jar")"
REPLACECALL_JAR="$(newest_jar "${RANDOOP_DIR}/build/distlibs/replacecall*.jar")"
COVERED_CLASS_JAR="$(newest_jar "${RANDOOP_DIR}/build/distlibs/covered-class*.jar")"

# Install new versions
ln -sf "$RANDOOP_ALL_JAR" .
ln -sf "$REPLACECALL_JAR" .
ln -sf "$COVERED_CLASS_JAR" .
ln -sf "$(basename -- "$RANDOOP_ALL_JAR")" "randoop${SUFFIX}.jar"
ln -sf "$(basename -- "$REPLACECALL_JAR")" "replacecall${SUFFIX}.jar"
ln -sf "$(basename -- "$COVERED_CLASS_JAR")" "covered-class${SUFFIX}.jar"
