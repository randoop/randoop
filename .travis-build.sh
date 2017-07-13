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

SLUGOWNER=${TRAVIS_REPO_SLUG%/*}
if [[ "$SLUGOWNER" == "" ]]; then
  SLUGOWNER=randoop
fi

./.travis-build-without-test.sh ${BUILDJDK}

if [[ "${GROUP}" == "test" || "${GROUP}" == "all" ]]; then
  # ./gradlew --info check
  ./gradlew --info check
fi

if [[ "${GROUP}" == "misc" || "${GROUP}" == "all" ]]; then
  ./gradlew javadoc
  ./gradlew manual
fi

## TODO Re-enable codecov.io code coverage tests.
## Disabled for now, because codecov is only aware of unit tests.
## Once we inform it of system tests as well, it will be more useful
## and we can turn it on without getting a lot of failures whenever we
## introduce a feature that is best tested by system tests.
# after_success:
#  - bash <(curl -s https://codecov.io/bash)
