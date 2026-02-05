package com.a2ui.core.registry

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.a2ui.core.model.A2UINode
import com.a2ui.core.render.A2UIActionEvent

/**
 * Component renderer function type
 */
typealias ComponentRenderer = @Composable (
    node: A2UINode,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) -> Unit

/**
 * Registry for custom component renderers.
 * Allows consumers to override default component implementations
 * with their own custom Composable functions.
 * 
 * @example
 * ```kotlin
 * val customRegistry = ComponentRegistry().apply {
 *     // Override the default button implementation
 *     register(A2UINodeType.BUTTON) { node, onAction, modifier ->
 *         MyCustomButton(node, onAction, modifier)
 *     }
 *     
 *     // Override text rendering with custom styling
 *     register(A2UINodeType.TEXT) { node, _, modifier ->
 *         MyFancyText(node.props?.text ?: "", modifier)
 *     }
 * }
 * ```
 */
class ComponentRegistry {
    private val components = mutableMapOf<String, ComponentRenderer>()
    
    /**
     * Register a custom renderer for a component type
     * @param type The A2UI node type to override
     * @param renderer The custom composable renderer
     */
    fun register(type: String, renderer: ComponentRenderer) {
        components[type] = renderer
    }
    
    /**
     * Register multiple custom renderers at once
     * @param renderers Map of node types to renderers
     */
    fun registerAll(renderers: Map<String, ComponentRenderer>) {
        components.putAll(renderers)
    }
    
    /**
     * Get a custom renderer for a node type
     * @param type The node type to look up
     * @return The custom renderer, or null if not registered
     */
    fun get(type: String): ComponentRenderer? = components[type]
    
    /**
     * Check if a custom renderer exists for a type
     * @param type The node type to check
     * @return true if a custom renderer is registered
     */
    fun has(type: String): Boolean = components.containsKey(type)
    
    /**
     * Remove a custom renderer
     * @param type The node type to remove
     */
    fun unregister(type: String) {
        components.remove(type)
    }
    
    /**
     * Clear all custom renderers
     */
    fun clear() {
        components.clear()
    }
    
    /**
     * Create a copy of this registry
     * @return A new registry with the same components
     */
    fun copy(): ComponentRegistry = ComponentRegistry().apply {
        registerAll(this@ComponentRegistry.components)
    }
    
    /**
     * Merge another registry into this one
     * @param other The registry to merge
     */
    fun merge(other: ComponentRegistry) {
        components.putAll(other.components)
    }
    
    companion object {
        /**
         * Create a registry with common custom components
         */
        fun withDefaults(builder: ComponentRegistry.() -> Unit): ComponentRegistry {
            return ComponentRegistry().apply(builder)
        }
    }
}