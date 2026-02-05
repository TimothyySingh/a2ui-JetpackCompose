package com.a2ui.core.resolve

import com.a2ui.core.model.A2UIComponent
import kotlinx.serialization.json.*

/**
 * Reference to a child component, optionally with scoped data for templates.
 */
data class ChildReference(
    val componentId: String,
    val scopedData: JsonObject? = null
)

/**
 * Resolves the children field of an A2UIComponent to a list of component IDs to render.
 *
 * 1. Static list: JsonArray of string primitives -> each is a component ID
 * 2. Template: JsonObject with componentId + path -> resolve path to get data array,
 *    render template component for each element with scoped data
 */
class ChildListResolver(private val resolver: DynamicResolver) {

    fun resolve(
        children: JsonElement?,
        components: Map<String, A2UIComponent>
    ): List<ChildReference> {
        if (children == null) return emptyList()

        return when (children) {
            is JsonArray -> resolveStaticList(children)
            is JsonObject -> resolveTemplate(children)
            is JsonPrimitive -> {
                // Single child ID as a string
                val id = children.contentOrNull ?: return emptyList()
                listOf(ChildReference(id))
            }
            else -> emptyList()
        }
    }

    private fun resolveStaticList(array: JsonArray): List<ChildReference> {
        return array.mapNotNull { element ->
            when (element) {
                is JsonPrimitive -> element.contentOrNull?.let { ChildReference(it) }
                else -> null
            }
        }
    }

    private fun resolveTemplate(template: JsonObject): List<ChildReference> {
        val componentId = template["componentId"]?.jsonPrimitive?.contentOrNull ?: return emptyList()
        val path = template["path"]?.jsonPrimitive?.contentOrNull ?: return emptyList()

        val dataArray = resolver.resolveJsonPointer(path)
        if (dataArray !is JsonArray) return emptyList()

        return dataArray.mapIndexedNotNull { index, item ->
            val scopedData = when (item) {
                is JsonObject -> item
                else -> JsonObject(mapOf("value" to item, "index" to JsonPrimitive(index)))
            }
            ChildReference(componentId, scopedData)
        }
    }
}
