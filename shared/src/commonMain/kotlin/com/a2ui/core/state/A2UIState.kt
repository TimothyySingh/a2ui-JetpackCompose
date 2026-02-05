package com.a2ui.core.state

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.a2ui.core.model.A2UIDocument
import com.a2ui.core.model.A2UINode
import com.a2ui.core.parser.A2UICommand
import com.a2ui.core.parser.A2UIOperation
import com.a2ui.core.parser.A2UIParser
import com.a2ui.core.render.A2UIActionEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A2UI State Manager
 * 
 * Manages the current UI state and handles updates from the agent.
 * Emits action events back to the agent.
 */
class A2UIStateManager {
    
    private val _document = MutableStateFlow<A2UIDocument?>(null)
    val document: StateFlow<A2UIDocument?> = _document.asStateFlow()
    
    private val _actions = MutableSharedFlow<A2UIActionEvent>()
    val actions: SharedFlow<A2UIActionEvent> = _actions.asSharedFlow()
    
    private val _errors = MutableSharedFlow<A2UIError>()
    val errors: SharedFlow<A2UIError> = _errors.asSharedFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    /**
     * Load a complete document from JSON
     */
    fun loadDocument(json: String) {
        A2UIParser.parseDocument(json)
            .onSuccess { doc ->
                _document.value = doc
            }
            .onFailure { error ->
                _errors.tryEmit(A2UIError.ParseError(error.message ?: "Failed to parse document"))
            }
    }
    
    /**
     * Load a single node as the root
     */
    fun loadNode(json: String) {
        A2UIParser.parseNode(json)
            .onSuccess { node ->
                _document.value = A2UIParser.wrapNode(node)
            }
            .onFailure { error ->
                _errors.tryEmit(A2UIError.ParseError(error.message ?: "Failed to parse node"))
            }
    }
    
    /**
     * Apply incremental                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       from JSONL
     */
    fun applyCommands(jsonl: String) {
        val commands = A2UIParser.parseJsonl(jsonl)
        commands.forEach { command ->
            applyCommand(command)
        }
    }
    
    /**
     * Apply a single command
     */
    fun applyCommand(command: A2UICommand) {
        val currentDoc = _document.value ?: return
        
        when (command.op) {
            A2UIOperation.REPLACE -> {
                command.node?.let { node ->
                    _document.value = A2UIParser.wrapNode(node)
                }
            }
            A2UIOperation.ADD -> {
                // TODO: Implement tree manipulation
            }
            A2UIOperation.REMOVE -> {
                // TODO: Implement tree manipulation
            }
            A2UIOperation.UPDATE -> {
                // TODO: Implement props update
            }
            A2UIOperation.CLEAR -> {
                // TODO: Implement clear children
            }
        }
    }
    
    /**
     * Handle action from UI component
     */
    suspend fun handleAction(event: A2UIActionEvent) {
        _actions.emit(event)
    }
    
    /**
     * Set loading state
     */
    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    /**
     * Clear the current document
     */
    fun clear() {
        _document.value = null
    }
}

/**
 * A2UI Error types
 */
sealed class A2UIError {
    data class ParseError(val message: String) : A2UIError()
    data class RenderError(val message: String) : A2UIError()
    data class NetworkError(val message: String) : A2UIError()
}
