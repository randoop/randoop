#!/bin/bash

# Optional argument $1 is one of:
#   all, test, misc
# If it is omitted, this script does everything.
export GROUP=$1
if [[ "${GROUP}" == "" ]]; then
  export GROUP=all
fi

if [[ "${GROUP}" != "all" && "${GROUP}" != "test" && "${GROUP}" != "misc" ]]; then
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

SLUGOWNER=${TRAVIS_PULL_REQUEST_SLUG%/*}
if [[ "$SLUGOWNER" == "" ]]; then
  SLUGOWNER=${TRAVIS_REPO_SLUG%/*}
fi
if [[ "$SLUGOWNER" == "" ]]; then
  SLUGOWNER=randoop
fi
echo SLUGOWNER=$SLUGOWNER

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

  # `gradle build` == `gradle check assemble`
  ./gradlew --info check
fi

if [[ "${GROUP}" == "misc" || "${GROUP}" == "all" ]]; then
  ./gradlew javadoc
  ./gradlew manual

  echo "TRAVIS_COMMIT_RANGE = $TRAVIS_COMMIT_RANGE"
  # $TRAVIS_COMMIT_RANGE is empty for builds triggered by the initial commit of a new branch.
  if [ -n "$TRAVIS_COMMIT_RANGE" ] ; then
    # Until https://github.com/travis-ci/travis-ci/issues/4596 is fixed, $TRAVIS_COMMIT_RANGE is a
    # good argument to `git diff` but a bad argument to `git log` (they interpret "..." differently!).
    (git diff $TRAVIS_COMMIT_RANGE > /tmp/diff.txt 2>&1) || true
    (./gradlew requireJavadocPrivate > /tmp/rjp-output.txt 2>&1) || true
    [ -s /tmp/diff.txt ] || ([[ "${TRAVIS_BRANCH}" != "master" && "${TRAVIS_EVENT_TYPE}" == "push" ]] || (echo "/tmp/diff.txt is empty; try pulling base branch into compare branch" && false))
    wget https://raw.githubusercontent.com/plume-lib/plume-scripts/master/lint-diff.py
    python lint-diff.py --strip-diff=1 --strip-lint=2 /tmp/diff.txt /tmp/rjp-output.txt
  fi
fi

## TODO Re-enable codecov.io code coverage tests.
## Disabled for now, because codecov is only aware of unit tests.
## Once we inform it of system tests as well, it will be more useful
## and we can turn it on without getting a lot of failures whenever we
## introduce a feature that is best tested by system tests.
# after_success:
#  - bash <(curl -s https://codecov.io/bash)
