package com.a2ui.core.parser

import com.a2ui.core.model.A2UIDocument
import com.a2ui.core.model.A2UINode
import kotlinx.serialization.json.Json

/**
 * A2UI Parser - Parses JSON/JSONL into A2UI documents
 */
object A2UIParser {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    /**
     * Parse a full A2UI document from JSON
     */
    fun parseDocument(jsonString: String): Result<A2UIDocument> {
        return runCatching {
            json.decodeFromString<A2UIDocument>(jsonString)
        }
    }
    
    /**
     * Parse a single A2UI node from JSON
     */
    fun parseNode(jsonString: String): Result<A2UINode> {
        return runCatching {
            json.decodeFromString<A2UINode>(jsonString)
        }
    }
    
    /**
     * Parse JSONL format (newline-delimited JSON)
     * Each line represents a node update or command
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
    
    /**
     * Create a minimal document wrapping a single node
     */
    fun wrapNode(node: A2UINode): A2UIDocument {
        return A2UIDocument(root = node)
    }
}

/**
 * A2UI Command - For incremental updates via JSONL
 */
@kotlinx.serialization.Serializable
data class A2UICommand(
    val op: A2UIOperation,
    val target: String? = null,  // Node ID to target
    val node: A2UINode? = null,   // Node data for add/replace
    val props: com.a2ui.core.model.A2UIProps? = null  // Props for update
)

@kotlinx.serialization.Serializable
enum class A2UIOperation {
    @kotlinx.serialization.SerialName("replace") REPLACE,  // Replace entire tree
    @kotlinx.serialization.SerialName("add") ADD,          // Add child to target
    @kotlinx.serialization.SerialName("remove") REMOVE,    // Remove target node
    @kotlinx.serialization.SerialName("update") UPDATE,    // Update target props
    @kotlinx.serialization.SerialName("clear") CLEAR       // Clear all children of target
}
