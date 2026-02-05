package com.a2ui.core.provider

import androidx.compose.runtime.*
import com.a2ui.core.registry.ComponentRegistry
import com.a2ui.core.theme.A2UITheme

/**
 * Provider for A2UI configuration and customization.
 * This provides component registry and theme configuration to child components.
 * 
 * @example
 * ```kotlin
 * @Composable
 * fun MyApp() {
 *     val customRegistry = remember {
 *         ComponentRegistry().apply {
 *             register(A2UINodeType.BUTTON) { node, onAction, modifier ->
 *                 MyCustomButton(node, onAction, modifier)
 *             }
 *         }
 *     }
 *     
 *     A2UIProvider(
 *         componentRegistry = customRegistry,
 *         theme = MyCustomTheme
 *     ) {
 *         // Your app content with A2UI components
 *         A2UIRenderer(document)
 *     }
 * }
 * ```
 */
@Composable
fun A2UIProvider(
    componentRegistry: ComponentRegistry = ComponentRegistry(),
    theme: A2UITheme = A2UITheme.Default,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalComponentRegistry provides componentRegistry,
        LocalA2UITheme provides theme
    ) {
        content()
    }
}

/**
 * CompositionLocal for accessing the component registry
 */
internal val LocalComponentRegistry = compositionLocalOf { ComponentRegistry() }

/**
 * CompositionLocal for accessing the theme
 */
internal val LocalA2UITheme = compositionLocalOf { A2UITheme.Default }

/**
 * Hook to access the current component registry
 */
@Composable
fun useComponentRegistry(): ComponentRegistry = LocalComponentRegistry.current

/**
 * Hook to access the current theme
 */
@Composable
fun useA2UITheme(): A2UITheme = LocalA2UITheme.current

/**
 * Data class for A2UI configuration
 */
data class A2UIConfig(
    val componentRegistry: ComponentRegistry = ComponentRegistry(),
    val theme: A2UITheme = A2UITheme.Default,
    val debug: Boolean = false
)

/**
 * Alternative provider using a config object
 */
@Composable
fun A2UIProvider(
    config: A2UIConfig,
    content: @Composable () -> Unit
) {
    A2UIProvider(
        componentRegistry = config.componentRegistry,
        theme = config.theme,
        content = content
    )
}