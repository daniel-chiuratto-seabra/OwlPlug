#!/bin/bash

ROOT=$(cd "$(dirname "$0")/.."; pwd)
echo "The ROOT path being used is: $ROOT"

echo $JAVA_HOME
echo "The JAVA_HOME path being used is: $JAVA_HOME"

ls $JAVA_HOME/include/darwin

# Resave jucer files
"$ROOT/build/bin/JUCE/Projucer.app/Contents/MacOS/Projucer" --resave "$ROOT/owlplug-host/src/main/juce/OwlPlugHost.jucer"

cd "$ROOT/owlplug-host/src/main/juce/Builds/MacOSX"
xcodebuild -configuration Release || exit 1
