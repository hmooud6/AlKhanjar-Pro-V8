#!/bin/bash
echo "Building AlKhanjar Pro V8.0..."
chmod +x gradlew
./gradlew clean
./gradlew assembleRelease
echo "Build complete! APK location:"
echo "app/build/outputs/apk/release/app-release.apk"
