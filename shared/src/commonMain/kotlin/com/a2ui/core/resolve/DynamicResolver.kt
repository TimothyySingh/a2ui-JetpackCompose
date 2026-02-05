package com.a2ui.core.resolve

import kotlinx.serialization.json.*

/**
 * Context for resolving dynamic values against the data model.
 */
class ResolverContext(val data: JsonObject, val scopedData: JsonObject? = null)

/**
 * Resolves DynamicValue JsonElements against the surface data model.
 *
 * Resolution rules:
 * 1. null -> null
 * 2. JsonPrimitive literal (string not starting with /, number, boolean) -> return as-is
 * 3. JsonPrimitive string starting with / -> JSON Pointer lookup in context.data
 * 4. JsonObject with "call" key -> delegate to FunctionEvaluator
 * 5. JsonArray -> resolve each element recursively
 */
class DynamicResolver(private val context: ResolverContext) {

    private val functionEvaluator by lazy { FunctionEvaluator(this) }

    fun resolve(element: JsonElement?): JsonElement? {
        if (element == null) return null

        return when (element) {
            is JsonNull -> null
            is JsonPrimitive -> resolvePrimitive(element)
            is JsonObject -> resolveObject(element)
            is JsonArray -> JsonArray(element.mapNotNull { resolve(it) })
        }
    }

    private fun resolvePrimitive(primitive: JsonPrimitive): JsonElement? {
        if (!primitive.isString) return primitive
        val str = primitive.content
        return if (str.startsWith("/")) {
            resolveJsonPointer(str)
        } else {
            primitive
        }
    }

    private fun resolveObject(obj: JsonObject): JsonElement? {
        if (obj.containsKey("call")) {
            return functionEvaluator.evaluate(obj)
        }
        return obj
    }

    /**
     * Resolve a JSON Pointer (RFC 6901) against the data model.
     * First checks scopedData, then falls back to root data.
     */
    fun resolveJsonPointer(pointer: String): JsonElement? {
        if (pointer.isEmpty() || pointer == "/") return context.data

        val segments = pointer.removePrefix("/").split("/").map { segment ->
            segment.replace("~1", "/").replace("~0", "~")
        }

        // Try scoped data first
        context.scopedData?.let { scoped ->
            val result = walkPath(scoped, segments)
            if (result != null) return result
        }

        return walkPath(context.data, segments)
    }

    private fun walkPath(root: JsonElement, segments: List<String>): JsonElement? {
        var current: JsonElement = root
        for (segment in segments) {
            current = when (current) {
                is JsonObject -> current[segment] ?: return null
                is JsonArray -> {
                    val index = segment.toIntOrNull() ?: return null
                    current.getOrNull(index) ?: return null
                }
                else -> return null
            }
        }
        return current
    }

    fun resolveString(element: JsonElement?): String? {
        val resolved = resolve(element) ?: return null
        return when (resolved) {
            is JsonPrimitive -> resolved.contentOrNull
            else -> resolved.toString()
        }
    }

    fun resolveBoolean(element: JsonElement?): Boolean? {
        val resolved = resolve(element) ?: return null
        return when (resolved) {
            is JsonPrimitive -> resolved.booleanOrNull
            else -> null
        }
    }

    fun resolveNumber(element: JsonElement?): Number? {
        val resolved = resolve(element) ?: return null
        return when (resolved) {
            is JsonPrimitive -> resolved.doubleOrNull ?: resolved.longOrNull
            else -> null
        }
    }

    fun resolveFloat(element: JsonElement?): Float? {
        return resolveNumber(element)?.toFloat()
    }

    fun resolveStringList(element: JsonElement?): List<String>? {
        val resolved = resolve(element) ?: return null
        return when (resolved) {
            is JsonArray -> resolved.mapNotNull { item ->
                when (item) {
                    is JsonPrimitive -> item.contentOrNull
                    else -> item.toString()
                }
            }
            else -> null
        }
    }

    /**
     * Create a child resolver with scoped data for template rendering.
     */
    fun withScopedData(scopedData: JsonObject): DynamicResolver {
        return DynamicResolver(ResolverContext(context.data, scopedData))
    }
}
