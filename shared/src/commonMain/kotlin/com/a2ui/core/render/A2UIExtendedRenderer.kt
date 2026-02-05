package com.a2ui.core.render

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.a2ui.core.model.*
import com.a2ui.core.provider.useA2UITheme
import com.a2ui.core.provider.useComponentRegistry
import com.a2ui.core.resolve.*

/**
 * Extended A2UI Renderer with component override support.
 * Checks the ComponentRegistry for custom implementations before
 * falling back to the default A2UIRenderer.
 */
@Composable
fun A2UIExtendedRenderer(
    surface: A2UISurface,
    onAction: (A2UIActionEvent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val resolver = remember(surface.data) {
        DynamicResolver(ResolverContext(surface.data))
    }

    RenderComponentWithRegistry(
        componentId = surface.root,
        surface = surface,
        resolver = resolver,
        onAction = onAction,
        modifier = modifier
    )
}

@Composable
fun RenderComponentWithRegistry(
    componentId: String,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val registry = useComponentRegistry()
    val theme = useA2UITheme()

    val component = surface.components[componentId] ?: return

    // Check for custom renderer first
    val customRenderer = registry.get(component.component)
    if (customRenderer != null) {
        customRenderer(component, surface, resolver, onAction, modifier)
        return
    }

    // Fall back to default implementation
    RenderComponent(componentId, surface, resolver, onAction, modifier)
}
