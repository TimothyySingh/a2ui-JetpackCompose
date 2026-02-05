package com.a2ui.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * A2UI v0.9 Schema - Agent-to-UI specification
 *
 * Flat component map with IDs, PascalCase types, DynamicValue data binding.
 */

@Serializable
data class A2UISurface(
    val root: String,
    val components: Map<String, A2UIComponent>,
    val data: JsonObject = JsonObject(emptyMap()),
    val theme: A2UISurfaceTheme? = null
)

@Serializable
data class A2UISurfaceTheme(
    val primaryColor: String? = null,
    val iconUrl: String? = null,
    val agentDisplayName: String? = null
)

@Serializable
data class A2UIComponent(
    val id: String,
    val component: String,
    val properties: JsonObject = JsonObject(emptyMap()),
    val children: JsonElement? = null,
    val checks: List<A2UICheck>? = null,
    val accessibility: A2UIAccessibility? = null,
    val weight: JsonElement? = null
)

@Serializable
data class A2UIAccessibility(
    val label: String? = null,
    val description: String? = null
)

@Serializable
data class A2UICheck(
    val condition: JsonElement,
    val message: JsonElement
)

@Serializable
data class A2UIAction(
    val name: String? = null,
    val context: JsonObject? = null,
    @SerialName("call") val functionCall: String? = null,
    val args: JsonObject? = null
)

data class A2UIActionEvent(
    val componentId: String,
    val actionName: String,
    val context: JsonObject? = null,
    val value: JsonElement? = null
)

typealias A2UIDocument = A2UISurface
