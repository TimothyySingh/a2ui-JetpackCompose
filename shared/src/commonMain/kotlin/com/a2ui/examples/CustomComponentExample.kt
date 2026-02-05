package com.a2ui.examples

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a2ui.core.model.*
import com.a2ui.core.provider.A2UIProvider
import com.a2ui.core.registry.ComponentRegistry
import com.a2ui.core.render.A2UIActionEvent
import com.a2ui.core.render.A2UIExtendedRenderer
import com.a2ui.core.render.RenderNodeWithRegistry
import com.a2ui.core.theme.A2UITheme
import com.a2ui.core.theme.buildA2UITheme
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Example: Custom Button Component
 * A gradient button with animations
 */
@Composable
fun CustomGradientButton(
    node: A2UINode,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "button_scale"
    )
    
    val clickAction = node.actions?.find { it.event == A2UIEventType.CLICK }
    
    Button(
        onClick = {
            clickAction?.let {
                onAction(A2UIActionEvent(node.id, it.handler, it.payload))
            }
        },
        modifier = modifier
            .scale(scale)
            .height(56.dp),
        enabled = node.props?.enabled ?: true,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF667EEA),
                            Color(0xFF764BA2)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White
                )
                Text(
                    text = node.props?.text ?: "Custom Button",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Example: Custom Card Component
 * A neumorphic-style card with shadow effects
 */
@Composable
fun CustomNeumorphicCard(
    node: A2UINode,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp,
        color = Color(0xFFF0F0F3)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f),
                            Color(0xFFF0F0F3)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFE0E0E3)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                node.children?.forEach { child ->
                    RenderNodeWithRegistry(child, onAction)
                }
            }
        }
    }
}

/**
 * Example: Custom Text Component
 * Text with animated gradient colors
 */
@Composable
fun CustomAnimatedText(
    node: A2UINode,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "text_gradient")
    
    Text(
        text = node.props?.text ?: "",
        modifier = modifier,
        fontSize = node.props?.style?.size?.sp ?: 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF667EEA)
    )
}

/**
 * Example: Custom Chip/Tag Component
 * For displaying tags or categories
 */
@Composable
fun CustomChip(
    node: A2UINode,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val clickAction = node.actions?.find { it.event == A2UIEventType.CLICK }
    val text = node.props?.text ?: "Chip"
    val selected = node.props?.checked ?: false
    
    Surface(
        onClick = {
            clickAction?.let {
                onAction(A2UIActionEvent(node.id, it.handler, it.payload))
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
 * Example: Create a custom component registry
 */
fun createCustomComponentRegistry(): ComponentRegistry {
    return ComponentRegistry().apply {
        // Override standard button with gradient version
        register(A2UINodeType.BUTTON.name) { node, onAction, modifier ->
            CustomGradientButton(node, onAction, modifier)
        }
        
        // Override card with neumorphic style
        register(A2UINodeType.CARD.name) { node, onAction, modifier ->
            CustomNeumorphicCard(node, onAction, modifier)
        }
        
        // Add custom component types
        register("CHIP") { node, onAction, modifier ->
            CustomChip(node, onAction, modifier)
        }
        
        register("ANIMATED_TEXT") { node, onAction, modifier ->
            CustomAnimatedText(node, onAction, modifier)
        }
    }
}

/**
 * Example: Create a custom theme
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
            copy(
                xs = 4.dp,
                sm = 8.dp,
                md = 16.dp,
                lg = 32.dp,
                xl = 48.dp
            )
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
 * Example: Complete app with custom components
 */
@Composable
fun CustomA2UIApp() {
    val customRegistry = remember { createCustomComponentRegistry() }
    val customTheme = remember { createCustomTheme() }
    
    // Sample document with custom components
    val document = remember {
        A2UIDocument(
            version = "0.8",
            root = A2UINode(
                type = A2UINodeType.SCAFFOLD,
                children = listOf(
                    A2UINode(
                        type = A2UINodeType.TOP_BAR,
                        props = A2UIProps(text = "Custom A2UI Demo")
                    ),
                    A2UINode(
                        type = A2UINodeType.SCROLLABLE,
                        children = listOf(
                            A2UINode(
                                type = A2UINodeType.COLUMN,
                                props = A2UIProps(
                                    padding = A2UIPadding(all = 16),
                                    arrangement = A2UIArrangement.SPACE_BETWEEN
                                ),
                                children = listOf(
                                    // Custom animated text
                                    A2UINode(
                                        id = "title",
                                        type = A2UINodeType.CUSTOM,
                                        props = A2UIProps(
                                            text = "Welcome to Custom A2UI!",
                                            style = A2UITextStyle(size = 28)
                                        )
                                    ),
                                    
                                    // Neumorphic card
                                    A2UINode(
                                        type = A2UINodeType.CARD,
                                        children = listOf(
                                            A2UINode(
                                                type = A2UINodeType.COLUMN,
                                                children = listOf(
                                                    A2UINode(
                                                        type = A2UINodeType.TEXT,
                                                        props = A2UIProps(
                                                            text = "Custom Card Content",
                                                            style = A2UITextStyle(
                                                                size = 20,
                                                                weight = A2UIFontWeight.BOLD
                                                            )
                                                        )
                                                    ),
                                                    A2UINode(
                                                        type = A2UINodeType.SPACER,
                                                        props = A2UIProps(
                                                            height = A2UIDimension(
                                                                type = A2UIDimensionType.DP,
                                                                value = 8f
                                                            )
                                                        )
                                                    ),
                                                    A2UINode(
                                                        type = A2UINodeType.TEXT,
                                                        props = A2UIProps(
                                                            text = "This card uses a custom neumorphic style with gradient backgrounds."
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    ),
                                    
                                    // Row of custom chips
                                    A2UINode(
                                        type = A2UINodeType.ROW,
                                        props = A2UIProps(
                                            arrangement = A2UIArrangement.SPACE_EVENLY
                                        ),
                                        children = listOf("Design", "Development", "Testing").map { tag ->
                                            A2UINode(
                                                id = "chip_$tag",
                                                type = A2UINodeType.CUSTOM,
                                                props = A2UIProps(
                                                    text = tag,
                                                    checked = tag == "Design"
                                                ),
                                                actions = listOf(
                                                    A2UIAction(
                                                        event = A2UIEventType.CLICK,
                                                        handler = "selectTag",
                                                        payload = JsonPrimitive(tag)
                                                    )
                                                )
                                            )
                                        }
                                    ),
                                    
                                    // Gradient button
                                    A2UINode(
                                        id = "gradient_button",
                                        type = A2UINodeType.BUTTON,
                                        props = A2UIProps(
                                            text = "Click Me!",
                                            width = A2UIDimension(A2UIDimensionType.FILL)
                                        ),
                                        actions = listOf(
                                            A2UIAction(
                                                event = A2UIEventType.CLICK,
                                                handler = "handleClick"
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
    }
    
    A2UIProvider(
        componentRegistry = customRegistry,
        theme = customTheme
    ) {
        A2UIExtendedRenderer(
            document = document,
            onAction = { event ->
                // Handle actions
                println("Action: ${event.handler} from ${event.nodeId} with ${event.payload}")
            }
        )
    }
}

/**
 * Example: Partial override - only customize specific components
 */
@Composable
fun PartialOverrideExample() {
    // Only override buttons, keep everything else default
    val partialRegistry = ComponentRegistry().apply {
        register(A2UINodeType.BUTTON.name) { node, onAction, modifier ->
            // Simple outlined button override
            OutlinedButton(
                onClick = {
                    node.actions?.find { it.event == A2UIEventType.CLICK }?.let {
                        onAction(A2UIActionEvent(node.id, it.handler, it.payload))
                    }
                },
                modifier = modifier,
                border = BorderStroke(2.dp, Color(0xFF667EEA))
            ) {
                Text(
                    node.props?.text ?: "Button",
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    
    A2UIProvider(componentRegistry = partialRegistry) {
        // Your A2UI content here
    }
}