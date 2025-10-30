#!/bin/bash

# This is the "misc" job of the pull request.

set -e
set -o pipefail
set -o verbose
set -o xtrace
export SHELLOPTS

# Set JAVA_HOME to JDK 21 so that Gradle runs using Java 21.
# Prefer an OS-appropriate default only if JAVA21_HOME is unset and exists.
if [ -z "${JAVA21_HOME:-}" ]; then
  if [ "$(uname)" = "Darwin" ]; then
    CANDIDATE="$(/usr/libexec/java_home -v 21 2> /dev/null || true)"
    [ -n "$CANDIDATE" ] && export JAVA21_HOME="$CANDIDATE"
  elif [ -d /usr/lib/jvm/java-21-openjdk-amd64 ]; then
    export JAVA21_HOME=/usr/lib/jvm/java-21-openjdk-amd64
  fi
fi
# Only override JAVA_HOME if JAVA21_HOME points to a usable JDK.
if [ -n "${JAVA21_HOME:-}" ] && [ -x "${JAVA21_HOME}/bin/javac" ]; then
  export JAVA_HOME="${JAVA21_HOME}"
fi

# Download dependencies, trying a second time if there is a failure.
(./gradlew --write-verification-metadata sha256 help --dry-run \
  || (sleep 60 && ./gradlew --write-verification-metadata sha256 help --dry-run))

./gradlew assemble
./gradlew javadoc
echo "----------------  Javadoc warnings above  ----------------"
echo "---------------- do not cause CI failures ----------------"

failures=""

## Code style
make -C scripts style-check || failures="style-check $failures"
if grep -n -r --exclude-dir=test --exclude-dir=testInput --exclude="*~" '^\(import .*\*;$\)'; then
  echo "Don't use wildcard import"
  failures="wildcard-import $failures"
fi
JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1 | sed 's/-ea//')
if [ "$JAVA_VER" != "8" ]; then
  ./gradlew spotlessCheck || failures="spotlessCheck $failures"
fi

PLUME_SCRIPTS=/tmp/"$USER"/plume-scripts
if [ -d "$PLUME_SCRIPTS" ]; then
  git -C "$PLUME_SCRIPTS" pull -q > /dev/null 2>&1
else
  PLUME_SCRIPTS_PARENT="$(dirname "$PLUME_SCRIPTS")"
  mkdir -p "$PLUME_SCRIPTS_PARENT" && git -C "$PLUME_SCRIPTS_PARENT" clone --depth=1 -q https://github.com/plume-lib/plume-scripts.git
fi

# Additional pluggable type-checking
# The `./gradlew assemble` above handles all the type-checkers that fully pass.
if [ ! -f SKIP-REQUIRE-JAVADOC ]; then
  (./gradlew compileJava -PcfNullness --console=plain --warning-mode=all --no-daemon > /tmp/warnings-nullness.txt 2>&1) || true
  "$PLUME_SCRIPTS"/ci-lint-diff /tmp/warnings-nullness.txt || failures="nullness-compileJava $failures"
fi

## Javadoc documentation
if [ ! -f SKIP-REQUIRE-JAVADOC ]; then
  (./gradlew requireJavadoc --console=plain --warning-mode=all --no-daemon > /tmp/warnings-rjp.txt 2>&1) || true
  "$PLUME_SCRIPTS"/ci-lint-diff /tmp/warnings-rjp.txt || failures="requireJavadoc $failures"
  (./gradlew javadoc --console=plain --warning-mode=all --no-daemon > /tmp/warnings-javadoc.txt 2>&1) || true
  "$PLUME_SCRIPTS"/ci-lint-diff /tmp/warnings-javadoc.txt || failures="javadoc-warning-mode-all $failures"
  (./gradlew javadocPrivate --console=plain --warning-mode=all --no-daemon > /tmp/warnings-javadocPrivate.txt 2>&1) || true
  "$PLUME_SCRIPTS"/ci-lint-diff /tmp/warnings-javadocPrivate.txt || failures="javadocPrivate-warning-mode-all $failures"
fi

## The manual, which depends on Javadoc.
./gradlew manual || failures="manual $failures"

if [ "$failures" != "" ]; then
  echo "Here are the test failures: $failures"
  exit 1
fi
