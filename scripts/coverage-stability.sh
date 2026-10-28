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
#   --test TESTMETHOD  a test method in RandoopSystemTest; default: runJDKTest
#   --jdk VERSION      the JDK version for running tests; default: Gradle's JDK
#   SEED ...           the random seeds to use; default: 1 2 3 4 5
#
# Each seed requires a separate run of the system test.  The test fails when
# its coverage differs from its coverage goals, and this script ignores such
# failures.  If a run fails before checking coverage (for example, because
# Randoop crashed), this script omits that seed from the report.  The per-seed
# results and the report are written to build/coverage-stability/.

set -e
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
ROOT_DIR="$(dirname "${SCRIPT_DIR}")"

TEST_METHOD=runJDKTest
GRADLE_ARGS=()
while [ $# -gt 0 ]; do
  case "$1" in
    --test)
      TEST_METHOD="$2"
      shift 2
      ;;
    --jdk)
      GRADLE_ARGS+=("-PjdkTestVersion=$2")
      shift 2
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
  set -- 1 2 3 4 5
fi

OUT_DIR="${ROOT_DIR}/build/coverage-stability/${TEST_METHOD}"
mkdir -p "${OUT_DIR}"

COVERAGE_FILES=()
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
  END {
    for (m in count) {
      if (count[m] == numSeeds) {
        always++
      } else if (count[m] == 0) {
        never++
      } else {
        some++
        printf "%d/%d %s\n", count[m], numSeeds, m | "sort -k2"
      }
    }
    close("sort -k2")
    printf "\nCovered under all %d seeds: %d methods\n", numSeeds, always
    printf "Covered under some seeds but not all: %d methods\n", some
    printf "Covered under no seed: %d methods\n", never
  }
' "${COVERAGE_FILES[@]}" > "${REPORT_FILE}"

echo
echo "Methods covered under some seeds but not all (also in ${REPORT_FILE}):"
cat "${REPORT_FILE}"
