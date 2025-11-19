#!/bin/bash

# This runs all of `./gradlew check` except for `./gradlew systemTest`.

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
if [ -z "${JAVA_GRADLE_HOME:-}" ]; then
  echo "Error: Neither Java 21 nor Java 24 found. Cannot run Gradle."
  exit 1
fi

# Download dependencies, trying a second time if there is a failure.
(./gradlew --write-verification-metadata sha256 help --dry-run \
  || (sleep 60 && ./gradlew --write-verification-metadata sha256 help --dry-run))

./gradlew assemble

# Need GUI for running tests of replace call agent with Swing/AWT.
# Run xvfb.
export DISPLAY=:99.0
XVFB=/usr/bin/Xvfb
XVFBARGS="$DISPLAY -ac -screen 0 1024x768x16 +extension RANDR"
PIDFILE=/tmp/xvfb_${DISPLAY:1}.pid
# shellcheck disable=SC2086
/sbin/start-stop-daemon --start --quiet --pidfile "$PIDFILE" --make-pidfile --background --exec "$XVFB" -- $XVFBARGS
sleep 3 # give xvfb some time to start

./gradlew printJunitJarPath

# `gradle build` == `gradle check assemble`.
./gradlew --info test replacecallAgentTest

# Stop xvfb because 'start-stop-daemon --start' will fail if already running.
/sbin/start-stop-daemon --stop --quiet --oknodo --pidfile "$PIDFILE" || true
