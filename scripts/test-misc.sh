#!/bin/bash

# This is the "misc" job of the pull request.

set -e
set -o pipefail
set -o verbose
set -o xtrace
export SHELLOPTS

env
set
export

./gradlew clean assemble -PuseCheckerFramework=true
./gradlew javadoc
./gradlew checkstyle checkstyleMain checkstyleCoveredTest checkstyleReplacecallTest
./gradlew manual

# $TRAVIS_COMMIT_RANGE is empty for builds triggered by the initial commit of a new branch.
# Until https://github.com/travis-ci/travis-ci/issues/4596 is fixed, $TRAVIS_COMMIT_RANGE is a
# good argument to `git diff` but a bad argument to `git log` (they interpret "..." differently!).
COMMIT_RANGE=$TRAVIS_COMMIT_RANGE
if [ -n "$CIRCLE_COMPARE_URL" ] ; then
  COMMIT_RANGE=$(echo "${CIRCLE_COMPARE_URL}" | cut -d/ -f7)
  if [[ $COMMIT_RANGE != *"..."* ]]; then
    COMMIT_RANGE="${COMMIT_RANGE}...${COMMIT_RANGE}"
  fi
fi
BRANCH=${TRAVIS_BRANCH:=$CIRCLE_BRANCH}

echo COMMIT_RANGE=$COMMIT_RANGE
echo BRANCH=$BRANCH

if [ -n "$COMMIT_RANGE" ] ; then
  (git diff $COMMIT_RANGE > /tmp/diff.txt 2>&1) || true
  (./gradlew requireJavadocPrivate > /tmp/rjp-output.txt 2>&1) || true
  [ -s /tmp/diff.txt ] || ([[ "${BRANCH}" != "master" && "${TRAVIS_EVENT_TYPE}" == "push" ]] || (echo "/tmp/diff.txt is empty; try pulling base branch (often master) into compare branch (often feature branch)" && false))
  wget -q https://raw.githubusercontent.com/plume-lib/plume-scripts/master/lint-diff.py
  python lint-diff.py --strip-diff=1 --strip-lint=2 /tmp/diff.txt /tmp/rjp-output.txt
fi
