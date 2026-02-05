package com.a2ui.core.render

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a2ui.core.model.*
import com.a2ui.core.provider.useComponentRegistry
import com.a2ui.core.provider.useA2UITheme
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

/**
 * Extended A2UI Renderer with component override support.
 * This renderer checks for custom components in the registry before
 * falling back to default implementations.
 * 
 * @param document The A2UI document to render
 * @param onAction Callback for handling user actions
 * @param modifier Optional modifier for the root element
 * 
 * @example
 * ```kotlin
 * A2UIProvider(componentRegistry = myCustomRegistry) {
 *     A2UIExtendedRenderer(
 *         document = document,
 *         onAction = { event -> handleAction(event) }
 *     )
 * }
 * ```
 */
@Composable
fun A2UIExtendedRenderer(
    document: A2UIDocument,
    onAction: (A2UIActionEvent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    RenderNodeWithRegistry(
        node = document.root,
        onAction = onAction,
        modifier = modifier
    )
}

/**
 * Render a node, checking registry for custom implementations first
 */
@Composable
fun RenderNodeWithRegistry(
    node: A2UINode,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val registry = useComponentRegistry()
    val theme = useA2UITheme()
    
    // Check for custom renderer first
    val customRenderer = registry.get(node.type.name)
    if (customRenderer != null) {
        customRenderer(node, onAction, modifier)
        return
    }
    
    // Fall back to default implementation
    DefaultNodeRenderer(node, onAction, modifier)
}

/**
 * Default renderer implementations
 */
@Composable
private fun DefaultNodeRenderer(
    node: A2UINode,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val theme = useA2UITheme()
    val nodeModifier = modifier
        .applyProps(node.props, theme)
        .applyActions(node, onAction)
    
    when (node.type) {
        // Layout
        A2UINodeType.COLUMN -> DefaultColumn(node, onAction, nodeModifier)
        A2UINodeType.ROW -> DefaultRow(node, onAction, nodeModifier)
        A2UINodeType.BOX -> DefaultBox(node, onAction, nodeModifier)
        A2UINodeType.CARD -> DefaultCard(node, onAction, nodeModifier, theme)
        A2UINodeType.SCAFFOLD -> DefaultScaffold(node, onAction, nodeModifier)
        A2UINodeType.SCROLLABLE -> DefaultScrollable(node, onAction, nodeModifier)
        A2UINodeType.LAZY_COLUMN -> DefaultLazyColumn(node, onAction, nodeModifier)
        A2UINodeType.LAZY_ROW -> DefaultLazyRow(node, onAction, nodeModifier)
        
        // Content
        A2UINodeType.TEXT -> DefaultText(node, nodeModifier, theme)
        A2UINodeType.IMAGE -> DefaultImage(node, nodeModifier)
        A2UINodeType.ICON -> DefaultIcon(node, nodeModifier)
        A2UINodeType.SPACER -> Spacer(nodeModifier)
        A2UINodeType.DIVIDER -> HorizontalDivider(nodeModifier)
        
        // Input
        A2UINodeType.BUTTON -> DefaultButton(node, onAction, nodeModifier, theme)
        A2UINodeType.TEXT_FIELD -> DefaultTextField(node, onAction, nodeModifier, theme)
        A2UINodeType.CHECKBOX -> DefaultCheckbox(node, onAction, nodeModifier)
        A2UINodeType.SWITCH -> DefaultSwitch(node, onAction, nodeModifier)
        A2UINodeType.SLIDER -> DefaultSlider(node, onAction, nodeModifier)
        A2UINodeType.DROPDOWN -> DefaultDropdown(node, onAction, nodeModifier)
        
        // Feedback
        A2UINodeType.PROGRESS -> DefaultProgress(node, nodeModifier)
        A2UINodeType.LOADING -> DefaultLoading(nodeModifier)
        A2UINodeType.SNACKBAR -> { /* Handled at scaffold level */ }
        
        // Navigation
        A2UINodeType.TOP_BAR -> DefaultTopBar(node, onAction, nodeModifier)
        A2UINodeType.BOTTOM_BAR -> DefaultBottomBar(node, onAction, nodeModifier)
        A2UINodeType.NAV_ITEM -> { /* Rendered by parent */ }
        A2UINodeType.FAB -> DefaultFab(node, onAction, nodeModifier)
        
        // Custom
        A2UINodeType.CUSTOM -> DefaultCustom(node, onAction, nodeModifier)
    }
}

// Default Layout Components

@Composable
private fun DefaultColumn(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = node.props?.alignment.toHorizontalAlignment(),
        verticalArrangement = node.props?.arrangement.toVerticalArrangement()
    ) {
        node.children?.forEach { child ->
            RenderNodeWithRegistry(child, onAction, Modifier.maybeWeight(child.props?.weight))
        }
    }
}

@Composable
private fun DefaultRow(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = node.props?.alignment.toVerticalAlignment(),
        horizontalArrangement = node.props?.arrangement.toHorizontalArrangement()
    ) {
        node.children?.forEach { child ->
            RenderNodeWithRegistry(child, onAction, Modifier.maybeWeight(child.props?.weight))
        }
    }
}

@Composable
private fun DefaultBox(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    Box(modifier = modifier) {
        node.children?.forEach { child ->
            RenderNodeWithRegistry(child, onAction)
        }
    }
}

@Composable
private fun DefaultCard(
    node: A2UINode, 
    onAction: (A2UIActionEvent) -> Unit, 
    modifier: Modifier,
    theme: com.a2ui.core.theme.A2UITheme
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = (node.props?.elevation ?: theme.components.card.elevation.value).dp
        ),
        shape = RoundedCornerShape(
            node.props?.cornerRadius?.dp ?: theme.components.card.cornerRadius
        )
    ) {
        Box(Modifier.padding(theme.components.card.padding)) {
            node.children?.forEach { child ->
                RenderNodeWithRegistry(child, onAction)
            }
        }
    }
}

@Composable
private fun DefaultScaffold(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    val topBar = node.children?.find { it.type == A2UINodeType.TOP_BAR }
    val bottomBar = node.children?.find { it.type == A2UINodeType.BOTTOM_BAR }
    val fab = node.children?.find { it.type == A2UINodeType.FAB }
    val content = node.children?.filter { 
        it.type != A2UINodeType.TOP_BAR && 
        it.type != A2UINodeType.BOTTOM_BAR && 
        it.type != A2UINodeType.FAB 
    }
    
    Scaffold(
        modifier = modifier,
        topBar = { topBar?.let { DefaultTopBar(it, onAction) } },
        bottomBar = { bottomBar?.let { DefaultBottomBar(it, onAction) } },
        floatingActionButton = { fab?.let { DefaultFab(it, onAction) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            content?.forEach { child ->
                RenderNodeWithRegistry(child, onAction)
            }
        }
    }
}

@Composable
private fun DefaultScrollable(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        node.children?.forEach { child ->
            RenderNodeWithRegistry(child, onAction)
        }
    }
}

@Composable
private fun DefaultLazyColumn(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    LazyColumn(modifier = modifier) {
        items(node.children ?: emptyList()) { child ->
            RenderNodeWithRegistry(child, onAction)
        }
    }
}

@Composable
private fun DefaultLazyRow(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    LazyRow(modifier = modifier) {
        items(node.children ?: emptyList()) { child ->
            RenderNodeWithRegistry(child, onAction)
        }
    }
}

// Default Content Components

@Composable
private fun DefaultText(
    node: A2UINode, 
    modifier: Modifier,
    theme: com.a2ui.core.theme.A2UITheme
) {
    val style = node.props?.style
    val usageHint = when(style?.size) {
        32 -> theme.typography.h1
        28 -> theme.typography.h2
        24 -> theme.typography.h3
        20 -> theme.typography.h4
        18 -> theme.typography.h5
        14 -> theme.typography.caption
        else -> theme.typography.body
    }
    
    Text(
        text = node.props?.text ?: "",
        modifier = modifier,
        fontSize = usageHint.fontSize,
        fontWeight = style?.weight.toFontWeight() ?: usageHint.fontWeight,
        color = style?.color?.toColor() ?: theme.colors.onSurface,
        textAlign = style?.align.toTextAlign(),
        maxLines = style?.maxLines ?: Int.MAX_VALUE,
        overflow = style?.overflow.toTextOverflow(),
        lineHeight = usageHint.lineHeight
    )
}

@Composable
private fun DefaultImage(node: A2UINode, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(Color.Gray.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text("Image: ${node.props?.src ?: "?"}")
    }
}

@Composable
private fun DefaultIcon(node: A2UINode, modifier: Modifier) {
    Text(
        text = "⬤",
        modifier = modifier
    )
}

// Default Input Components

@Composable
private fun DefaultButton(
    node: A2UINode, 
    onAction: (A2UIActionEvent) -> Unit, 
    modifier: Modifier,
    theme: com.a2ui.core.theme.A2UITheme
) {
    val clickAction = node.actions?.find { it.event == A2UIEventType.CLICK }
    
    Button(
        onClick = {
            clickAction?.let {
                onAction(A2UIActionEvent(node.id, it.handler, it.payload))
            }
        },
        modifier = modifier.height(theme.components.button.minHeight),
        enabled = node.props?.enabled ?: true,
        shape = RoundedCornerShape(theme.components.button.cornerRadius),
        contentPadding = PaddingValues(
            horizontal = theme.components.button.horizontalPadding,
            vertical = theme.components.button.verticalPadding
        )
    ) {
        Text(
            node.props?.text ?: "Button",
            style = androidx.compose.ui.text.TextStyle(
                fontSize = theme.typography.button.fontSize
            )
        )
    }
}

@Composable
private fun DefaultTextField(
    node: A2UINode, 
    onAction: (A2UIActionEvent) -> Unit, 
    modifier: Modifier,
    theme: com.a2ui.core.theme.A2UITheme
) {
    var text by remember { 
        mutableStateOf(
            (node.props?.value as? JsonPrimitive)?.contentOrNull ?: ""
        ) 
    }
    val changeAction = node.actions?.find { it.event == A2UIEventType.CHANGE }
    
    OutlinedTextField(
        value = text,
        onValueChange = { newValue ->
            text = newValue
            changeAction?.let {
                onAction(A2UIActionEvent(node.id, it.handler, JsonPrimitive(newValue)))
            }
        },
        modifier = modifier,
        label = node.props?.label?.let { { Text(it) } },
        placeholder = node.props?.hint?.let { { Text(it) } },
        enabled = node.props?.enabled ?: true,
        maxLines = node.props?.maxLines ?: 1,
        shape = RoundedCornerShape(theme.components.textField.cornerRadius)
    )
}

@Composable
private fun DefaultCheckbox(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    var checked by remember { 
        mutableStateOf(
            (node.props?.value as? JsonPrimitive)?.booleanOrNull ?: node.props?.checked ?: false
        ) 
    }
    val changeAction = node.actions?.find { it.event == A2UIEventType.CHANGE }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { newValue ->
                checked = newValue
                changeAction?.let {
                    onAction(A2UIActionEvent(node.id, it.handler, JsonPrimitive(newValue)))
                }
            },
            enabled = node.props?.enabled ?: true
        )
        node.props?.label?.let { Text(it) }
    }
}

@Composable
private fun DefaultSwitch(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    var checked by remember { 
        mutableStateOf(
            (node.props?.value as? JsonPrimitive)?.booleanOrNull ?: node.props?.checked ?: false
        )
    }
    val changeAction = node.actions?.find { it.event == A2UIEventType.CHANGE }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        node.props?.label?.let { 
            Text(it, modifier = Modifier.weight(1f)) 
        }
        Switch(
            checked = checked,
            onCheckedChange = { newValue ->
                checked = newValue
                changeAction?.let {
                    onAction(A2UIActionEvent(node.id, it.handler, JsonPrimitive(newValue)))
                }
            },
            enabled = node.props?.enabled ?: true
        )
    }
}

@Composable
private fun DefaultSlider(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    var value by remember { 
        mutableStateOf(node.props?.progress ?: 0f) 
    }
    val changeAction = node.actions?.find { it.event == A2UIEventType.CHANGE }
    
    Slider(
        value = value,
        onValueChange = { newValue ->
            value = newValue
            changeAction?.let {
                onAction(A2UIActionEvent(node.id, it.handler, JsonPrimitive(newValue)))
            }
        },
        modifier = modifier,
        enabled = node.props?.enabled ?: true
    )
}

@Composable
private fun DefaultDropdown(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { 
        mutableStateOf(
            (node.props?.value as? JsonPrimitive)?.contentOrNull ?: ""
        )
    }
    val changeAction = node.actions?.find { it.event == A2UIEventType.CHANGE }
    
    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(
                node.props?.options?.find { it.value == selectedOption }?.label 
                    ?: node.props?.hint 
                    ?: "Select..."
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            node.props?.options?.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        selectedOption = option.value
                        expanded = false
                        changeAction?.let {
                            onAction(A2UIActionEvent(node.id, it.handler, JsonPrimitive(option.value)))
                        }
                    }
                )
            }
        }
    }
}

// Default Feedback Components

@Composable
private fun DefaultProgress(node: A2UINode, modifier: Modifier) {
    val progress = node.props?.progress
    if (progress != null) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = modifier
        )
    } else {
        LinearProgressIndicator(modifier = modifier)
    }
}

@Composable
private fun DefaultLoading(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// Default Navigation Components

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTopBar(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier = Modifier) {
    TopAppBar(
        title = { Text(node.props?.text ?: "") },
        modifier = modifier
    )
}

@Composable
private fun DefaultBottomBar(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier) {
        node.children?.forEach { navItem ->
            val clickAction = navItem.actions?.find { it.event == A2UIEventType.CLICK }
            NavigationBarItem(
                icon = { Text("•") },
                label = { Text(navItem.props?.label ?: "") },
                selected = navItem.props?.checked ?: false,
                onClick = {
                    clickAction?.let {
                        onAction(A2UIActionEvent(navItem.id, it.handler, it.payload))
                    }
                }
            )
        }
    }
}

@Composable
private fun DefaultFab(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier = Modifier) {
    val clickAction = node.actions?.find { it.event == A2UIEventType.CLICK }
    
    FloatingActionButton(
        onClick = {
            clickAction?.let {
                onAction(A2UIActionEvent(node.id, it.handler, it.payload))
            }
        },
        modifier = modifier
    ) {
        Text(node.props?.icon ?: "+")
    }
}

// Default Custom Component

@Composable
private fun DefaultCustom(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(Color.LightGray.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Text("Custom: ${node.id ?: "unknown"}")
    }
}

// Extension Functions

private fun Modifier.applyProps(props: A2UIProps?, theme: com.a2ui.core.theme.A2UITheme): Modifier {
    if (props == null) return this
    
    var mod = this
    
    // Dimensions
    props.width?.let { dim ->
        mod = when (dim.type) {
            A2UIDimensionType.FILL -> mod.fillMaxWidth()
            A2UIDimensionType.WRAP -> mod
            A2UIDimensionType.DP -> dim.value?.let { mod.width(it.dp) } ?: mod
        }
    }
    
    props.height?.let { dim ->
        mod = when (dim.type) {
            A2UIDimensionType.FILL -> mod.fillMaxHeight()
            A2UIDimensionType.WRAP -> mod
            A2UIDimensionType.DP -> dim.value?.let { mod.height(it.dp) } ?: mod
        }
    }
    
    // Padding with theme defaults
    props.padding?.let { p ->
        mod = when {
            p.all != null -> mod.padding(p.all.dp)
            p.horizontal != null || p.vertical != null -> 
                mod.padding(horizontal = (p.horizontal ?: 0).dp, vertical = (p.vertical ?: 0).dp)
            else -> mod.padding(
                start = (p.start ?: 0).dp,
                end = (p.end ?: 0).dp,
                top = (p.top ?: 0).dp,
                bottom = (p.bottom ?: 0).dp
            )
        }
    }
    
    // Background with theme color
    props.background?.let { bg ->
        mod = mod.background(bg.toColor(theme))
    }
    
    // Corner radius with theme shapes
    props.cornerRadius?.let { radius ->
        mod = mod.clip(RoundedCornerShape(radius.dp))
    }
    
    return mod
}

private fun Modifier.applyActions(node: A2UINode, onAction: (A2UIActionEvent) -> Unit): Modifier {
    val clickAction = node.actions?.find { it.event == A2UIEventType.CLICK }
    
    return if (clickAction != null && node.type !in listOf(
        A2UINodeType.BUTTON, 
        A2UINodeType.CHECKBOX, 
        A2UINodeType.SWITCH,
        A2UINodeType.FAB,
        A2UINodeType.NAV_ITEM
    )) {
        this.clickable { 
            onAction(A2UIActionEvent(node.id, clickAction.handler, clickAction.payload))
        }
    } else {
        this
    }
}

@Composable
private fun RowScope.Modifier.maybeWeight(weight: Float?): Modifier {
    return if (weight != null) this.weight(weight) else this
}

@Composable
private fun ColumnScope.Modifier.maybeWeight(weight: Float?): Modifier {
    return if (weight != null) this.weight(weight) else this
}

private fun String.toColor(theme: com.a2ui.core.theme.A2UITheme): Color {
    // Check for theme color names first
    return when(this.lowercase()) {
        "primary" -> theme.colors.primary
        "secondary" -> theme.colors.secondary
        "background" -> theme.colors.background
        "surface" -> theme.colors.surface
        "error" -> theme.colors.error
        else -> {
            try {
                Color(android.graphics.Color.parseColor(if (startsWith("#")) this else "#$this"))
            } catch (e: Exception) {
                theme.colors.surface
            }
        }
    }
}

private fun A2UIAlignment?.toHorizontalAlignment(): Alignment.Horizontal = when (this) {
    A2UIAlignment.START -> Alignment.Start
    A2UIAlignment.CENTER, A2UIAlignment.CENTER_HORIZONTAL -> Alignment.CenterHorizontally
    A2UIAlignment.END -> Alignment.End
    else -> Alignment.Start
}

private fun A2UIAlignment?.toVerticalAlignment(): Alignment.Vertical = when (this) {
    A2UIAlignment.TOP -> Alignment.Top
    A2UIAlignment.CENTER, A2UIAlignment.CENTER_VERTICAL -> Alignment.CenterVertically
    A2UIAlignment.BOTTOM -> Alignment.Bottom
    else -> Alignment.Top
}

private fun A2UIArrangement?.toVerticalArrangement(): Arrangement.Vertical = when (this) {
    A2UIArrangement.START -> Arrangement.Top
    A2UIArrangement.CENTER -> Arrangement.Center
    A2UIArrangement.END -> Arrangement.Bottom
    A2UIArrangement.SPACE_BETWEEN -> Arrangement.SpaceBetween
    A2UIArrangement.SPACE_AROUND -> Arrangement.SpaceAround
    A2UIArrangement.SPACE_EVENLY -> Arrangement.SpaceEvenly
    else -> Arrangement.Top
}

private fun A2UIArrangement?.toHorizontalArrangement(): Arrangement.Horizontal = when (this) {
    A2UIArrangement.START -> Arrangement.Start
    A2UIArrangement.CENTER -> Arrangement.Center
    A2UIArrangement.END -> Arrangement.End
    A2UIArrangement.SPACE_BETWEEN -> Arrangement.SpaceBetween
    A2UIArrangement.SPACE_AROUND -> Arrangement.SpaceAround
    A2UIArrangement.SPACE_EVENLY -> Arrangement.SpaceEvenly
    else -> Arrangement.Start
}

private fun A2UIFontWeight?.toFontWeight(): FontWeight = when (this) {
    A2UIFontWeight.THIN -> FontWeight.Thin
    A2UIFontWeight.LIGHT -> FontWeight.Light
    A2UIFontWeight.NORMAL -> FontWeight.Normal
    A2UIFontWeight.MEDIUM -> FontWeight.Medium
    A2UIFontWeight.SEMIBOLD -> FontWeight.SemiBold
    A2UIFontWeight.BOLD -> FontWeight.Bold
    else -> FontWeight.Normal
}

private fun A2UITextAlign?.toTextAlign(): TextAlign = when (this) {
    A2UITextAlign.START -> TextAlign.Start
    A2UITextAlign.CENTER -> TextAlign.Center
    A2UITextAlign.END -> TextAlign.End
    else -> TextAlign.Start
}

private fun A2UITextOverflow?.toTextOverflow(): TextOverflow = when (this) {
    A2UITextOverflow.CLIP -> TextOverflow.Clip
    A2UITextOverflow.ELLIPSIS -> TextOverflow.Ellipsis
    else -> TextOverflow.Clip
}