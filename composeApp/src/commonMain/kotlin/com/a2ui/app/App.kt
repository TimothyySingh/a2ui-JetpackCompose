package com.a2ui.app

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a2ui.core.model.*
import com.a2ui.core.render.A2UIRenderer
import kotlinx.serialization.json.*

@Composable
fun App() {
    val surface = remember { createDemoSurface() }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            A2UIRenderer(
                surface = surface,
                onAction = { action ->
                    println("Action: component=${action.componentId}, name=${action.actionName}, value=${action.value}")
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Build a demo A2UISurface with flat component map showcasing:
 * - Data binding, variant properties, validation, template ChildList,
 *   Tabs, Modal, ChoicePicker, DateTimeInput, all actions via onAction.
 */
private fun createDemoSurface(): A2UISurface {
    val components = mutableMapOf<String, A2UIComponent>()

    fun add(comp: A2UIComponent) { components[comp.id] = comp }

    // Root scaffold
    add(A2UIComponent(
        id = "root",
        component = "Scaffold",
        children = JsonArray(listOf("topbar", "body").map { JsonPrimitive(it) })
    ))

    add(A2UIComponent(
        id = "topbar",
        component = "TopBar",
        properties = buildJsonObject { put("text", "A2UI v0.9 Demo") }
    ))

    add(A2UIComponent(
        id = "body",
        component = "Scrollable",
        children = JsonArray(listOf(JsonPrimitive("main_column")))
    ))

    add(A2UIComponent(
        id = "main_column",
        component = "Column",
        properties = buildJsonObject { put("justify", "start") },
        children = JsonArray(listOf(
            "welcome_header", "welcome_desc", "spacer1",
            "info_card", "spacer2",
            "tabs_section", "spacer3",
            "modal_section", "spacer4",
            "choice_section", "spacer5",
            "date_section", "spacer6",
            "name_field", "spacer7",
            "button_row", "spacer8",
            "data_list", "spacer9",
            "divider1", "footer"
        ).map { JsonPrimitive(it) })
    ))

    // --- Data-bound text with variant ---
    add(A2UIComponent(
        id = "welcome_header",
        component = "Text",
        properties = buildJsonObject {
            put("text", "/greeting")
            put("variant", "h2")
        }
    ))

    add(A2UIComponent(
        id = "welcome_desc",
        component = "Text",
        properties = buildJsonObject {
            put("text", "A2UI v0.9 rendering with flat component map, data binding, and spec components.")
        }
    ))

    add(A2UIComponent(id = "spacer1", component = "Spacer", properties = buildJsonObject { put("height", 16) }))
    add(A2UIComponent(id = "spacer2", component = "Spacer", properties = buildJsonObject { put("height", 16) }))
    add(A2UIComponent(id = "spacer3", component = "Spacer", properties = buildJsonObject { put("height", 16) }))
    add(A2UIComponent(id = "spacer4", component = "Spacer", properties = buildJsonObject { put("height", 16) }))
    add(A2UIComponent(id = "spacer5", component = "Spacer", properties = buildJsonObject { put("height", 16) }))
    add(A2UIComponent(id = "spacer6", component = "Spacer", properties = buildJsonObject { put("height", 16) }))
    add(A2UIComponent(id = "spacer7", component = "Spacer", properties = buildJsonObject { put("height", 12) }))
    add(A2UIComponent(id = "spacer8", component = "Spacer", properties = buildJsonObject { put("height", 16) }))
    add(A2UIComponent(id = "spacer9", component = "Spacer", properties = buildJsonObject { put("height", 16) }))

    // --- Info Card ---
    add(A2UIComponent(
        id = "info_card",
        component = "Card",
        children = JsonArray(listOf(JsonPrimitive("card_content")))
    ))

    add(A2UIComponent(
        id = "card_content",
        component = "Column",
        children = JsonArray(listOf("card_title", "card_body").map { JsonPrimitive(it) })
    ))

    add(A2UIComponent(
        id = "card_title",
        component = "Text",
        properties = buildJsonObject {
            put("text", "Getting Started")
            put("variant", "h4")
        }
    ))

    add(A2UIComponent(
        id = "card_body",
        component = "Text",
        properties = buildJsonObject {
            put("text", "This UI is defined as an A2UISurface with a flat component map. Components reference each other by ID.")
        }
    ))

    // --- Tabs ---
    add(A2UIComponent(
        id = "tabs_section",
        component = "Tabs",
        properties = buildJsonObject {
            putJsonArray("tabs") {
                addJsonObject { put("title", "Overview"); put("child", "tab1_content") }
                addJsonObject { put("title", "Details"); put("child", "tab2_content") }
                addJsonObject { put("title", "Settings"); put("child", "tab3_content") }
            }
        }
    ))

    add(A2UIComponent(
        id = "tab1_content",
        component = "Text",
        properties = buildJsonObject { put("text", "Overview: A2UI renders agent-driven UIs natively on mobile.") }
    ))

    add(A2UIComponent(
        id = "tab2_content",
        component = "Text",
        properties = buildJsonObject { put("text", "Details: Supports 18 spec components with data binding and validation.") }
    ))

    add(A2UIComponent(
        id = "tab3_content",
        component = "Text",
        properties = buildJsonObject { put("text", "Settings: Configure themes, custom components, and connection parameters.") }
    ))

    // --- Modal ---
    add(A2UIComponent(
        id = "modal_section",
        component = "Modal",
        properties = buildJsonObject {
            put("trigger", "modal_trigger_btn")
            put("content", "modal_body")
        }
    ))

    add(A2UIComponent(
        id = "modal_trigger_btn",
        component = "Button",
        properties = buildJsonObject {
            put("text", "Open Modal")
            put("variant", "primary")
        }
    ))

    add(A2UIComponent(
        id = "modal_body",
        component = "Text",
        properties = buildJsonObject {
            put("text", "This is modal content rendered inside an AlertDialog.")
        }
    ))

    // --- ChoicePicker ---
    add(A2UIComponent(
        id = "choice_section",
        component = "ChoicePicker",
        properties = buildJsonObject {
            put("label", "Select a framework")
            put("selectionMode", "mutuallyExclusive")
            putJsonArray("options") {
                addJsonObject { put("value", "compose"); put("label", "Compose Multiplatform") }
                addJsonObject { put("value", "swiftui"); put("label", "SwiftUI") }
                addJsonObject { put("value", "flutter"); put("label", "Flutter") }
            }
            putJsonObject("action") { put("name", "frameworkSelected") }
        }
    ))

    // --- DateTimeInput ---
    add(A2UIComponent(
        id = "date_section",
        component = "DateTimeInput",
        properties = buildJsonObject {
            put("label", "Select a date")
            put("enableDate", true)
            put("enableTime", false)
            putJsonObject("action") { put("name", "dateSelected") }
        }
    ))

    // --- TextField with validation ---
    add(A2UIComponent(
        id = "name_field",
        component = "TextField",
        properties = buildJsonObject {
            put("label", "Your name")
            put("placeholder", "Enter your name")
            putJsonObject("action") { put("name", "nameChanged") }
        },
        checks = listOf(
            A2UICheck(
                condition = buildJsonObject {
                    put("call", "required")
                    putJsonObject("args") { put("value", "/userName") }
                },
                message = JsonPrimitive("Name is required")
            ),
            A2UICheck(
                condition = buildJsonObject {
                    put("call", "length")
                    putJsonObject("args") {
                        put("value", "/userName")
                        put("min", 2)
                        put("max", 50)
                    }
                },
                message = JsonPrimitive("Name must be 2-50 characters")
            )
        )
    ))

    // --- Button row ---
    add(A2UIComponent(
        id = "button_row",
        component = "Row",
        properties = buildJsonObject { put("justify", "spaceBetween") },
        children = JsonArray(listOf("greet_btn", "reset_btn").map { JsonPrimitive(it) })
    ))

    add(A2UIComponent(
        id = "greet_btn",
        component = "Button",
        properties = buildJsonObject {
            put("text", "Say Hello")
            putJsonObject("action") { put("name", "greet") }
        }
    ))

    add(A2UIComponent(
        id = "reset_btn",
        component = "Button",
        properties = buildJsonObject {
            put("text", "Reset")
            put("variant", "borderless")
            putJsonObject("action") { put("name", "reset") }
        }
    ))

    // --- Template-based List ---
    add(A2UIComponent(
        id = "data_list",
        component = "List",
        properties = buildJsonObject { put("direction", "vertical") },
        children = buildJsonObject {
            put("componentId", "list_item_tmpl")
            put("path", "/items")
        }
    ))

    add(A2UIComponent(
        id = "list_item_tmpl",
        component = "Text",
        properties = buildJsonObject {
            put("text", "/name")
        }
    ))

    // --- Divider + Footer ---
    add(A2UIComponent(id = "divider1", component = "Divider"))

    add(A2UIComponent(
        id = "footer",
        component = "Text",
        properties = buildJsonObject {
            put("text", "Built with A2UI v0.9")
            put("variant", "caption")
        }
    ))

    return A2UISurface(
        root = "root",
        components = components,
        data = buildJsonObject {
            put("greeting", "Welcome to A2UI v0.9")
            put("userName", "")
            putJsonArray("items") {
                addJsonObject { put("name", "Compose Multiplatform") }
                addJsonObject { put("name", "Data Binding") }
                addJsonObject { put("name", "Validation Engine") }
                addJsonObject { put("name", "Template Lists") }
            }
        },
        theme = A2UISurfaceTheme(
            primaryColor = "#1976D2",
            agentDisplayName = "A2UI Demo Agent"
        )
    )
}

@Composable
private fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(message)
        }
    }
}

@Composable
private fun DisconnectedScreen(onConnect: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "A2UI",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Not connected to agent",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onConnect) {
                Text("Connect")
            }
        }
    }
}

@Composable
private fun ConnectionErrorScreen(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Connection Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Waiting for UI...",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "The agent will push a UI when ready",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
