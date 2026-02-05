package com.a2ui.core.registry

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.a2ui.core.model.A2UIActionEvent
import com.a2ui.core.model.A2UIComponent
import com.a2ui.core.model.A2UISurface
import com.a2ui.core.resolve.DynamicResolver

/**
 * Component renderer function type for v0.9.
 */
typealias ComponentRenderer = @Composable (
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) -> Unit

/**
 * Registry for custom component renderers.
 * Uses PascalCase type strings: "Button", "Text", "Card", etc.
 */
class ComponentRegistry {
    private val components = mutableMapOf<String, ComponentRenderer>()

    fun register(type: String, renderer: ComponentRenderer) {
        components[type] = renderer
    }

    fun registerAll(renderers: Map<String, ComponentRenderer>) {
        components.putAll(renderers)
    }

    fun get(type: String): ComponentRenderer? = components[type]

    fun has(type: String): Boolean = components.containsKey(type)

    fun unregister(type: String) {
        components.remove(type)
    }

    fun clear() {
        components.clear()
    }

    fun copy(): ComponentRegistry = ComponentRegistry().apply {
        registerAll(this@ComponentRegistry.components)
    }

    fun merge(other: ComponentRegistry) {
        components.putAll(other.components)
    }

    companion object {
        fun withDefaults(builder: ComponentRegistry.() -> Unit): ComponentRegistry {
            return ComponentRegistry().apply(builder)
        }
    }
}
