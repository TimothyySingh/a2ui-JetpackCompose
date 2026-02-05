package com.a2ui.examples

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a2ui.core.model.*
import com.a2ui.core.provider.A2UIProvider
import com.a2ui.core.registry.ComponentRegistry
import com.a2ui.core.render.A2UIExtendedRenderer
import com.a2ui.core.render.RenderComponent
import com.a2ui.core.resolve.*
import com.a2ui.core.theme.A2UITheme
import com.a2ui.core.theme.buildA2UITheme
import kotlinx.serialization.json.*

/**
 * Custom Gradient Button - v0.9 compatible.
 */
@Composable
fun CustomGradientButton(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "button_scale"
    )

    val action = component.action()
    val enabled = component.isEnabled(resolver)

    Button(
        onClick = {
            if (action?.name != null) {
                onAction(A2UIActionEvent(component.id, action.name, action.context))
            }
        },
        modifier = modifier.scale(scale).height(56.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                Text(
                    text = component.text(resolver) ?: "Custom Button",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Custom Neumorphic Card - v0.9 compatible.
 */
@Composable
fun CustomNeumorphicCard(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(4.dp),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp,
        color = Color(0xFFF0F0F3)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.7f), Color(0xFFF0F0F3))
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White, Color(0xFFE0E0E3))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                val childResolver = ChildListResolver(resolver)
                val children = childResolver.resolve(component.children, surface.components)
                for (child in children) {
                    RenderComponent(child.componentId, surface, resolver, onAction)
                }
            }
        }
    }
}

/**
 * Custom Animated Text - v0.9 compatible.
 */
@Composable
fun CustomAnimatedText(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "text_gradient")
    val text = component.text(resolver) ?: ""

    Text(
        text = text,
        modifier = modifier,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF667EEA)
    )
}

/**
 * Custom Chip - v0.9 compatible.
 */
@Composable
fun CustomChip(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val action = component.action()
    val text = component.text(resolver) ?: "Chip"
    val selected = resolver.resolveBoolean(component.properties["selected"]) ?: false

    Surface(
        onClick = {
            if (action?.name != null) {
                onAction(A2UIActionEvent(component.id, action.name, action.context))
            }
        },
        modifier = modifier,
        shape = CircleShape,
        color = if (selected) Color(0xFF667EEA) else Color(0xFFF0F0F3),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Color.Transparent else Color(0xFFE0E0E3)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 14.sp,
            color = if (selected) Color.White else Color(0xFF333333)
        )
    }
}

/**
 * Create a custom component registry using PascalCase types.
 */
fun createCustomComponentRegistry(): ComponentRegistry {
    return ComponentRegistry().apply {
        register("Button") { component, surface, resolver, onAction, modifier ->
            CustomGradientButton(component, surface, resolver, onAction, modifier)
        }
        register("Card") { component, surface, resolver, onAction, modifier ->
            CustomNeumorphicCard(component, surface, resolver, onAction, modifier)
        }
        register("Chip") { component, surface, resolver, onAction, modifier ->
            CustomChip(component, surface, resolver, onAction, modifier)
        }
        register("AnimatedText") { component, surface, resolver, onAction, modifier ->
            CustomAnimatedText(component, surface, resolver, onAction, modifier)
        }
    }
}

/**
 * Create a custom theme.
 */
fun createCustomTheme(): A2UITheme {
    return buildA2UITheme {
        colors {
            copy(
                primary = Color(0xFF667EEA),
                primaryVariant = Color(0xFF764BA2),
                secondary = Color(0xFFFFC107),
                background = Color(0xFFF7F8FA),
                surface = Color.White
            )
        }
        typography {
            copy(
                h1 = h1.copy(fontSize = 36.sp, fontWeight = FontWeight.Black),
                h2 = h2.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                body = body.copy(fontSize = 16.sp, lineHeight = 24.sp)
            )
        }
        spacing {
            copy(xs = 4.dp, sm = 8.dp, md = 16.dp, lg = 32.dp, xl = 48.dp)
        }
        components {
            copy(
                button = button.copy(
                    minHeight = 56.dp,
                    horizontalPadding = 24.dp,
                    cornerRadius = 12.dp
                ),
                card = card.copy(
                    elevation = 8.dp,
                    padding = 20.dp,
                    cornerRadius = 20.dp
                )
            )
        }
    }
}

/**
 * Complete custom app using v0.9 surface model.
 */
@Composable
fun CustomA2UIApp() {
    val customRegistry = remember { createCustomComponentRegistry() }
    val customTheme = remember { createCustomTheme() }

    val surface = remember {
        val components = mutableMapOf<String, A2UIComponent>()
        fun add(c: A2UIComponent) { components[c.id] = c }

        add(A2UIComponent(
            id = "root",
            component = "Column",
            children = JsonArray(listOf("title", "card1", "chip_row", "gradient_btn").map { JsonPrimitive(it) })
        ))

        add(A2UIComponent(
            id = "title",
            component = "Text",
            properties = buildJsonObject {
                put("text", "Custom A2UI Demo!")
                put("variant", "h2")
            }
        ))

        add(A2UIComponent(
            id = "card1",
            component = "Card",
            children = JsonArray(listOf("card_col").map { JsonPrimitive(it) })
        ))

        add(A2UIComponent(
            id = "card_col",
            component = "Column",
            children = JsonArray(listOf("card_title", "card_desc").map { JsonPrimitive(it) })
        ))

        add(A2UIComponent(
            id = "card_title",
            component = "Text",
            properties = buildJsonObject {
                put("text", "Custom Card Content")
                put("variant", "h4")
            }
        ))

        add(A2UIComponent(
            id = "card_desc",
            component = "Text",
            properties = buildJsonObject {
                put("text", "This card uses a custom neumorphic style with gradient backgrounds.")
            }
        ))

        add(A2UIComponent(
            id = "chip_row",
            component = "Row",
            properties = buildJsonObject { put("justify", "spaceEvenly") },
            children = JsonArray(listOf("chip_design", "chip_dev", "chip_test").map { JsonPrimitive(it) })
        ))

        listOf("Design" to true, "Development" to false, "Testing" to false).forEachIndexed { _, (label, sel) ->
            add(A2UIComponent(
                id = "chip_${label.lowercase()}",
                component = "Chip",
                properties = buildJsonObject {
                    put("text", label)
                    put("selected", sel)
                    putJsonObject("action") { put("name", "selectTag") }
                }
            ))
        }

        add(A2UIComponent(
            id = "gradient_btn",
            component = "Button",
            properties = buildJsonObject {
                put("text", "Click Me!")
                putJsonObject("action") { put("name", "handleClick") }
            }
        ))

        A2UISurface(root = "root", components = components)
    }

    A2UIProvider(
        componentRegistry = customRegistry,
        theme = customTheme
    ) {
        A2UIExtendedRenderer(
            surface = surface,
            onAction = { event ->
                println("Action: ${event.actionName} from ${event.componentId}")
            }
        )
    }
}

/**
 * Partial override - only customize buttons.
 */
@Composable
fun PartialOverrideExample() {
    val partialRegistry = ComponentRegistry().apply {
        register("Button") { component, surface, resolver, onAction, modifier ->
            val action = component.action()
            OutlinedButton(
                onClick = {
                    if (action?.name != null) {
                        onAction(A2UIActionEvent(component.id, action.name, action.context))
                    }
                },
                modifier = modifier,
                border = BorderStroke(2.dp, Color(0xFF667EEA))
            ) {
                Text(
                    component.text(resolver) ?: "Button",
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    A2UIProvider(componentRegistry = partialRegistry) {
        // Content here
    }
}
