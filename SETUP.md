# A2UI Mobile - Setup Guide

## 🚀 Quick Start

### Prerequisites

1. **Android Studio** Ladybug (2024.2.1) or later
2. **JDK 17** or higher
3. **Android SDK** (API 35)
4. **Xcode 15+** (for iOS development on macOS)

## 📱 Initial Setup

### Step 1: Bootstrap the Project

The project needs the Gradle wrapper to build. Run the bootstrap script first:

```bash
cd a2ui-mobile
./bootstrap.sh
```

This will download the necessary Gradle wrapper files.

### Step 2: Configure Android SDK Location

Edit `local.properties` and set your Android SDK path:

**macOS:**
```properties
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```

**Windows:**
```properties
sdk.dir=C:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
```

**Linux:**
```properties
sdk.dir=/home/YOUR_USERNAME/Android/Sdk
```

### Step 3: Open in Android Studio

1. Open Android Studio
2. Click **"Open"** (not "Import")
3. Navigate to the `a2ui-mobile` folder
4. Click **"OK"**
5. Wait for Gradle sync to complete (this will download dependencies)

## 🔧 Troubleshooting

### Error: "gradle-wrapper.jar not found"

Run the bootstrap script:
```bash
./bootstrap.sh
```

### Error: "SDK location not found"

1. Open Android Studio → **Settings/Preferences**
2. Go to **Appearance & Behavior → System Settings → Android SDK**
3. Note the **Android SDK Location**
4. Update `local.properties` with this path

### Error: "Gradle sync failed"

1. File → **Invalidate Caches and Restart**
2. After restart, let Gradle sync again
3. If still failing, try: Build → **Clean Project**, then **Rebuild Project**

### Error: "Could not determine java version"

Ensure you have JDK 17:
```bash
java --version
```

If not, install JDK 17:
- **macOS**: `brew install openjdk@17`
- **Windows/Linux**: Download from [Oracle](https://www.oracle.com/java/technologies/downloads/#java17) or [Adoptium](https://adoptium.net/)

In Android Studio:
1. File → **Project Structure**
2. **SDK Location** → **JDK location**
3. Select JDK 17

## 🏃 Running the App

### Android

1. Create an emulator or connect a device
2. Click the **Run** button (green triangle)
3. Select your device/emulator
4. Wait for the build and deployment

Or from command line:
```bash
./gradlew :composeApp:installDebug
```

### iOS (macOS only)

1. In Android Studio, select the iOS run configuration
2. Click **Run**
3. Select iOS Simulator

Or from command line:
```bash
./gradlew :composeApp:iosSimulatorArm64
```

## 🧪 Running Tests

```bash
# All tests
./gradlew test

# Android tests only
./gradlew :shared:testDebugUnitTest

# iOS tests (macOS only)
./gradlew :shared:iosSimulatorArm64Test
```

## 📝 Project Structure

```
a2ui-mobile/
├── composeApp/          # Application module
│   ├── src/
│   │   ├── androidMain/ # Android-specific app code
│   │   ├── iosMain/     # iOS-specific app code
│   │   └── commonMain/  # Shared app code
│   └── build.gradle.kts
│
├── shared/              # Shared library module
│   ├── src/
│   │   ├── androidMain/ # Android-specific library code
│   │   ├── iosMain/     # iOS-specific library code
│   │   ├── commonMain/  # Shared library code (A2UI implementation)
│   │   └── commonTest/  # Tests
│   └── build.gradle.kts
│
├── gradle/              # Gradle wrapper
├── gradlew             # Gradle wrapper script (Unix)
├── gradlew.bat         # Gradle wrapper script (Windows)
├── settings.gradle.kts # Project settings
├── build.gradle.kts    # Root build configuration
└── local.properties    # Local configuration (SDK path)
```

## 🎨 Using the A2UI Library

### Basic Usage

```kotlin
import com.a2ui.core.render.A2UIRenderer
import com.a2ui.core.parser.A2UIParser

@Composable
fun MyApp() {
    val jsonResponse = """{"version":"0.8","root":{...}}"""
    val document = A2UIParser.parse(jsonResponse)
    
    A2UIRenderer(
        document = document,
        onAction = { event ->
            // Handle user actions
        }
    )
}
```

### With Custom Components

```kotlin
import com.a2ui.core.provider.A2UIProvider
import com.a2ui.core.registry.ComponentRegistry
import com.a2ui.core.render.A2UIExtendedRenderer

@Composable
fun MyCustomApp() {
    val customRegistry = ComponentRegistry().apply {
        register("BUTTON") { node, onAction, modifier ->
            MyCustomButton(node, onAction, modifier)
        }
    }
    
    A2UIProvider(componentRegistry = customRegistry) {
        A2UIExtendedRenderer(document, onAction)
    }
}
```

See [README_EXAMPLES.md](README_EXAMPLES.md) for complete examples.

## 🆘 Getting Help

1. Check the [README_EXTENDED.md](README_EXTENDED.md) for API documentation
2. Review [README_EXAMPLES.md](README_EXAMPLES.md) for usage patterns
3. Look at the test files in `shared/src/commonTest/` for examples
4. Check Android Studio logs: **View → Tool Windows → Build**

## ✅ Verification Checklist

After setup, verify everything works:

- [ ] `./gradlew build` completes successfully
- [ ] Android Studio recognizes the project structure
- [ ] You can see `composeApp` and `shared` modules in the Project view
- [ ] Gradle sync completes without errors
- [ ] You can run the app on an emulator/device
- [ ] Tests pass: `./gradlew test`

## 🎉 Ready to Build!

Once setup is complete, you can:

1. Modify the example app in `composeApp/`
2. Extend the A2UI library in `shared/`
3. Add custom components using the registry system
4. Create themes and apply them
5. Handle A2UI responses from your agent

Happy coding! 🚀