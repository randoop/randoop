#!/bin/bash

# This runs all of `./gradlew check` except for `./gradlew systemTest`.

set -e
set -o pipefail
set -o verbose
set -o xtrace
export SHELLOPTS

# Download dependencies, trying a second time if there is a failure.
(./gradlew --write-verification-metadata sha256 help --dry-run ||
     (sleep 60 && ./gradlew --write-verification-metadata sha256 help --dry-run))

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
./gradlew --info --stacktrace test coveredTest replacecallTest
