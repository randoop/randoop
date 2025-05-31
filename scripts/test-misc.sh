#!/bin/bash

# This is the "misc" job of the pull request.

set -e
set -o pipefail
set -o verbose
set -o xtrace
export SHELLOPTS

# Download dependencies, trying a second time if there is a failure.
(./gradlew --write-verification-metadata sha256 help --dry-run \
  || (sleep 60 && ./gradlew --write-verification-metadata sha256 help --dry-run))

./gradlew assemble
./gradlew javadoc
echo "----------------  Javadoc warnings above  ----------------"
echo "---------------- do not cause CI failures ----------------"

status=0

## Code style
make -C scripts style-check || status=1
if grep -n -r --exclude-dir=test --exclude-dir=testInput --exclude="*~" '^\(import .*\*;$\)'; then
  echo "Don't use wildcard import"
  status=2
fi
JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1 | sed 's/-ea//')
if [ "$JAVA_VER" != "8" ]; then
  ./gradlew spotlessCheck || status=3
fi

## Javadoc documentation
PLUME_SCRIPTS=/tmp/"$USER"/plume-scripts
if [ -d "$PLUME_SCRIPTS" ]; then
  git -C "$PLUME_SCRIPTS" pull -q > /dev/null 2>&1
else
  PLUME_SCRIPTS_PARENT="$(dirname "$PLUME_SCRIPTS")"
  mkdir -p "$PLUME_SCRIPTS_PARENT" && git -C "$PLUME_SCRIPTS_PARENT" clone --depth=1 -q https://github.com/plume-lib/plume-scripts.git
fi
(./gradlew requireJavadoc --console=plain --warning-mode=all --no-daemon > /tmp/warnings-rjp.txt 2>&1) || true
"$PLUME_SCRIPTS"/ci-lint-diff /tmp/warnings-rjp.txt || status=4
(./gradlew javadoc --console=plain --warning-mode=all --no-daemon > /tmp/warnings-javadoc.txt 2>&1) || true
"$PLUME_SCRIPTS"/ci-lint-diff /tmp/warnings-javadoc.txt || status=5
(./gradlew javadocPrivate --console=plain --warning-mode=all --no-daemon > /tmp/warnings-javadocPrivate.txt 2>&1) || true
"$PLUME_SCRIPTS"/ci-lint-diff /tmp/warnings-javadocPrivate.txt || status=6

## The manual, which depends on Javadoc.
./gradlew manual || status=7

if [ "$status" -ne 0 ]; then
  echo "Look for \"status=$status\" above to see the last failure,"
  echo "though there may have been previous failures too."
  exit "$status"
fi
