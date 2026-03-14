#!/bin/bash
set -e

ROOT=$(cd "$(dirname "$0")/.."; pwd)
cd "$ROOT"

# Clean up previous downloads
rm -rf "$ROOT/build/bin"

# Determine OS
OS_NAME=""
case "$(uname -s)" in
    Linux*)     OS_NAME=linux;;
    Darwin*)    OS_NAME=osx;;
    CYGWIN*|MINGW*|MSYS*) OS_NAME=win;;
    *)          echo "Unsupported OS"; exit 1;;
esac

# Get the Projucer version from CMakeLists.txt
JUCE_VERSION=$(grep 'project(JUCE VERSION' owlplug-host/modules/JUCE/CMakeLists.txt | grep -o '[0-9.]*' | head -n 1)

if [ -z "$JUCE_VERSION" ]; then
    echo "Could not find JUCE version in CMakeLists.txt"
    exit 1
fi

echo "JUCE Version: $JUCE_VERSION"
echo "OS: $OS_NAME"

# Download the Projucer
mkdir -p "$ROOT/build/bin"
cd "$ROOT/build/bin"

DOWNLOAD_URL="https://github.com/juce-framework/JUCE/releases/download/$JUCE_VERSION/juce-$JUCE_VERSION-$OS_NAME.zip"
echo "Downloading Projucer from $DOWNLOAD_URL"

curl -L "$DOWNLOAD_URL" --output "JUCE.zip"
unzip -o JUCE.zip
