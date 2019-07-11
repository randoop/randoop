#!/bin/bash

# This is the "misc" job of the pull request.

set -e
set -o pipefail
set -o verbose
set -o xtrace
export SHELLOPTS

./gradlew clean assemble -PuseCheckerFramework=true
./gradlew javadoc
./gradlew checkstyle checkstyleMain checkstyleCoveredTest checkstyleReplacecallTest
./gradlew manual

git -C /tmp/plume-scripts pull > /dev/null 2>&1 \
  || git -C /tmp clone --depth 1 -q https://github.com/plume-lib/plume-scripts.git

source /tmp/plume-scripts/git-set-commit-range

echo COMMIT_RANGE=$COMMIT_RANGE
echo BRANCH=$BRANCH

if [ -n "$COMMIT_RANGE" ] ; then
  (git diff $COMMIT_RANGE > /tmp/diff.txt 2>&1) || true
  (./gradlew requireJavadocPrivate > /tmp/warnings.txt 2>&1) || true
  [ -s /tmp/diff.txt ] || ([[ "${BRANCH}" != "master" && "${TRAVIS_EVENT_TYPE}" == "push" ]] || (echo "/tmp/diff.txt is empty for COMMIT_RANGE=$COMMIT_RANGE; try pulling base branch (often master) into compare branch (often your feature branch)" && false))
  python /tmp/plume-scripts/lint-diff.py --guess-strip /tmp/diff.txt /tmp/warnings.txt
fi
