package com.a2ui.core.resolve

import com.a2ui.core.model.A2UIAction
import com.a2ui.core.model.A2UIComponent
import kotlinx.serialization.json.*

/**
 * Extension functions to extract typed properties from A2UIComponent.properties.
 */

// --- Text ---

fun A2UIComponent.text(r: DynamicResolver): String? {
    return r.resolveString(properties["text"])
}

fun A2UIComponent.variant(): String? {
    return properties["variant"]?.jsonPrimitive?.contentOrNull
}

// --- Layout (Row/Column) ---

fun A2UIComponent.justify(): String? {
    return properties["justify"]?.jsonPrimitive?.contentOrNull
}

fun A2UIComponent.align(): String? {
    return properties["align"]?.jsonPrimitive?.contentOrNull
}

// --- Button ---

fun A2UIComponent.childId(): String? {
    return properties["child"]?.jsonPrimitive?.contentOrNull
}

fun A2UIComponent.action(): A2UIAction? {
    val actionObj = properties["action"]?.jsonObject ?: return null
    return A2UIAction(
        name = actionObj["name"]?.jsonPrimitive?.contentOrNull,
        context = actionObj["context"]?.jsonObject,
        functionCall = actionObj["call"]?.jsonPrimitive?.contentOrNull,
        args = actionObj["args"]?.jsonObject
    )
}

// --- TextField / CheckBox / Slider / ChoicePicker / DateTimeInput ---

fun A2UIComponent.label(r: DynamicResolver): String? {
    return r.resolveString(properties["label"])
}

fun A2UIComponent.value(r: DynamicResolver): JsonElement? {
    return r.resolve(properties["value"])
}

fun A2UIComponent.isEnabled(r: DynamicResolver): Boolean {
    return r.resolveBoolean(properties["enabled"]) ?: true
}

fun A2UIComponent.placeholder(r: DynamicResolver): String? {
    return r.resolveString(properties["placeholder"])
}

// --- Image / Video / AudioPlayer ---

fun A2UIComponent.url(r: DynamicResolver): String? {
    return r.resolveString(properties["url"])
}

fun A2UIComponent.fit(): String? {
    return properties["fit"]?.jsonPrimitive?.contentOrNull
}

fun A2UIComponent.altText(r: DynamicResolver): String? {
    return r.resolveString(properties["altText"])
}

fun A2UIComponent.description(r: DynamicResolver): String? {
    return r.resolveString(properties["description"])
}

// --- Slider ---

fun A2UIComponent.min(r: DynamicResolver): Float {
    return r.resolveFloat(properties["min"]) ?: 0f
}

fun A2UIComponent.max(r: DynamicResolver): Float {
    return r.resolveFloat(properties["max"]) ?: 1f
}

// --- ChoicePicker ---

data class PickerOption(val value: String, val label: String)

fun A2UIComponent.options(r: DynamicResolver): List<PickerOption> {
    val optionsArray = properties["options"]?.jsonArray ?: return emptyList()
    return optionsArray.mapNotNull { element ->
        val obj = element as? JsonObject ?: return@mapNotNull null
        val value = r.resolveString(obj["value"]) ?: return@mapNotNull null
        val label = r.resolveString(obj["label"]) ?: value
        PickerOption(value, label)
    }
}

fun A2UIComponent.selectionMode(): String? {
    return properties["selectionMode"]?.jsonPrimitive?.contentOrNull
}

// --- Tabs ---

data class TabDef(val title: String, val childId: String)

fun A2UIComponent.tabs(): List<TabDef> {
    val tabsArray = properties["tabs"]?.jsonArray ?: return emptyList()
    return tabsArray.mapNotNull { element ->
        val obj = element as? JsonObject ?: return@mapNotNull null
        val title = obj["title"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        val child = obj["child"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
        TabDef(title, child)
    }
}

// --- Modal ---

fun A2UIComponent.triggerId(): String? {
    return properties["trigger"]?.jsonPrimitive?.contentOrNull
}

fun A2UIComponent.contentId(): String? {
    return properties["content"]?.jsonPrimitive?.contentOrNull
}

// --- DateTimeInput ---

fun A2UIComponent.enableDate(): Boolean {
    return properties["enableDate"]?.jsonPrimitive?.booleanOrNull ?: true
}

fun A2UIComponent.enableTime(): Boolean {
    return properties["enableTime"]?.jsonPrimitive?.booleanOrNull ?: false
}

// --- Divider ---

fun A2UIComponent.axis(): String? {
    return properties["axis"]?.jsonPrimitive?.contentOrNull
}

// --- List ---

fun A2UIComponent.direction(): String? {
    return properties["direction"]?.jsonPrimitive?.contentOrNull
}

// --- Common ---

fun A2UIComponent.resolvedWeight(r: DynamicResolver): Float? {
    return r.resolveFloat(weight)
}
