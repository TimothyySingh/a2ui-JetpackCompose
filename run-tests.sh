#!/bin/bash

# A2UI Mobile Test Runner
# Runs all tests and generates a coverage report

echo "🧪 A2UI Mobile Test Suite"
echo "========================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if gradlew exists and is executable
if [ ! -f "./gradlew" ]; then
    echo -e "${RED}✗ gradlew not found${NC}"
    echo "Run ./bootstrap.sh first to set up the project"
    exit 1
fi

# Check if gradle wrapper jar exists
if [ ! -f "./gradle/wrapper/gradle-wrapper.jar" ]; then
    echo -e "${YELLOW}⚠ Gradle wrapper not initialized${NC}"
    echo "Running bootstrap..."
    ./bootstrap.sh
    if [ $? -ne 0 ]; then
        echo -e "${RED}✗ Bootstrap failed${NC}"
        exit 1
    fi
fi

# Run unit tests
echo "📝 Running Unit Tests..."
echo "------------------------"

# Clean build
echo "🧹 Cleaning build..."
./gradlew clean

# Run common tests
echo ""
echo "🎯 Running Common Tests (all platforms)..."
./gradlew :shared:allTests

# Check result
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Common tests passed${NC}"
else
    echo -e "${RED}✗ Common tests failed${NC}"
    exit 1
fi

# Run Android tests
echo ""
echo "🤖 Running Android Tests..."
./gradlew :shared:testDebugUnitTest

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Android tests passed${NC}"
else
    echo -e "${RED}✗ Android tests failed${NC}"
    exit 1
fi

# Run iOS tests (if on macOS)
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo ""
    echo "🍎 Running iOS Tests..."
    ./gradlew :shared:iosSimulatorArm64Test
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ iOS tests passed${NC}"
    else
        echo -e "${RED}✗ iOS tests failed${NC}"
        exit 1
    fi
else
    echo ""
    echo -e "${YELLOW}⚠ Skipping iOS tests (not on macOS)${NC}"
fi

# Run integration tests for the app
echo ""
echo "🔗 Running Integration Tests..."
./gradlew :composeApp:test

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Integration tests passed${NC}"
else
    echo -e "${RED}✗ Integration tests failed${NC}"
    exit 1
fi

# Generate test report
echo ""
echo "📊 Generating Test Report..."
./gradlew :shared:koverHtmlReport 2>/dev/null || echo -e "${YELLOW}Coverage report not available (kover plugin not configured)${NC}"

echo ""
echo "========================"
echo -e "${GREEN}✅ All tests completed!${NC}"
echo ""
echo "Test Results:"
echo "- Unit Tests: ✓"
echo "- Android Tests: ✓"
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "- iOS Tests: ✓"
fi
echo "- Integration Tests: ✓"
echo ""
echo "View detailed results in:"
echo "  ./shared/build/reports/tests/"
echo ""