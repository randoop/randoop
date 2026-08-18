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
  local newest=""
  local file
  for file in "$@"; do
    # Ignore jar files that are not distribution artifacts, and ignore the glob itself
    # if the shell passed it unexpanded because it matched no file.
    case "${file}" in
      *-thin.jar | *-javadoc.jar | *-sources.jar) continue ;;
    esac
    if [ ! -e "${file}" ]; then
      continue
    fi
    if [ -z "${newest}" ] || [ "${file}" -nt "${newest}" ]; then
      newest="${file}"
    fi
  done
  if [ -z "${newest}" ]; then
    # Report every argument, because "$1" may be a file that was skipped above, and
    # reporting only "$1" would suggest that the file does not exist.
    echo "$0: no distribution jar file among: $*" >&2
    echo "$0: run \`./gradlew assemble\` in ${RANDOOP_DIR}" >&2
    exit 1
  fi
  printf '%s\n' "${newest}"
}

# Read every jar file from build/libs, into which Gradle's `copyJars` task copies the
# agents' jar files.  build/libs accumulates the jar files of every version that has
# been built, so `newest_jar` chooses the most recently built one.  build/libs also
# holds jar files that are not part of the distribution: the agents' "-thin" jar
# files, which do not work as Java agents, and "-javadoc" and "-sources" jar files.
LIBS_DIR="${RANDOOP_DIR}/build/libs"
# The call to `newest_jar` runs in a subshell, so the `exit` in `newest_jar` exits only
# that subshell.  The `|| exit 1` propagates the failure to this script.
RANDOOP_ALL_JAR="$(newest_jar "${LIBS_DIR}"/randoop-all*.jar)" || exit 1

# Choose the agents' jar files by version rather than by modification time, so that all
# three jar files come from the same build.  Choosing each of the three independently
# by modification time would link, say, the 4.3.10 randoop-all jar file with the 4.3.9
# agent jar files, if the latest build rebuilt only some of the three.
VERSION="$(basename -- "${RANDOOP_ALL_JAR}" .jar)"
VERSION="${VERSION#randoop-all-}"
REPLACECALL_JAR="${LIBS_DIR}/replacecall-${VERSION}.jar"
COVERED_CLASS_JAR="${LIBS_DIR}/covered-class-${VERSION}.jar"
for jarfile in "${REPLACECALL_JAR}" "${COVERED_CLASS_JAR}"; do
  if [ ! -e "${jarfile}" ]; then
    echo "$0: no version ${VERSION} agent jar file: ${jarfile}" >&2
    echo "$0: run \`./gradlew assemble\` in ${RANDOOP_DIR}" >&2
    exit 1
  fi
done

# Install new versions
ln -sf "$RANDOOP_ALL_JAR" .
ln -sf "$REPLACECALL_JAR" .
ln -sf "$COVERED_CLASS_JAR" .
ln -sf "$(basename -- "$RANDOOP_ALL_JAR")" "randoop${SUFFIX}.jar"
ln -sf "$(basename -- "$REPLACECALL_JAR")" "replacecall${SUFFIX}.jar"
ln -sf "$(basename -- "$COVERED_CLASS_JAR")" "covered-class${SUFFIX}.jar"
