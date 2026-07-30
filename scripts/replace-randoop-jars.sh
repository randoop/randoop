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

# Outputs the most recently built of the arguments, which are the expansion of a glob.
# Compares modification times rather than names, because name order is not version
# order: "randoop-all-4.3.9.jar" sorts after "randoop-all-4.3.10.jar".
# The caller passes the glob unquoted, so that the shell expands it.  This works even
# if a directory or file name contains a space; parsing the output of `ls` would not,
# because `ls` may escape a space in a file name.
newest_jar() {
  # If the glob matched no file, then the shell passes the glob itself, unexpanded.
  if [ ! -e "$1" ]; then
    echo "$0: no file matches $1" >&2
    echo "$0: run \`./gradlew assemble\` in ${RANDOOP_DIR}" >&2
    exit 1
  fi
  local newest="$1"
  local file
  for file in "$@"; do
    if [ "${file}" -nt "${newest}" ]; then
      newest="${file}"
    fi
  done
  printf '%s\n' "${newest}"
}

# Read every jar file from build/distlibs, which is written by Gradle's `copyJars`
# task.  Because `copyJars` is a Sync task, build/distlibs holds only the current
# version's jar files.  (By contrast, build/libs accumulates the jar files of every
# version that has been built, and build/libs also holds the agents' "-thin" jar
# files, which do not work as Java agents.)
DISTLIBS_DIR="${RANDOOP_DIR}/build/distlibs"
# Each call to `newest_jar` runs in a subshell, so the `exit` in `newest_jar` exits
# only that subshell.  Each `|| exit 1` propagates the failure to this script.
RANDOOP_ALL_JAR="$(newest_jar "${DISTLIBS_DIR}"/randoop-all*.jar)" || exit 1
REPLACECALL_JAR="$(newest_jar "${DISTLIBS_DIR}"/replacecall*.jar)" || exit 1
COVERED_CLASS_JAR="$(newest_jar "${DISTLIBS_DIR}"/covered-class*.jar)" || exit 1

# Install new versions
ln -sf "$RANDOOP_ALL_JAR" .
ln -sf "$REPLACECALL_JAR" .
ln -sf "$COVERED_CLASS_JAR" .
ln -sf "$(basename -- "$RANDOOP_ALL_JAR")" "randoop${SUFFIX}.jar"
ln -sf "$(basename -- "$REPLACECALL_JAR")" "replacecall${SUFFIX}.jar"
ln -sf "$(basename -- "$COVERED_CLASS_JAR")" "covered-class${SUFFIX}.jar"
