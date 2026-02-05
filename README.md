# A2UI for Compose Multiplatform

**Render agent-driven UIs natively on Android and iOS from a single Kotlin codebase.**

A2UI (Agent-to-UI) is a declarative UI protocol that lets AI agents send structured UI definitions to mobile apps. Instead of agents returning plain text or markdown, they send rich, interactive surfaces — complete with buttons, forms, cards, tabs, modals, and more — that your app renders natively using Compose Multiplatform.

This library implements the [A2UI v0.9 specification](https://github.com/google/A2UI) and gives mobile teams a drop-in renderer, a component extension system, and full theming — so agent responses look and feel like they belong in your app.

---

## Why A2UI?

AI agents are getting powerful, but their output is still trapped in chat bubbles. A2UI changes that.

**The problem:** When an agent wants to show a form, a data table, a set of options, or a multi-step workflow, it's forced to describe it in text. The user reads, interprets, and manually acts. That's slow, error-prone, and a terrible experience on mobile.

**The A2UI approach:** The agent sends a lightweight JSON surface describing *what* to show. Your app decides *how* to render it — natively, with your design system, your animations, your interaction patterns. The agent stays focused on logic and decisions. Your app stays focused on presentation.

```
Agent                          Your App
  │                               │
  │── A2UISurface (JSON) ────────>│
  │                               │── Renders native Compose UI
  │                               │── User interacts (taps, types, selects)
  │<── A2UIActionEvent ──────────│
  │                               │
  │── Updated Surface ──────────>│
  │                               │── Re-renders with new state
```

**Why this matters:**

- **Native performance.** Every component is a real Compose widget — not a WebView, not a canvas hack. Scrolling is smooth. Accessibility works. Animations feel right.
- **Agent-agnostic.** Any backend agent can produce A2UI surfaces. Swap agents without touching your mobile code.
- **Design system alignment.** Override any component with your own implementation. Agent UIs match your app's look and feel exactly.
- **Cross-platform from one codebase.** Write once in Kotlin, run on Android and iOS via Compose Multiplatform. No separate Swift or platform-specific rendering.
- **Real-time updates.** Surfaces can be pushed over WebSocket. Agents can update, add, or remove components incrementally without resending the whole UI.

---

## How It Works

### The Surface Model

An A2UI surface is a flat map of components, each identified by a unique ID. One component is the root. Components reference their children by ID.

```json
{
  "root": "main",
  "components": {
    "main": {
      "id": "main",
      "component": "Column",
      "children": ["greeting", "action_btn"]
    },
    "greeting": {
      "id": "greeting",
      "component": "Text",
      "properties": { "text": "Hello from the agent!", "variant": "h2" }
    },
    "action_btn": {
      "id": "action_btn",
      "component": "Button",
      "properties": {
        "text": "Get Started",
        "action": { "name": "onGetStarted" }
      }
    }
  },
  "data": {
    "userName": "Alex"
  }
}
```

The renderer walks this map starting from `root`, resolves data bindings, evaluates validation rules, and produces native Compose UI.

### Data Binding

Properties can reference the surface's data model using JSON Pointers (RFC 6901):

```json
{
  "component": "Text",
  "properties": { "text": "/userName" }
}
```

When the data model contains `{"userName": "Alex"}`, the text renders as "Alex". The agent updates the data model, and the UI updates reactively.

### Actions

When a user taps a button, submits a form, or changes a value, an `A2UIActionEvent` fires with the component ID, action name, and any associated value or context. Your app handles these however you like — send them to the agent over WebSocket, process them locally, or both.

---

## Quick Start

### 1. Render a surface

```kotlin
@Composable
fun AgentScreen() {
    val surface = remember { /* parse from JSON or build programmatically */ }

    MaterialTheme {
        A2UIRenderer(
            surface = surface,
            onAction = { event ->
                println("${event.actionName} from ${event.componentId}")
                // Send to your agent backend
            }
        )
    }
}
```

### 2. Build a surface in code

```kotlin
val surface = A2UISurface(
    root = "root",
    components = mapOf(
        "root" to A2UIComponent(
            id = "root",
            component = "Column",
            children = JsonArray(listOf(JsonPrimitive("title"), JsonPrimitive("card")))
        ),
        "title" to A2UIComponent(
            id = "title",
            component = "Text",
            properties = buildJsonObject {
                put("text", "Dashboard")
                put("variant", "h2")
            }
        ),
        "card" to A2UIComponent(
            id = "card",
            component = "Card",
            children = JsonArray(listOf(JsonPrimitive("card_body")))
        ),
        "card_body" to A2UIComponent(
            id = "card_body",
            component = "Text",
            properties = buildJsonObject {
                put("text", "Your agent-powered content here.")
            }
        )
    )
)
```

### 3. Parse from JSON

```kotlin
val json = """{"root":"main","components":{...}}"""
val result = A2UIParser.parseDocument(json)

result.onSuccess { surface ->
    // Render it
}
result.onFailure { error ->
    // Handle parse error
}
```

### 4. Connect to a live agent via WebSocket

```kotlin
val stateManager = A2UIStateManager()
val connection = A2UIConnection(
    stateManager = stateManager,
    config = A2UIConnectionConfig(
        host = "your-agent-gateway.com",
        port = 443,
        path = "/a2ui"
    )
)

// In a coroutine scope
connection.connect(scope)

// Observe surfaces reactively
stateManager.surface.collect { surface ->
    // Render the latest surface
}
```

The connection handles four message types automatically:
- **`surface`** — Load a complete new surface
- **`commands`** — Apply incremental updates (add, remove, update components or data)
- **`loading`** — Show/hide loading state
- **`clear`** — Reset the UI

---

## Built-In Components

A2UI ships with **29 components** out of the box, covering the full v0.9 spec plus useful extensions.

### Content

| Component | Description |
|-----------|-------------|
| `Text` | Rich text with variants: `h1`–`h5`, `body`, `caption`, `overline` |
| `Image` | Image display with variants: `icon`, `avatar`, `smallFeature`, `mediumFeature`, `largeFeature`, `header` |
| `Icon` | Material icon by name (28+ mapped icons: `home`, `search`, `settings`, `favorite`, `star`, etc.) |
| `Video` | Video placeholder with URL display |
| `AudioPlayer` | Audio player with play/pause controls |

### Layout

| Component | Description |
|-----------|-------------|
| `Row` | Horizontal layout with `justify` and `align` support |
| `Column` | Vertical layout with `justify` and `align` support |
| `Card` | Material card with elevation and corner radius |
| `List` | Lazy list (vertical/horizontal) with template-based data binding |
| `Tabs` | Tab navigation with swipeable content |
| `Modal` | AlertDialog triggered by a child component |
| `Divider` | Horizontal or vertical divider |

### Input

| Component | Description |
|-----------|-------------|
| `Button` | Material button with variants: `primary`, `borderless` |
| `TextField` | Text input with variants: `standard`, `longText`, `number`, `obscured` |
| `CheckBox` | Checkbox with label and data binding |
| `ChoicePicker` | Radio group (`mutuallyExclusive`) or checkbox group (`multipleSelection`) |
| `Slider` | Range slider with min/max/step |
| `DateTimeInput` | Date and/or time picker |

### Extensions

`Scaffold`, `Box`, `Scrollable`, `LazyColumn`, `LazyRow`, `Spacer`, `Switch`, `Dropdown`, `Progress`, `Loading`, `TopBar`, `BottomBar`, `Fab`

---

## Custom Components & Design System Override

This is where A2UI gets powerful for product teams. You can override **any** built-in component or register entirely new ones. Agent responses render with *your* design system.

### Override a built-in component

```kotlin
val registry = ComponentRegistry().apply {
    register("Button") { component, surface, resolver, onAction, modifier ->
        // Your custom button implementation
        val action = component.action()
        YourDesignSystemButton(
            label = component.text(resolver) ?: "Button",
            style = YourButtonStyle.Primary,
            onClick = {
                if (action?.name != null) {
                    onAction(A2UIActionEvent(component.id, action.name, action.context))
                }
            },
            modifier = modifier
        )
    }
}
```

### Register a new component type

```kotlin
registry.register("Rating") { component, surface, resolver, onAction, modifier ->
    val value = resolver.resolveFloat(component.properties["value"]) ?: 0f
    val max = resolver.resolveFloat(component.properties["max"]) ?: 5f

    YourStarRating(
        rating = value,
        maxRating = max.toInt(),
        onRatingChanged = { newRating ->
            val action = component.action()
            if (action?.name != null) {
                onAction(A2UIActionEvent(
                    componentId = component.id,
                    actionName = action.name,
                    value = JsonPrimitive(newRating)
                ))
            }
        },
        modifier = modifier
    )
}
```

### Apply your registry and theme

```kotlin
A2UIProvider(
    componentRegistry = registry,
    theme = buildA2UITheme {
        colors {
            copy(
                primary = Color(0xFF6200EE),
                secondary = Color(0xFF03DAC6),
                background = Color(0xFFF5F5F5)
            )
        }
        components {
            copy(
                button = button.copy(cornerRadius = 24.dp, minHeight = 52.dp),
                card = card.copy(elevation = 2.dp, cornerRadius = 16.dp)
            )
        }
    }
) {
    A2UIExtendedRenderer(
        surface = surface,
        onAction = { event -> /* handle */ }
    )
}
```

The `A2UIExtendedRenderer` checks your registry first. If you've registered a custom `"Button"`, every button the agent sends renders with your implementation. Components you haven't overridden fall through to the built-in renderer. You can override one component or all of them.

---

## Validation

Components can carry validation rules via the `checks` array. The validation engine evaluates conditions against the data model and produces error messages.

```json
{
  "component": "TextField",
  "properties": { "label": "Email", "action": { "name": "updateEmail" } },
  "checks": [
    {
      "condition": { "call": "required", "args": { "value": "/email" } },
      "message": "Email is required"
    },
    {
      "condition": { "call": "email", "args": { "value": "/email" } },
      "message": "Enter a valid email address"
    }
  ]
}
```

**14 built-in functions** are available for validation, formatting, and logic:

| Category | Functions |
|----------|-----------|
| Validation | `required`, `regex`, `length`, `numeric`, `email` |
| Formatting | `formatString`, `formatNumber`, `formatCurrency`, `formatDate`, `pluralize` |
| Logic | `and`, `or`, `not` |
| Action | `openUrl` |

---

## Theming

Three built-in themes ship with the library:

```kotlin
A2UITheme.Default      // Material light
A2UITheme.Dark         // Material dark
A2UITheme.HighContrast // Accessibility-focused
```

Build custom themes with the DSL:

```kotlin
val theme = buildA2UITheme {
    colors { copy(primary = Color(0xFF1DB954)) }
    typography {
        copy(
            h1 = h1.copy(fontSize = 36.sp, fontWeight = FontWeight.Black),
            body = body.copy(fontSize = 15.sp, lineHeight = 22.sp)
        )
    }
    spacing { copy(md = 20.dp, lg = 32.dp) }
}
```

Agents can also suggest a primary color via the surface theme:

```json
{
  "theme": { "primaryColor": "#1DB954", "agentDisplayName": "Music Agent" }
}
```

This is applied as a hint — your app's theme takes precedence if configured.

---

## Use Cases

### Customer Support Agent
An agent asks qualifying questions via forms, shows order details in cards, and presents resolution options as choice pickers — all rendered natively. No chat bubble parsing required.

### Healthcare Companion
A health agent sends intake forms with validated fields (date of birth, medication lists, symptom checklists). The app renders accessible, native inputs. Validation happens client-side before submission.

### E-Commerce Assistant
A shopping agent sends product cards with images, comparison tabs, and "Add to Cart" buttons. Each component is a native widget. The experience feels like browsing the app, not talking to a bot.

### Internal Tools
Enterprise agents produce dashboards, approval forms, and data tables for field workers. IT teams override components to match the corporate design system. One agent, consistent UX across Android and iOS.

### Education Platform
A tutoring agent sends interactive quizzes (choice pickers), progress sliders, and multimedia content (video/audio players). Students interact with native controls instead of typing answers in chat.

### Financial Services
A banking agent surfaces account summaries in cards, transaction lists with template-based data binding, and transfer forms with numeric validation. Every interaction is auditable via action events.

---

## Architecture

```
┌─────────────────────────────────────────────┐
│                  Your App                    │
│                                              │
│   ┌──────────────────────────────────────┐   │
│   │          A2UIProvider                │   │
│   │   ┌──────────┐  ┌───────────────┐   │   │
│   │   │  Theme   │  │   Registry    │   │   │
│   │   └──────────┘  └───────────────┘   │   │
│   │                                      │   │
│   │   ┌──────────────────────────────┐   │   │
│   │   │    A2UIExtendedRenderer      │   │   │
│   │   │                              │   │   │
│   │   │  ┌─────────────────────┐     │   │   │
│   │   │  │ ComponentRegistry   │────>│Custom│ │
│   │   │  │ (your overrides)    │     │Render│ │
│   │   │  └─────────────────────┘     │      │ │
│   │   │           │ fallback         │      │ │
│   │   │  ┌─────────────────────┐     │      │ │
│   │   │  │  A2UIRenderer       │────>│Built │ │
│   │   │  │  (29 components)    │     │-in   │ │
│   │   │  └─────────────────────┘     │      │ │
│   │   └──────────────────────────────┘   │   │
│   └──────────────────────────────────────┘   │
│                                              │
│   ┌──────────────┐    ┌──────────────────┐   │
│   │ A2UIConnection│<──>│ A2UIStateManager │   │
│   │  (WebSocket)  │    │  (Surface Flow)  │   │
│   └──────────────┘    └──────────────────┘   │
└─────────────────────────────────────────────┘
```

### Key modules

| Module | Purpose |
|--------|---------|
| `core/model` | Data model — `A2UISurface`, `A2UIComponent`, `A2UIAction`, `A2UIActionEvent` |
| `core/render` | Built-in renderer (29 components) and extended renderer with registry lookup |
| `core/registry` | `ComponentRegistry` for custom component registration |
| `core/resolve` | `DynamicResolver` for data binding, `FunctionEvaluator` for 14 spec functions, `ValidationEngine`, `ChildListResolver` |
| `core/provider` | `A2UIProvider` composable and `A2UIConfig` |
| `core/theme` | `A2UITheme` with builder DSL and surface theme integration |
| `core/state` | `A2UIStateManager` — reactive state with incremental command support |
| `core/connection` | WebSocket connection to agent gateway |
| `core/parser` | JSON/JSONL parser for surfaces and commands |

---

## Compose Multiplatform: One Codebase, Two Platforms

This project is built with [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) (1.7.1) and [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) (2.0.21). The entire A2UI library — renderer, registry, resolver, state, theming — lives in `commonMain` and compiles for both Android and iOS.

- **Android:** Standard Compose with Material3. Runs on API 24+.
- **iOS:** Compose for iOS via Kotlin/Native. Static framework integrates into any Xcode project.

Your custom components, theme overrides, and action handlers are written once and work on both platforms. Platform-specific code (networking uses Ktor with OkHttp on Android and Darwin on iOS) is handled by the existing infrastructure.

---

## Building

```bash
# Set JAVA_HOME to Android Studio's bundled JDK
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Build Android app
./gradlew :composeApp:assembleDebug

# Run tests (143 tests)
./gradlew :shared:testDebugUnitTest

# Build iOS framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Requirements

- Android Studio (with bundled JDK 21)
- Android SDK (API 35)
- Xcode 15+ and command line tools (for iOS)

---

## Project Structure

```
a2ui-JetpackCompose/
├── composeApp/                  # Application module (Android + iOS entry points)
│   └── src/commonMain/kotlin/
│       └── com/a2ui/app/App.kt  # Demo app with full surface example
├── shared/                      # A2UI library (Kotlin Multiplatform)
│   └── src/
│       ├── commonMain/kotlin/com/a2ui/
│       │   ├── core/
│       │   │   ├── model/       # A2UISurface, A2UIComponent, A2UIAction
│       │   │   ├── render/      # A2UIRenderer, A2UIExtendedRenderer
│       │   │   ├── registry/    # ComponentRegistry
│       │   │   ├── resolve/     # DynamicResolver, FunctionEvaluator, ValidationEngine
│       │   │   ├── provider/    # A2UIProvider, A2UIConfig
│       │   │   ├── theme/       # A2UITheme, builder DSL
│       │   │   ├── state/       # A2UIStateManager
│       │   │   ├── connection/  # WebSocket connection
│       │   │   └── parser/      # JSON/JSONL parser
│       │   └── examples/        # Custom component examples
│       └── commonTest/          # 143 unit tests
├── iosApp/                      # Xcode project for iOS
└── gradle/libs.versions.toml   # Version catalog
```

---

## License

See [LICENSE](LICENSE) for details.
