#!/bin/bash

# Reports which methods a system test covers under some random seeds but not
# others.  Such methods are the ones whose coverage goals (in
# src/systemTest/resources/test-covgoals/) are likely to change when Randoop
# is edited, because an edit to Randoop changes its sequence of random choices
# much as a new seed does.
#
# Usage:
#   scripts/coverage-stability.sh [--test TESTMETHOD] [--jdk VERSION] [SEED ...]
#
#   --test TESTMETHOD  a test method in RandoopSystemTest that checks coverage
#                      (that is, calls checkCoverage); default: runJDKTest
#   --jdk VERSION      the JDK version for running tests; default: Gradle's JDK
#   SEED ...           the random seeds to use; default: 0 1 2 3 4
#                      (0 is Randoop's default seed)
#
# Randoop and the tests that it generates run under the "java" command on PATH,
# so the coverage is that of the JDK on PATH.  If --jdk is given, the "java"
# command on PATH must be that JDK version.
#
# Each seed requires a separate run of the system test.  The test fails when
# its coverage differs from its coverage goals, and this script ignores such
# failures.  If a run fails before checking coverage (for example, because
# Randoop crashed), this script omits that seed from the report's counts and
# lists it at the end of the report.  The per-seed
# results and the report are written to build/coverage-stability/.

set -e
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
ROOT_DIR="$(dirname "${SCRIPT_DIR}")"

TEST_METHOD=runJDKTest
JDK_VERSION=
GRADLE_ARGS=()
while [ $# -gt 0 ]; do
  case "$1" in
    --test)
      if [ $# -lt 2 ]; then
        echo "$0: $1 requires an argument" >&2
        exit 2
      fi
      TEST_METHOD="$2"
      shift 2
      ;;
    --jdk)
      if [ $# -lt 2 ]; then
        echo "$0: $1 requires an argument" >&2
        exit 2
      fi
      JDK_VERSION="$2"
      GRADLE_ARGS+=("-PjdkTestVersion=$2")
      shift 2
      ;;
    -[0-9]*)
      break
      ;;
    -*)
      echo "$0: unrecognized option: $1" >&2
      exit 2
      ;;
    *)
      break
      ;;
  esac
done
if [ $# -eq 0 ]; then
  set -- 0 1 2 3 4
fi
for seed in "$@"; do
  if [[ ! "${seed}" =~ ^-?[0-9]+$ ]]; then
    echo "$0: bad seed, not an integer: ${seed}" >&2
    exit 2
  fi
done

if [ -n "${JDK_VERSION}" ]; then
  # The major version of the "java" command on PATH, such as 8 for "1.8.0_392".
  path_java_version="$(java -version 2>&1 | sed -n 's/.* version "\([^"]*\)".*/\1/p' | head -n 1)" || true
  path_java_version="${path_java_version#1.}"
  path_java_version="${path_java_version%%[^0-9]*}"
  if [ "${path_java_version}" != "${JDK_VERSION}" ]; then
    echo "$0: --jdk is ${JDK_VERSION}, but the java command on PATH is version ${path_java_version:-unknown}." >&2
    echo "Randoop runs under the java command on PATH, so put JDK ${JDK_VERSION} first on PATH." >&2
    exit 2
  fi
fi

OUT_DIR="${ROOT_DIR}/build/coverage-stability/${TEST_METHOD}"
mkdir -p "${OUT_DIR}"

COVERAGE_FILES=()
OMITTED_SEEDS=()
for seed in "$@"; do
  coverage_file="${OUT_DIR}/seed-${seed}.txt"
  log_file="${OUT_DIR}/seed-${seed}.log"
  rm -f "${coverage_file}"
  echo "Running ${TEST_METHOD} with seed ${seed}; log is in ${log_file}"
  "${ROOT_DIR}/gradlew" -p "${ROOT_DIR}" systemTest --rerun \
    --tests "randoop.main.RandoopSystemTest.${TEST_METHOD}" \
    "-Drandoop.randomseed=${seed}" \
    "-Drandoop.method.coverage.file=${coverage_file}" \
    "${GRADLE_ARGS[@]}" > "${log_file}" 2>&1 || true
  if [ -f "${coverage_file}" ]; then
    COVERAGE_FILES+=("${coverage_file}")
  else
    OMITTED_SEEDS+=("${seed}")
    echo "$0: omitting seed ${seed}, whose run recorded no coverage; see ${log_file}" >&2
  fi
done
if [ ${#COVERAGE_FILES[@]} -eq 0 ]; then
  echo "$0: no run recorded coverage" >&2
  exit 1
fi

REPORT_FILE="${OUT_DIR}/report.txt"
# Each line of a coverage file is "covered METHOD" or "uncovered METHOD".
awk -v numSeeds=${#COVERAGE_FILES[@]} '
  $1 == "covered" { count[substr($0, 9)]++ }
  $1 == "uncovered" { count[substr($0, 11)] += 0 }
  BEGIN { sortCmd = "LC_ALL=C sort -k2" }
  END {
    for (m in count) {
      if (count[m] == numSeeds) {
        always++
      } else if (count[m] == 0) {
        never++
      } else {
        some++
        printf "%d/%d %s\n", count[m], numSeeds, m | sortCmd
      }
    }
    close(sortCmd)
    printf "\nCovered under all %d seeds: %d methods\n", numSeeds, always
    printf "Covered under some seeds but not all: %d methods\n", some
    printf "Covered under no seed: %d methods\n", never
  }
' "${COVERAGE_FILES[@]}" > "${REPORT_FILE}"
if [ ${#OMITTED_SEEDS[@]} -gt 0 ]; then
  echo "Omitted seeds, whose runs recorded no coverage: ${OMITTED_SEEDS[*]}" >> "${REPORT_FILE}"
fi

echo
echo "Methods covered under some seeds but not all (also in ${REPORT_FILE}):"
cat "${REPORT_FILE}"
