package com.a2ui.core.parser

import com.a2ui.core.model.A2UIComponent
import com.a2ui.core.model.A2UISurface
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * A2UI v0.9 Parser - Parses JSON into A2UISurface documents.
 */
object A2UIParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Parse a full A2UISurface document from JSON.
     */
    fun parseDocument(jsonString: String): Result<A2UISurface> {
        return runCatching {
            json.decodeFromString<A2UISurface>(jsonString)
        }
    }

    /**
     * Parse JSONL format (newline-delimited JSON).
     * Each line represents a command for incremental updates.
     */
    fun parseJsonl(jsonlString: String): List<A2UICommand> {
        return jsonlString
            .lines()
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                runCatching {
                    json.decodeFromString<A2UICommand>(line)
                }.getOrNull()
            }
    }
}

/**
 * A2UI Command - For incremental updates via JSONL.
 * Operates on the flat component map.
 */
@Serializable
data class A2UICommand(
    val op: A2UIOperation,
    val target: String? = null,
    val component: A2UIComponent? = null,
    val data: JsonObject? = null
)

@Serializable
enum class A2UIOperation {
    @kotlinx.serialization.SerialName("replace") REPLACE,
    @kotlinx.serialization.SerialName("add") ADD,
    @kotlinx.serialization.SerialName("remove") REMOVE,
    @kotlinx.serialization.SerialName("update") UPDATE,
    @kotlinx.serialization.SerialName("setData") SET_DATA,
    @kotlinx.serialization.SerialName("clear") CLEAR
}
