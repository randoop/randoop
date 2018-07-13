#!/bin/bash

# Optional argument $1 is one of:
#   all, test, misc
# If it is omitted, this script does everything.
export GROUP=$1
if [[ "${GROUP}" == "" ]]; then
  export GROUP=all
fi

if [[ "${GROUP}" != "all" && "${GROUP}" != "test" && "${GROUP}" != "misc" && "${GROUP}" != "diff" ]]; then
  echo "Bad argument '${GROUP}'; should be omitted or one of: all, test, misc"
  exit 1
fi

# Fail the whole script if any command fails
set -e

## Diagnostic output
# Output lines of this script as they are read.
set -o verbose
# Output expanded lines of this script as they are executed.
set -o xtrace

export SHELLOPTS

SLUGOWNER=${TRAVIS_REPO_SLUG%/*}
if [[ "$SLUGOWNER" == "" ]]; then
  SLUGOWNER=randoop
fi

./.travis-build-without-test.sh

# If you don't have xvfb running, then you should probably run gradle directly
# rather than running this script.
if [[ "${GROUP}" == "test" || "${GROUP}" == "all" ]]; then
  # need gui for running tests of replace call agent with Swing/AWT
  # run xvfb
  export DISPLAY=:99.0
  XVFB=/usr/bin/Xvfb
  XVFBARGS="$DISPLAY -ac -screen 0 1024x768x16 +extension RANDR"
  PIDFILE=/var/xvfb_${DISPLAY:1}.pid
  /sbin/start-stop-daemon --start --quiet --pidfile $PIDFILE --make-pidfile --background --exec $XVFB -- $XVFBARGS
  sleep 3 # give xvfb some time to start

  # ./gradlew --info check
  ./gradlew --info check
fi

if [[ "${GROUP}" == "misc" || "${GROUP}" == "all" ]]; then
  ./gradlew javadoc
  ./gradlew manual
fi

## TODO: merge into "misc" once it is working.
if [[ "${GROUP}" == "diff" || "${GROUP}" == "all" ]]; then
  echo "TRAVIS_BRANCH = $TRAVIS_BRANCH"
  echo "TRAVIS_COMMIT_RANGE = $TRAVIS_COMMIT_RANGE"
  (git diff $TRAVIS_COMMIT_RANGE > /tmp/diff.txt 2>&1) || true
  # The change to TRAVIS_COMMIT_RANGE is due to travis-ci/travis-ci#4596 .
  (git diff "${TRAVIS_COMMIT_RANGE/.../..}" > /tmp/diff2.txt 2>&1) || true
  # (git diff HEAD...$TRAVIS_BRANCH > /tmp/diff.txt 2>&1) || true
  # (git diff $(git merge-base origin/master...HEAD) > /tmp/diff1.txt 2>&1) || true
  (./gradlew requireJavadoc > /tmp/output.txt 2>&1) || true
  ls -l /tmp
  echo "/tmp/diff.txt"
  cat /tmp/diff.txt
  echo "/tmp/diff2.txt"
  cat /tmp/diff2.txt
  [ -s /tmp/diff.txt ] || (echo "/tmp/diff.txt is empty" && false)
  echo "difffilter output:"
  curl -s -O https://raw.githubusercontent.com/plume-lib/plume-scripts/master/lint-diff.py
  python lint-diff.py /tmp/diff.txt /tmp/output.txt
fi

## TODO Re-enable codecov.io code coverage tests.
## Disabled for now, because codecov is only aware of unit tests.
## Once we inform it of system tests as well, it will be more useful
## and we can turn it on without getting a lot of failures whenever we
## introduce a feature that is best tested by system tests.
# after_success:
#  - bash <(curl -s https://codecov.io/bash)
