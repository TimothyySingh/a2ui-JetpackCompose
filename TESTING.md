# A2UI Mobile Testing Guide

## 📋 Test Coverage Overview

The A2UI Mobile Extended library includes comprehensive test coverage for all major components:

### ✅ Unit Tests

| Component | Test File | Coverage |
|-----------|-----------|----------|
| ComponentRegistry | `ComponentRegistryTest.kt` | Registration, retrieval, merging, copying |
| A2UITheme | `A2UIThemeTest.kt` | Theme building, presets, customization |
| A2UIProvider | `A2UIProviderTest.kt` | Context provision, nesting, configuration |
| A2UIExtendedRenderer | `A2UIExtendedRendererTest.kt` | Custom rendering, fallbacks, actions |
| Integration | `A2UIIntegrationTest.kt` | End-to-end scenarios, forms, themes |

## 🚀 Running Tests

### Quick Start
```bash
# Run all tests
./run-tests.sh

# Run specific test module
./gradlew :shared:test

# Run with coverage
./gradlew :shared:koverHtmlReport
```

### Platform-Specific Tests

**Android:**
```bash
./gradlew :shared:testDebugUnitTest
```

**iOS (macOS only):**
```bash
./gradlew :shared:iosSimulatorArm64Test
```

**Common (all platforms):**
```bash
./gradlew :shared:allTests
```

## 🧪 Test Categories

### 1. Component Registry Tests

Tests the component override system:

```kotlin
@Test
fun testRegisterAndGet() {
    val registry = ComponentRegistry()
    registry.register("BUTTON", myRenderer)
    assertTrue(registry.has("BUTTON"))
}

@Test
fun testMergeRegistries() {
    registry1.merge(registry2)
    // Verify merged components
}
```

**What's tested:**
- Component registration and retrieval
- Registry merging and copying
- Overwriting existing components
- Runtime modifications
- Custom component types

### 2. Theme System Tests

Tests the theming capabilities:

```kotlin
@Test
fun testThemeBuilder() {
    val theme = buildA2UITheme {
        colors { copy(primary = Color.Blue) }
        typography { copy(h1 = h1.copy(fontSize = 40.sp)) }
    }
    assertEquals(Color.Blue, theme.colors.primary)
}
```

**What's tested:**
- Default themes (Light, Dark, HighContrast)
- Theme builder DSL
- Component style customization
- Color, typography, spacing systems
- Data class immutability

### 3. Provider Tests

Tests the dependency injection system:

```kotlin
@Test
fun testProviderProvidesComponents() {
    composeTestRule.setContent {
        A2UIProvider(componentRegistry = customRegistry) {
            val registry = useComponentRegistry()
            assertTrue(registry.has("CUSTOM"))
        }
    }
}
```

**What's tested:**
- CompositionLocal provision
- Nested providers
- Config-based setup
- Default values
- Hook functions

### 4. Renderer Tests

Tests the extended rendering system:

```kotlin
@Test
fun testCustomComponentOverride() {
    val registry = ComponentRegistry().apply {
        register("BUTTON", ::CustomButton)
    }
    // Verify custom button is rendered
}
```

**What's tested:**
- Custom component rendering
- Default component fallback
- Action handling
- Modifier propagation
- State management in custom components

### 5. Integration Tests

Tests the complete system:

```kotlin
@Test
fun testCompleteFormScenario() {
    // Tests form with custom fields
    // Verifies data collection and submission
}
```

**What's tested:**
- Complete rendering pipeline
- Form handling scenarios
- Theme switching at runtime
- Registry modifications
- Large component trees
- Error handling

## 🎯 Testing Custom Components

### Example: Testing a Custom Button

```kotlin
class CustomButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testCustomButtonRendering() {
        val registry = ComponentRegistry().apply {
            register(A2UINodeType.BUTTON.name, ::GradientButton)
        }
        
        val document = A2UIDocument(
            version = "0.8",
            root = A2UINode(
                type = A2UINodeType.BUTTON,
                props = A2UIProps(text = "Click Me")
            )
        )
        
        composeTestRule.setContent {
            A2UIProvider(componentRegistry = registry) {
                A2UIExtendedRenderer(document = document)
            }
        }
        
        composeTestRule
            .onNodeWithText("Click Me")
            .assertExists()
            .performClick()
    }
}
```

### Example: Testing Theme Application

```kotlin
@Test
fun testDarkThemeColors() {
    composeTestRule.setContent {
        A2UIProvider(theme = A2UITheme.Dark) {
            val theme = useA2UITheme()
            Box(
                modifier = Modifier
                    .testTag("themed-box")
                    .background(theme.colors.background)
            )
        }
    }
    
    // Verify dark background is applied
    composeTestRule.onNodeWithTag("themed-box").assertExists()
}
```

## 📊 Coverage Targets

### Minimum Coverage Goals

- **ComponentRegistry:** 100% - Core functionality
- **Theme System:** 95% - All customization paths
- **Provider:** 90% - Context provision
- **Renderer:** 85% - Component rendering paths
- **Integration:** 80% - Key user scenarios

### Current Coverage

Run coverage report:
```bash
./gradlew koverHtmlReport
open shared/build/reports/kover/html/index.html
```

## 🔍 Debugging Tests

### Enable Verbose Output
```bash
./gradlew test --info
```

### Run Single Test
```kotlin
./gradlew test --tests "*ComponentRegistryTest.testRegisterAndGet"
```

### Debug Mode
```bash
./gradlew test --debug-jvm
```

## 🏗️ Test Infrastructure

### Test Dependencies

**Common Test:**
```kotlin
implementation(kotlin("test"))
implementation(compose.uiTest)
implementation(libs.kotlinx.coroutines.test)
```

**Android Test:**
```kotlin
implementation(kotlin("test-junit"))
implementation(compose.desktop.uiTestJUnit4)
implementation(libs.mockito.core)
```

### Test Utilities

**Test Data Builders:**
```kotlin
object A2UITestData {
    fun simpleButton(text: String): A2UINode
    fun simpleCard(vararg children: A2UINode): A2UINode
    fun simpleDocument(root: A2UINode): A2UIDocument
}
```

**Integration Helpers:**
```kotlin
object IntegrationTestUtils {
    fun createFormDocument(fields: List<String>): A2UIDocument
    fun createDashboardDocument(): A2UIDocument
}
```

## ✅ Checklist for New Features

When adding new features, ensure:

1. [ ] Unit tests for new components
2. [ ] Integration test for feature workflow
3. [ ] Theme compatibility test
4. [ ] Registry registration test
5. [ ] Action handling test
6. [ ] Error scenario test
7. [ ] Documentation updated
8. [ ] Example added to CustomComponentExample

## 🐛 Common Test Issues

### Issue: Compose tests not running
**Solution:** Ensure compose test dependencies are included:
```kotlin
implementation(compose.uiTest)
```

### Issue: Provider not found in tests
**Solution:** Wrap test content with A2UIProvider:
```kotlin
A2UIProvider {
    // Your test content
}
```

### Issue: Custom component not rendering
**Solution:** Verify registration and node type match:
```kotlin
registry.register(A2UINodeType.BUTTON.name, ::CustomButton)
```

## 📚 Further Reading

- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Kotlin Multiplatform Testing](https://kotlinlang.org/docs/multiplatform-test.html)
- [JUnit Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito for Kotlin](https://github.com/mockito/mockito-kotlin)