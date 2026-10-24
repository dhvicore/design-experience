#!/bin/sh
# Build/install updates in place. Never uninstall the demo app or clear its data.
set -eu
cd "$(dirname "$0")/.."
adb_bin="${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb"
if [ ! -x "$adb_bin" ]; then adb_bin=adb; fi
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest --offline
"$adb_bin" install -r app/build/outputs/apk/debug/app-debug.apk
"$adb_bin" install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
# Optional fully qualified test class; otherwise run the normal suite (GPU tests skip).
test_report=$(mktemp)
trap 'rm -f "$test_report"' EXIT HUP INT TERM
if [ "$#" -gt 0 ]; then
    "$adb_bin" shell am instrument -w -e class "$1" com.nmims.songstudio.test/androidx.test.runner.AndroidJUnitRunner > "$test_report"
else
    "$adb_bin" shell am instrument -w com.nmims.songstudio.test/androidx.test.runner.AndroidJUnitRunner > "$test_report"
fi
cat "$test_report"
if rg -q 'FAILURES!!!|INSTRUMENTATION_FAILED|shortMsg=' "$test_report"; then exit 1; fi
