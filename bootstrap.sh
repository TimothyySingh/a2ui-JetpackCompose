#!/bin/bash

# Bootstrap script for A2UI Mobile project
# This script downloads the Gradle wrapper jar file needed to build the project

echo "🚀 A2UI Mobile Bootstrap"
echo "========================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Create gradle wrapper directory if it doesn't exist
echo "📁 Creating gradle wrapper directory..."
mkdir -p gradle/wrapper

# Download gradle-wrapper.jar
echo "📥 Downloading gradle-wrapper.jar..."
WRAPPER_VERSION="8.7"
WRAPPER_URL="https://github.com/gradle/gradle/raw/v${WRAPPER_VERSION}/gradle/wrapper/gradle-wrapper.jar"

# Try to download with curl or wget
if command -v curl &> /dev/null; then
    curl -L -o gradle/wrapper/gradle-wrapper.jar "$WRAPPER_URL"
elif command -v wget &> /dev/null; then
    wget -O gradle/wrapper/gradle-wrapper.jar "$WRAPPER_URL"
else
    echo -e "${RED}❌ Error: Neither curl nor wget is available. Please install one of them.${NC}"
    exit 1
fi

# Check if download was successful
if [ -f gradle/wrapper/gradle-wrapper.jar ]; then
    echo -e "${GREEN}✅ gradle-wrapper.jar downloaded successfully${NC}"
else
    echo -e "${RED}❌ Failed to download gradle-wrapper.jar${NC}"
    exit 1
fi

# Make gradlew executable
chmod +x gradlew

echo ""
echo -e "${GREEN}✅ Bootstrap complete!${NC}"
echo ""
echo "Next steps:"
echo "1. Open the project in Android Studio"
echo "2. Android Studio will automatically download Gradle ${WRAPPER_VERSION}"
echo "3. Wait for the project to sync"
echo "4. Build and run the app"
echo ""
echo "Or from command line:"
echo "  ./gradlew build"
echo ""