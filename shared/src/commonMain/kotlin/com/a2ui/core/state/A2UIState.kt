package com.a2ui.core.state

import com.a2ui.core.model.A2UIActionEvent
import com.a2ui.core.model.A2UISurface
import com.a2ui.core.parser.A2UICommand
import com.a2ui.core.parser.A2UIOperation
import com.a2ui.core.parser.A2UIParser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject

/**
 * A2UI State Manager
 *
 * Manages the current surface state and handles updates from the agent.
 */
class A2UIStateManager {

    private val _surface = MutableStateFlow<A2UISurface?>(null)
    val surface: StateFlow<A2UISurface?> = _surface.asStateFlow()

    // Keep document alias for compatibility
    val document: StateFlow<A2UISurface?> get() = surface

    private val _actions = MutableSharedFlow<A2UIActionEvent>()
    val actions: SharedFlow<A2UIActionEvent> = _actions.asSharedFlow()

    private val _errors = MutableSharedFlow<A2UIError>()
    val errors: SharedFlow<A2UIError> = _errors.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Load a complete surface from JSON.
     */
    fun loadDocument(json: String) {
        A2UIParser.parseDocument(json)
            .onSuccess { surface ->
                _surface.value = surface
            }
            .onFailure { error ->
                _errors.tryEmit(A2UIError.ParseError(error.message ?: "Failed to parse document"))
            }
    }

    /**
     * Set a surface directly.
     */
    fun setSurface(surface: A2UISurface) {
        _surface.value = surface
    }

    /**
     * Apply incremental commands from JSONL.
     */
    fun applyCommands(jsonl: String) {
        val commands = A2UIParser.parseJsonl(jsonl)
        commands.forEach { command ->
            applyCommand(command)
        }
    }

    /**
     * Apply a single command to the surface.
     */
    fun applyCommand(command: A2UICommand) {
        val currentSurface = _surface.value ?: return

        when (command.op) {
            A2UIOperation.REPLACE -> {
                // Replace entire surface with a new parse
                // (would need the full JSON, handled by loadDocument)
            }
            A2UIOperation.ADD -> {
                command.component?.let { comp ->
                    val newComponents = currentSurface.components.toMutableMap()
                    newComponents[comp.id] = comp
                    _surface.value = currentSurface.copy(components = newComponents)
                }
            }
            A2UIOperation.REMOVE -> {
                command.target?.let { targetId ->
                    val newComponents = currentSurface.components.toMutableMap()
                    newComponents.remove(targetId)
                    _surface.value = currentSurface.copy(components = newComponents)
                }
            }
            A2UIOperation.UPDATE -> {
                command.target?.let { targetId ->
                    command.component?.let { updatedComponent ->
                        val newComponents = currentSurface.components.toMutableMap()
                        newComponents[targetId] = updatedComponent
                        _surface.value = currentSurface.copy(components = newComponents)
                    }
                }
            }
            A2UIOperation.SET_DATA -> {
                command.data?.let { newData ->
                    // Merge new data into existing data
                    val mergedEntries = currentSurface.data.toMutableMap()
                    mergedEntries.putAll(newData)
                    _surface.value = currentSurface.copy(data = JsonObject(mergedEntries))
                }
            }
            A2UIOperation.CLEAR -> {
                _surface.value = null
            }
        }
    }

    /**
     * Handle action from UI component.
     */
    suspend fun handleAction(event: A2UIActionEvent) {
        _actions.emit(event)
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun clear() {
        _surface.value = null
    }
}

sealed class A2UIError {
    data class ParseError(val message: String) : A2UIError()
    data class RenderError(val message: String) : A2UIError()
    data class NetworkError(val message: String) : A2UIError()
}
