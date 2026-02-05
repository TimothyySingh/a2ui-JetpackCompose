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
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull

/**
 * A2UI Renderer - Converts A2UI schema to Compose UI
 */
@Composable
fun A2UIRenderer(
    document: A2UIDocument,
    onAction: (A2UIActionEvent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    RenderNode(
        node = document.root,
        onAction = onAction,
        modifier = modifier
    )
}

@Composable
fun RenderNode(
    node: A2UINode,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val nodeModifier = modifier
        .applyProps(node.props)
        .applyActions(node, onAction)
    
    when (node.type) {
        // Layout
        A2UINodeType.COLUMN -> RenderColumn(node, onAction, nodeModifier)
        A2UINodeType.ROW -> RenderRow(node, onAction, nodeModifier)
        A2UINodeType.BOX -> RenderBox(node, onAction, nodeModifier)
        A2UINodeType.CARD -> RenderCard(node, onAction, nodeModifier)
        A2UINodeType.SCAFFOLD -> RenderScaffold(node, onAction, nodeModifier)
        A2UINodeType.SCROLLABLE -> RenderScrollable(node, onAction, nodeModifier)
        A2UINodeType.LAZY_COLUMN -> RenderLazyColumn(node, onAction, nodeModifier)
        A2UINodeType.LAZY_ROW -> RenderLazyRow(node, onAction, nodeModifier)
        
        // Content
        A2UINodeType.TEXT -> RenderText(node, nodeModifier)
        A2UINodeType.IMAGE -> RenderImage(node, nodeModifier)
        A2UINodeType.ICON -> RenderIcon(node, nodeModifier)
        A2UINodeType.SPACER -> RenderSpacer(node, nodeModifier)
        A2UINodeType.DIVIDER -> HorizontalDivider(nodeModifier)
        
        // Input
        A2UINodeType.BUTTON -> RenderButton(node, onAction, nodeModifier)
        A2UINodeType.TEXT_FIELD -> RenderTextField(node, onAction, nodeModifier)
        A2UINodeType.CHECKBOX -> RenderCheckbox(node, onAction, nodeModifier)
        A2UINodeType.SWITCH -> RenderSwitch(node, onAction, nodeModifier)
        A2UINodeType.SLIDER -> RenderSlider(node, onAction, nodeModifier)
        A2UINodeType.DROPDOWN -> RenderDropdown(node, onAction, nodeModifier)
        
        // Feedback
        A2UINodeType.PROGRESS -> RenderProgress(node, nodeModifier)
        A2UINodeType.LOADING -> RenderLoading(nodeModifier)
        A2UINodeType.SNACKBAR -> { /* Handled at scaffold level */ }
        
        // Navigation
        A2UINodeType.TOP_BAR -> RenderTopBar(node, onAction, nodeModifier)
        A2UINodeType.BOTTOM_BAR -> RenderBottomBar(node, onAction, nodeModifier)
        A2UINodeType.NAV_ITEM -> { /* Rendered by parent */ }
        A2UINodeType.FAB -> RenderFab(node, onAction, nodeModifier)
        
        // Custom
        A2UINodeType.CUSTOM -> RenderCustom(node, onAction, nodeModifier)
    }
}

// Layout Components

@Composable
private fun RenderColumn(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = node.props?.alignment.toHorizontalAlignment(),
        verticalArrangement = node.props?.arrangement.toVerticalArrangement()
    ) {
        node.children?.forEach { child ->
            RenderNode(child, onAction, Modifier.maybeWeight(child.props?.weight))
        }
    }
}

@Composable
private fun RenderRow(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = node.props?.alignment.toVerticalAlignment(),
        horizontalArrangement = node.props?.arrangement.toHorizontalArrangement()
    ) {
        node.children?.forEach { child ->
            RenderNode(child, onAction, Modifier.maybeWeight(child.props?.weight))
        }
    }
}

@Composable
private fun RenderBox(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    Box(modifier = modifier) {
        node.children?.forEach { child ->
            RenderNode(child, onAction)
        }
    }
}

@Composable
private fun RenderCard(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = (node.props?.elevation ?: 4).dp
        ),
        shape = RoundedCornerShape((node.props?.cornerRadius ?: 8).dp)
    ) {
        node.children?.forEach { child ->
            RenderNode(child, onAction)
        }
    }
}

@Composable
private fun RenderScaffold(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
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
        topBar = { topBar?.let { RenderTopBar(it, onAction) } },
        bottomBar = { bottomBar?.let { RenderBottomBar(it, onAction) } },
        floatingActionButton = { fab?.let { RenderFab(it, onAction) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            content?.forEach { child ->
                RenderNode(child, onAction)
            }
        }
    }
}

@Composable
private fun RenderScrollable(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        node.children?.forEach { child ->
            RenderNode(child, onAction)
        }
    }
}

@Composable
private fun RenderLazyColumn(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    LazyColumn(modifier = modifier) {
        items(node.children ?: emptyList()) { child ->
            RenderNode(child, onAction)
        }
    }
}

@Composable
private fun RenderLazyRow(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    LazyRow(modifier = modifier) {
        items(node.children ?: emptyList()) { child ->
            RenderNode(child, onAction)
        }
    }
}

// Content Components

@Composable
private fun RenderText(node: A2UINode, modifier: Modifier) {
    val style = node.props?.style
    Text(
        text = node.props?.text ?: "",
        modifier = modifier,
        fontSize = (style?.size ?: 14).sp,
        fontWeight = style?.weight.toFontWeight(),
        color = style?.color?.toColor() ?: Color.Unspecified,
        textAlign = style?.align.toTextAlign(),
        maxLines = style?.maxLines ?: Int.MAX_VALUE,
        overflow = style?.overflow.toTextOverflow()
    )
}

@Composable
private fun RenderImage(node: A2UINode, modifier: Modifier) {
    // TODO: Implement async image loading with Coil or similar
    Box(
        modifier = modifier
            .background(Color.Gray.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text("Image: ${node.props?.src ?: "?"}")
    }
}

@Composable
private fun RenderIcon(node: A2UINode, modifier: Modifier) {
    // TODO: Map icon names to Material icons
    Text(
        text = "⬤", // Placeholder
        modifier = modifier
    )
}

@Composable
private fun RenderSpacer(node: A2UINode, modifier: Modifier) {
    Spacer(modifier = modifier)
}

// Input Components

@Composable
private fun RenderButton(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    val clickAction = node.actions?.find { it.event == A2UIEventType.CLICK }
    
    Button(
        onClick = {
            clickAction?.let {
                onAction(A2UIActionEvent(node.id, it.handler, it.payload))
            }
        },
        modifier = modifier,
        enabled = node.props?.enabled ?: true
    ) {
        Text(node.props?.text ?: "Button")
    }
}

@Composable
private fun RenderTextField(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
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
        maxLines = node.props?.maxLines ?: 1
    )
}

@Composable
private fun RenderCheckbox(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
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
private fun RenderSwitch(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
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
private fun RenderSlider(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
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
private fun RenderDropdown(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
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

// Feedback Components

@Composable
private fun RenderProgress(node: A2UINode, modifier: Modifier) {
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
private fun RenderLoading(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// Navigation Components

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenderTopBar(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier = Modifier) {
    TopAppBar(
        title = { Text(node.props?.text ?: "") },
        modifier = modifier
    )
}

@Composable
private fun RenderBottomBar(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier) {
        node.children?.forEach { navItem ->
            val clickAction = navItem.actions?.find { it.event == A2UIEventType.CLICK }
            NavigationBarItem(
                icon = { Text("•") }, // TODO: Icon mapping
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
private fun RenderFab(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier = Modifier) {
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

// Custom Component

@Composable
private fun RenderCustom(node: A2UINode, onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) {
    // Placeholder for custom component rendering
    Box(
        modifier = modifier
            .background(Color.LightGray.copy(alpha = 0.3f))
            .padding(8.dp)
    ) {
        Text("Custom: ${node.id ?: "unknown"}")
    }
}

// Action Event

data class A2UIActionEvent(
    val nodeId: String?,
    val handler: String,
    val payload: kotlinx.serialization.json.JsonElement?
)

// Extension Functions

private fun Modifier.applyProps(props: A2UIProps?): Modifier {
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
    
    // Padding
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
    
    // Background
    props.background?.let { bg ->
        mod = mod.background(bg.toColor())
    }
    
    // Corner radius
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

private fun String.toColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(if (startsWith("#")) this else "#$this"))
    } catch (e: Exception) {
        Color.Unspecified
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
