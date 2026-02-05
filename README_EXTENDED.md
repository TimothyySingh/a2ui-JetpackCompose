# A2UI Mobile - Extended Edition

A Kotlin Multiplatform implementation of A2UI (Agent-to-User Interface) for Android and iOS with **full component customization support**.

## ✨ New Features

This extended version adds powerful customization capabilities on top of the base A2UI Mobile:

- 🎭 **Component Override System** - Replace any component with your own implementation
- 🎨 **Custom Theme Support** - Full theming with colors, typography, spacing, and shapes
- 🧩 **Extensible Architecture** - Add new component types without modifying core code
- 🔌 **Registry Pattern** - Inspired by React's component registry for maximum flexibility
- 🎯 **Type-safe Overrides** - Full Kotlin type safety for custom components

## 🚀 Quick Start

### Basic Usage (Default Components)

```kotlin
@Composable
fun BasicApp() {
    A2UIRenderer(
        document = myDocument,
        onAction = { event -> handleAction(event) }
    )
}
```

### With Custom Components

```kotlin
@Composable
fun CustomApp() {
    val customRegistry = ComponentRegistry().apply {
        // Override default button with custom implementation
        register(A2UINodeType.BUTTON.name) { node, onAction, modifier ->
            MyCustomButton(node, onAction, modifier)
        }
    }
    
    A2UIProvider(componentRegistry = customRegistry) {
        A2UIExtendedRenderer(
            document = myDocument,
            onAction = { event -> handleAction(event) }
        )
    }
}
```

## 🎨 Component Customization

### Simple Component Override

Replace any built-in component with your own:

```kotlin
@Composable
fun MyCustomButton(
    node: A2UINode,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    // Your custom button implementation
    OutlinedButton(
        onClick = {
            node.actions?.find { it.event == A2UIEventType.CLICK }?.let {
                onAction(A2UIActionEvent(node.id, it.handler, it.payload))
            }
        },
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF667EEA)
        ),
        border = BorderStroke(2.dp, Color(0xFF667EEA))
    ) {
        Icon(Icons.Default.Star, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(
            node.props?.text ?: "Button",
            fontWeight = FontWeight.Bold
        )
    }
}
```

### Advanced: Gradient Button Example

Create sophisticated custom components:

```kotlin
@Composable
fun GradientButton(
    node: A2UINode,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val clickAction = node.actions?.find { it.event == A2UIEventType.CLICK }
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f
    )
    
    Button(
        onClick = {
            clickAction?.let {
                onAction(A2UIActionEvent(node.id, it.handler, it.payload))
            }
        },
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF667EEA),
                            Color(0xFF764BA2)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = node.props?.text ?: "Button",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

## 🎯 Component Registry API

### Creating Registries

```kotlin
// Empty registry
val registry = ComponentRegistry()

// With builder pattern
val registry = ComponentRegistry.withDefaults {
    register("BUTTON", ::MyButton)
    register("CARD", ::MyCard)
    register("CUSTOM_CHART", ::ChartWidget)
}
```

### Registry Operations

```kotlin
// Register components
registry.register("BUTTON") { node, onAction, modifier ->
    CustomButton(node, onAction, modifier)
}

// Register multiple at once
registry.registerAll(
    mapOf(
        "BUTTON" to ::CustomButton,
        "CARD" to ::CustomCard,
        "TEXT" to ::CustomText
    )
)

// Check registration
if (registry.has("BUTTON")) {
    println("Button is customized")
}

// Get renderer
val renderer = registry.get("BUTTON")

// Remove customization
registry.unregister("BUTTON")

// Copy registry
val newRegistry = registry.copy()

// Merge registries
registry.merge(anotherRegistry)
```

## 🎨 Theme System

### Built-in Themes

```kotlin
// Use pre-defined themes
A2UIProvider(theme = A2UITheme.Default) { /* ... */ }
A2UIProvider(theme = A2UITheme.Dark) { /* ... */ }
A2UIProvider(theme = A2UITheme.HighContrast) { /* ... */ }
```

### Custom Theme Builder

```kotlin
val customTheme = buildA2UITheme {
    colors {
        copy(
            primary = Color(0xFF667EEA),
            primaryVariant = Color(0xFF764BA2),
            secondary = Color(0xFFFFC107),
            background = Color(0xFFF7F8FA),
            surface = Color.White,
            onPrimary = Color.White,
            onSurface = Color.Black
        )
    }
    
    typography {
        copy(
            h1 = A2UITheme.TextStyle(
                fontSize = 36.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Black
            ),
            body = A2UITheme.TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal
            )
        )
    }
    
    spacing {
        copy(
            xs = 4.dp,
            sm = 8.dp,
            md = 16.dp,
            lg = 32.dp,
            xl = 48.dp
        )
    }
    
    shapes {
        copy(
            small = 4.dp,
            medium = 8.dp,
            large = 16.dp
        )
    }
    
    components {
        copy(
            button = A2UITheme.ButtonStyles(
                minHeight = 56.dp,
                horizontalPadding = 24.dp,
                verticalPadding = 12.dp,
                cornerRadius = 12.dp
            ),
            card = A2UITheme.CardStyles(
                elevation = 8.dp,
                padding = 20.dp,
                cornerRadius = 16.dp
            )
        )
    }
}
```

## 📱 Complete Example

```kotlin
@Composable
fun MyA2UIApp() {
    // Create custom components
    val customRegistry = remember {
        ComponentRegistry().apply {
            // Override standard components
            register(A2UINodeType.BUTTON.name, ::GradientButton)
            register(A2UINodeType.CARD.name, ::NeumorphicCard)
            register(A2UINodeType.TEXT.name, ::AnimatedText)
            
            // Add new component types
            register("CHART", ::ChartComponent)
            register("AVATAR", ::AvatarComponent)
            register("RATING", ::RatingComponent)
        }
    }
    
    // Create custom theme
    val customTheme = remember {
        buildA2UITheme {
            colors {
                copy(
                    primary = Color(0xFF6C63FF),
                    secondary = Color(0xFFFF6B6B)
                )
            }
        }
    }
    
    // Provide custom configuration
    A2UIProvider(
        componentRegistry = customRegistry,
        theme = customTheme
    ) {
        A2UIExtendedRenderer(
            document = document,
            onAction = { event ->
                when (event.handler) {
                    "handleClick" -> {
                        println("Button clicked: ${event.nodeId}")
                    }
                    "handleSubmit" -> {
                        val data = event.payload as? JsonObject
                        processFormSubmit(data)
                    }
                    else -> {
                        println("Unknown action: ${event.handler}")
                    }
                }
            }
        )
    }
}
```

## 🔌 Adding New Component Types

You can add completely new component types not in the original A2UI spec:

```kotlin
// Define new component type
const val CHART_TYPE = "CHART"
const val AVATAR_TYPE = "AVATAR"
const val RATING_TYPE = "RATING"

// Register custom components
registry.apply {
    register(CHART_TYPE) { node, _, modifier ->
        ChartComponent(
            data = node.props?.customData,
            modifier = modifier
        )
    }
    
    register(AVATAR_TYPE) { node, _, modifier ->
        AvatarComponent(
            imageUrl = node.props?.src,
            name = node.props?.text,
            modifier = modifier
        )
    }
    
    register(RATING_TYPE) { node, onAction, modifier ->
        RatingComponent(
            value = node.props?.progress ?: 0f,
            onValueChange = { newValue ->
                node.actions?.find { it.event == A2UIEventType.CHANGE }?.let {
                    onAction(A2UIActionEvent(
                        node.id, 
                        it.handler, 
                        JsonPrimitive(newValue)
                    ))
                }
            },
            modifier = modifier
        )
    }
}
```

## 📚 Using the Extended Features

1. **Import the extended packages:**
```kotlin
import com.a2ui.core.provider.A2UIProvider
import com.a2ui.core.registry.ComponentRegistry
import com.a2ui.core.render.A2UIExtendedRenderer
import com.a2ui.core.theme.A2UITheme
import com.a2ui.core.theme.buildA2UITheme
```

2. **Set up your custom configuration:**
```kotlin
val config = A2UIConfig(
    componentRegistry = myRegistry,
    theme = myTheme,
    debug = true // Enable debug logging
)
```

3. **Wrap your app with the provider:**
```kotlin
A2UIProvider(config) {
    // Your app content
}
```

## 🧪 Testing Custom Components

```kotlin
@Test
fun testCustomButton() {
    val registry = ComponentRegistry().apply {
        register(A2UINodeType.BUTTON.name, ::TestButton)
    }
    
    composeTestRule.setContent {
        A2UIProvider(componentRegistry = registry) {
            A2UIExtendedRenderer(
                document = testDocument,
                onAction = mockActionHandler
            )
        }
    }
    
    composeTestRule
        .onNodeWithText("Test Button")
        .assertExists()
        .performClick()
    
    verify { mockActionHandler(any()) }
}
```

## 📄 File Locations

The extended A2UI features are organized in these files:

```
shared/src/commonMain/kotlin/com/a2ui/
├── core/
│   ├── registry/
│   │   └── ComponentRegistry.kt        # Component override system
│   ├── provider/
│   │   └── A2UIProvider.kt            # Provider with CompositionLocal
│   ├── theme/
│   │   └── A2UITheme.kt               # Theme system
│   └── render/
│       └── A2UIExtendedRenderer.kt    # Extended renderer with overrides
└── examples/
    └── CustomComponentExample.kt       # Complete usage examples
```

## 🚢 Easy Access

The entire codebase is available at `/Users/mini/Desktop/a2ui-mobile/`

To quickly copy the project:
```bash
# Copy entire project
cp -r /Users/mini/Desktop/a2ui-mobile ~/my-project

# Or create a zip
cd /Users/mini/Desktop
zip -r a2ui-mobile-extended.zip a2ui-mobile/
```

## 🤝 Contributing

The component override system makes it easy to contribute new components:

1. Create your custom component
2. Test it with the registry system
3. Share as a reusable registry configuration

## 📚 Real-World Examples

See [README_EXAMPLES.md](README_EXAMPLES.md) for comprehensive examples showing:
- How to handle specific A2UI responses from your agent
- Form handling with validation
- Dashboard with animated cards
- Chat interfaces
- Settings screens
- E-commerce product lists
- Multi-step wizards
- Complete configuration patterns

## 📚 Resources

- [Original A2UI Spec](https://github.com/google/A2UI)
- [React Implementation PR](https://github.com/google/A2UI/pull/542)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

## License

Apache 2.0