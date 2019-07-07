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

# If it's a pull request, set COMMIT_RANGE and BRANCH
if [ -n "$SYSTEM_PULLREQUEST_TARGETBRANCH" ] ; then
  ## Azure Pipelines
  COMMIT_RANGE=`git rev-parse HEAD^1`...$BUILD_SOURCEVERSION
  BRANCH=$SYSTEM_PULLREQUEST_TARGETBRANCH
  STRIP_LINT=4
elif [ -n "$TRAVIS_COMMIT_RANGE" ] ; then
  ## Travis CI
  # $TRAVIS_COMMIT_RANGE is empty for builds triggered by the initial commit of a new branch.
  # Until https://github.com/travis-ci/travis-ci/issues/4596 is fixed, $TRAVIS_COMMIT_RANGE is a
  # good argument to `git diff` but a bad argument to `git log` (they interpret "..." differently!).
  COMMIT_RANGE=$TRAVIS_COMMIT_RANGE
  BRANCH=$CIRCLE_BRANCH
  STRIP_LINT=2
elif [ -n "$CIRCLE_COMPARE_URL" ] ; then
  ## CircleCI
  COMMIT_RANGE=$(echo "${CIRCLE_COMPARE_URL}" | cut -d/ -f7)
  if [[ $COMMIT_RANGE != *"..."* ]]; then
    COMMIT_RANGE="${COMMIT_RANGE}...${COMMIT_RANGE}"
  fi
  BRANCH=$TRAVIS_BRANCH
  # TODO: determine correct value
  STRIP_LINT=2
fi

echo COMMIT_RANGE=$COMMIT_RANGE
echo BRANCH=$BRANCH

if [ -n "$COMMIT_RANGE" ] ; then
  (git diff $COMMIT_RANGE > /tmp/diff.txt 2>&1) || true
  (./gradlew requireJavadocPrivate > /tmp/rjp-output.txt 2>&1) || true
  [ -s /tmp/diff.txt ] || ([[ "${BRANCH}" != "master" && "${TRAVIS_EVENT_TYPE}" == "push" ]] || (echo "/tmp/diff.txt is empty for COMMIT_RANGE=$COMMIT_RANGE; try pulling base branch (often master) into compare branch (often feature branch)" && false))
  wget https://raw.githubusercontent.com/plume-lib/plume-scripts/master/lint-diff.py
  python lint-diff.py --strip-diff=1 --strip-lint=$STRIP_LINT /tmp/diff.txt /tmp/rjp-output.txt
fi
