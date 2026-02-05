package com.a2ui.core.render

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a2ui.core.model.*
import com.a2ui.core.resolve.*
import kotlinx.serialization.json.*

/**
 * A2UI v0.9 Renderer - Converts A2UISurface to Compose UI via flat component map lookup.
 */
@Composable
fun A2UIRenderer(
    surface: A2UISurface,
    onAction: (A2UIActionEvent) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val resolver = remember(surface.data) {
        DynamicResolver(ResolverContext(surface.data))
    }

    RenderComponent(
        componentId = surface.root,
        surface = surface,
        resolver = resolver,
        onAction = onAction,
        modifier = modifier
    )
}

@Composable
fun RenderComponent(
    componentId: String,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val component = surface.components[componentId] ?: return

    when (component.component) {
        // Content
        "Text" -> RenderText(component, resolver, modifier)
        "Image" -> RenderImage(component, resolver, modifier)
        "Icon" -> RenderIcon(component, resolver, modifier)
        "Video" -> RenderVideo(component, resolver, modifier)
        "AudioPlayer" -> RenderAudioPlayer(component, resolver, onAction, modifier)

        // Layout
        "Row" -> RenderRow(component, surface, resolver, onAction, modifier)
        "Column" -> RenderColumn(component, surface, resolver, onAction, modifier)
        "Card" -> RenderCard(component, surface, resolver, onAction, modifier)
        "List" -> RenderList(component, surface, resolver, onAction, modifier)
        "Tabs" -> RenderTabs(component, surface, resolver, onAction, modifier)
        "Modal" -> RenderModal(component, surface, resolver, onAction, modifier)
        "Divider" -> RenderDivider(component, modifier)

        // Input
        "Button" -> RenderButton(component, surface, resolver, onAction, modifier)
        "TextField" -> RenderTextField(component, resolver, onAction, modifier)
        "CheckBox" -> RenderCheckBox(component, resolver, onAction, modifier)
        "ChoicePicker" -> RenderChoicePicker(component, resolver, onAction, modifier)
        "Slider" -> RenderSlider(component, resolver, onAction, modifier)
        "DateTimeInput" -> RenderDateTimeInput(component, resolver, onAction, modifier)

        // Extension components (non-spec, PascalCase)
        "Scaffold" -> RenderScaffold(component, surface, resolver, onAction, modifier)
        "Box" -> RenderBox(component, surface, resolver, onAction, modifier)
        "Scrollable" -> RenderScrollable(component, surface, resolver, onAction, modifier)
        "LazyColumn" -> RenderLazyColumnExt(component, surface, resolver, onAction, modifier)
        "LazyRow" -> RenderLazyRowExt(component, surface, resolver, onAction, modifier)
        "Spacer" -> RenderSpacer(component, resolver, modifier)
        "Switch" -> RenderSwitch(component, resolver, onAction, modifier)
        "Dropdown" -> RenderDropdown(component, resolver, onAction, modifier)
        "Progress" -> RenderProgress(component, resolver, modifier)
        "Loading" -> RenderLoading(modifier)
        "TopBar" -> RenderTopBar(component, resolver, modifier)
        "BottomBar" -> RenderBottomBar(component, surface, resolver, onAction, modifier)
        "Fab" -> RenderFab(component, resolver, onAction, modifier)
    }
}

// --- Helper: Resolve children and render ---

@Composable
private fun RenderChildren(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit
) {
    val childResolver = ChildListResolver(resolver)
    val children = childResolver.resolve(component.children, surface.components)
    for (child in children) {
        val childResolver2 = if (child.scopedData != null) resolver.withScopedData(child.scopedData) else resolver
        RenderComponent(child.componentId, surface, childResolver2, onAction)
    }
}

// --- Content Components ---

@Composable
private fun RenderText(component: A2UIComponent, resolver: DynamicResolver, modifier: Modifier) {
    val text = component.text(resolver) ?: ""
    val variant = component.variant()

    val (fontSize, fontWeight) = when (variant) {
        "h1" -> 32.sp to FontWeight.Bold
        "h2" -> 28.sp to FontWeight.Bold
        "h3" -> 24.sp to FontWeight.Bold
        "h4" -> 20.sp to FontWeight.SemiBold
        "h5" -> 18.sp to FontWeight.SemiBold
        "caption" -> 12.sp to FontWeight.Normal
        "overline" -> 10.sp to FontWeight.Normal
        else -> 16.sp to FontWeight.Normal // body
    }

    Text(
        text = text,
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = Color.Unspecified
    )
}

@Composable
private fun RenderImage(component: A2UIComponent, resolver: DynamicResolver, modifier: Modifier) {
    val url = component.url(resolver)
    val alt = component.altText(resolver) ?: ""
    val variant = component.variant()

    val size = when (variant) {
        "icon" -> 24.dp
        "avatar" -> 48.dp
        "smallFeature" -> 80.dp
        "mediumFeature" -> 160.dp
        "largeFeature" -> 240.dp
        "header" -> 200.dp
        else -> 120.dp
    }

    val shape = when (variant) {
        "avatar" -> RoundedCornerShape(50)
        else -> RoundedCornerShape(8.dp)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(Color.Gray.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (url != null) "IMG" else alt.take(3),
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun RenderIcon(component: A2UIComponent, resolver: DynamicResolver, modifier: Modifier) {
    val iconName = component.properties["icon"]?.jsonPrimitive?.contentOrNull
        ?: component.text(resolver) ?: ""

    val icon = mapNameToIcon(iconName)
    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = component.accessibility?.label ?: iconName,
            modifier = modifier.size(24.dp)
        )
    } else {
        Text(
            text = iconName.take(3),
            modifier = modifier,
            fontSize = 14.sp
        )
    }
}

private fun mapNameToIcon(name: String): ImageVector? = when (name.lowercase()) {
    "home" -> Icons.Default.Home
    "search" -> Icons.Default.Search
    "settings" -> Icons.Default.Settings
    "person", "account" -> Icons.Default.Person
    "favorite", "heart" -> Icons.Default.Favorite
    "star" -> Icons.Default.Star
    "add", "plus" -> Icons.Default.Add
    "delete", "trash" -> Icons.Default.Delete
    "edit", "pencil" -> Icons.Default.Edit
    "close", "x" -> Icons.Default.Close
    "check", "done" -> Icons.Default.Check
    "info" -> Icons.Default.Info
    "warning" -> Icons.Default.Warning
    "error" -> Icons.Default.Warning
    "email", "mail" -> Icons.Default.Email
    "phone", "call" -> Icons.Default.Phone
    "share" -> Icons.Default.Share
    "menu" -> Icons.Default.Menu
    "back", "arrowback" -> Icons.Default.ArrowBack
    "forward", "arrowforward" -> Icons.Default.ArrowForward
    "refresh" -> Icons.Default.Refresh
    "lock" -> Icons.Default.Lock
    "notification", "notifications" -> Icons.Default.Notifications
    "calendar", "date" -> Icons.Default.DateRange
    "location", "place" -> Icons.Default.LocationOn
    "play" -> Icons.Default.PlayArrow
    else -> null
}

@Composable
private fun RenderVideo(component: A2UIComponent, resolver: DynamicResolver, modifier: Modifier) {
    val url = component.url(resolver)
    val desc = component.description(resolver) ?: "Video"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Black.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(48.dp),
                    tint = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(desc, color = Color.White, fontSize = 14.sp)
                if (url != null) {
                    Text(url, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun RenderAudioPlayer(
    component: A2UIComponent,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val url = component.url(resolver)
    val desc = component.description(resolver) ?: "Audio"
    var isPlaying by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(desc, fontWeight = FontWeight.Medium)
                if (url != null) {
                    Text(url, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

// --- Layout Components ---

@Composable
private fun RenderRow(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = component.justify().toHorizontalArrangement(),
        verticalAlignment = component.align().toVerticalAlignment()
    ) {
        val childResolver = ChildListResolver(resolver)
        val children = childResolver.resolve(component.children, surface.components)
        for (child in children) {
            val childComponent = surface.components[child.componentId]
            val childWeight = childComponent?.resolvedWeight(resolver)
            val childMod = if (childWeight != null) Modifier.weight(childWeight) else Modifier
            val cr = if (child.scopedData != null) resolver.withScopedData(child.scopedData) else resolver
            RenderComponent(child.componentId, surface, cr, onAction, childMod)
        }
    }
}

@Composable
private fun RenderColumn(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = component.justify().toVerticalArrangement(),
        horizontalAlignment = component.align().toHorizontalAlignment()
    ) {
        val childResolver = ChildListResolver(resolver)
        val children = childResolver.resolve(component.children, surface.components)
        for (child in children) {
            val childComponent = surface.components[child.componentId]
            val childWeight = childComponent?.resolvedWeight(resolver)
            val childMod = if (childWeight != null) Modifier.weight(childWeight) else Modifier
            val cr = if (child.scopedData != null) resolver.withScopedData(child.scopedData) else resolver
            RenderComponent(child.componentId, surface, cr, onAction, childMod)
        }
    }
}

@Composable
private fun RenderCard(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        val childId = component.childId()
        if (childId != null) {
            RenderComponent(childId, surface, resolver, onAction, Modifier.padding(16.dp))
        } else {
            Column(Modifier.padding(16.dp)) {
                RenderChildren(component, surface, resolver, onAction)
            }
        }
    }
}

@Composable
private fun RenderList(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val direction = component.direction()
    val childResolver = ChildListResolver(resolver)
    val children = childResolver.resolve(component.children, surface.components)

    if (direction == "horizontal") {
        LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(children) { child ->
                val cr = if (child.scopedData != null) resolver.withScopedData(child.scopedData) else resolver
                RenderComponent(child.componentId, surface, cr, onAction)
            }
        }
    } else {
        LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(children) { child ->
                val cr = if (child.scopedData != null) resolver.withScopedData(child.scopedData) else resolver
                RenderComponent(child.componentId, surface, cr, onAction)
            }
        }
    }
}

@Composable
private fun RenderTabs(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val tabs = component.tabs()
    if (tabs.isEmpty()) return

    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = modifier) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(tab.title) }
                )
            }
        }
        val currentTab = tabs.getOrNull(selectedTab)
        if (currentTab != null) {
            RenderComponent(currentTab.childId, surface, resolver, onAction)
        }
    }
}

@Composable
private fun RenderModal(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val trigId = component.triggerId()
    val contId = component.contentId()
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Render trigger
        if (trigId != null) {
            Box(Modifier.clickable { showDialog = true }) {
                RenderComponent(trigId, surface, resolver, onAction)
            }
        }

        // Show dialog content
        if (showDialog && contId != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Close")
                    }
                },
                text = {
                    RenderComponent(contId, surface, resolver, onAction)
                }
            )
        }
    }
}

@Composable
private fun RenderDivider(component: A2UIComponent, modifier: Modifier) {
    val axis = component.axis()
    if (axis == "vertical") {
        VerticalDivider(modifier = modifier)
    } else {
        HorizontalDivider(modifier = modifier)
    }
}

// --- Input Components ---

@Composable
private fun RenderButton(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val action = component.action()
    val variant = component.variant()
    val enabled = component.isEnabled(resolver)

    val onClick: () -> Unit = {
        if (action != null) {
            if (action.name != null) {
                onAction(
                    A2UIActionEvent(
                        componentId = component.id,
                        actionName = action.name,
                        context = action.context
                    )
                )
            }
        }
    }

    when (variant) {
        "borderless" -> {
            TextButton(onClick = onClick, modifier = modifier, enabled = enabled) {
                val childId = component.childId()
                if (childId != null) {
                    RenderComponent(childId, surface, resolver, onAction)
                } else {
                    Text(component.text(resolver) ?: "Button")
                }
            }
        }
        else -> {
            Button(onClick = onClick, modifier = modifier, enabled = enabled) {
                val childId = component.childId()
                if (childId != null) {
                    RenderComponent(childId, surface, resolver, onAction)
                } else {
                    Text(component.text(resolver) ?: "Button")
                }
            }
        }
    }
}

@Composable
private fun RenderTextField(
    component: A2UIComponent,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val initialValue = resolver.resolveString(component.properties["value"]) ?: ""
    var text by remember { mutableStateOf(initialValue) }
    val label = component.label(resolver)
    val placeholder = component.placeholder(resolver)
    val enabled = component.isEnabled(resolver)
    val variant = component.variant()
    val action = component.action()

    val validationEngine = remember(resolver) { ValidationEngine(resolver) }
    val validation = validationEngine.validate(component.checks)

    val maxLines = when (variant) {
        "longText" -> 5
        else -> 1
    }

    OutlinedTextField(
        value = text,
        onValueChange = { newValue ->
            text = newValue
            if (action?.name != null) {
                onAction(
                    A2UIActionEvent(
                        componentId = component.id,
                        actionName = action.name,
                        value = JsonPrimitive(newValue)
                    )
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        enabled = enabled,
        maxLines = maxLines,
        isError = !validation.isValid,
        supportingText = if (!validation.isValid) {
            { Text(validation.errors.first(), color = MaterialTheme.colorScheme.error) }
        } else null,
        singleLine = variant != "longText"
    )
}

@Composable
private fun RenderCheckBox(
    component: A2UIComponent,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val initialValue = resolver.resolveBoolean(component.properties["value"]) ?: false
    var checked by remember { mutableStateOf(initialValue) }
    val label = component.label(resolver)
    val enabled = component.isEnabled(resolver)
    val action = component.action()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { newValue ->
                checked = newValue
                if (action?.name != null) {
                    onAction(
                        A2UIActionEvent(
                            componentId = component.id,
                            actionName = action.name,
                            value = JsonPrimitive(newValue)
                        )
                    )
                }
            },
            enabled = enabled
        )
        if (label != null) {
            Text(label, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun RenderChoicePicker(
    component: A2UIComponent,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val options = component.options(resolver)
    val selectionMode = component.selectionMode()
    val label = component.label(resolver)
    val action = component.action()

    val initialValue = resolver.resolveString(component.properties["value"]) ?: ""
    var selectedValue by remember { mutableStateOf(initialValue) }

    val initialMulti = resolver.resolveStringList(component.properties["value"]) ?: emptyList()
    var selectedValues by remember { mutableStateOf(initialMulti.toSet()) }

    Column(modifier = modifier) {
        if (label != null) {
            Text(label, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (selectionMode == "multipleSelection") {
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedValues = if (option.value in selectedValues)
                                selectedValues - option.value
                            else
                                selectedValues + option.value
                            if (action?.name != null) {
                                onAction(
                                    A2UIActionEvent(
                                        componentId = component.id,
                                        actionName = action.name,
                                        value = JsonArray(selectedValues.map { JsonPrimitive(it) })
                                    )
                                )
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = option.value in selectedValues,
                        onCheckedChange = null
                    )
                    Text(option.label, modifier = Modifier.padding(start = 8.dp))
                }
            }
        } else {
            // mutuallyExclusive (radio)
            options.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = option.value == selectedValue,
                            onClick = {
                                selectedValue = option.value
                                if (action?.name != null) {
                                    onAction(
                                        A2UIActionEvent(
                                            componentId = component.id,
                                            actionName = action.name,
                                            value = JsonPrimitive(option.value)
                                        )
                                    )
                                }
                            }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = option.value == selectedValue,
                        onClick = null
                    )
                    Text(option.label, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun RenderSlider(
    component: A2UIComponent,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val min = component.min(resolver)
    val max = component.max(resolver)
    val initialValue = resolver.resolveFloat(component.properties["value"]) ?: min
    var value by remember { mutableStateOf(initialValue) }
    val label = component.label(resolver)
    val enabled = component.isEnabled(resolver)
    val action = component.action()

    Column(modifier = modifier) {
        if (label != null) {
            Text(label, modifier = Modifier.padding(bottom = 4.dp))
        }
        Slider(
            value = value,
            onValueChange = { newValue ->
                value = newValue
                if (action?.name != null) {
                    onAction(
                        A2UIActionEvent(
                            componentId = component.id,
                            actionName = action.name,
                            value = JsonPrimitive(newValue)
                        )
                    )
                }
            },
            valueRange = min..max,
            enabled = enabled
        )
    }
}

@Composable
private fun RenderDateTimeInput(
    component: A2UIComponent,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val label = component.label(resolver)
    val initialValue = resolver.resolveString(component.properties["value"]) ?: ""
    var text by remember { mutableStateOf(initialValue) }
    val enabled = component.isEnabled(resolver)
    val action = component.action()
    val enableDate = component.enableDate()
    val enableTime = component.enableTime()

    val hint = when {
        enableDate && enableTime -> "YYYY-MM-DD HH:MM"
        enableTime -> "HH:MM"
        else -> "YYYY-MM-DD"
    }

    OutlinedTextField(
        value = text,
        onValueChange = { newValue ->
            text = newValue
            if (action?.name != null) {
                onAction(
                    A2UIActionEvent(
                        componentId = component.id,
                        actionName = action.name,
                        value = JsonPrimitive(newValue)
                    )
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it) } },
        placeholder = { Text(hint) },
        enabled = enabled,
        singleLine = true,
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Date/Time")
        }
    )
}

// --- Extension Components ---

@Composable
private fun RenderScaffold(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    // Look for TopBar, BottomBar, Fab children
    val childResolver = ChildListResolver(resolver)
    val children = childResolver.resolve(component.children, surface.components)

    val topBarChild = children.firstOrNull { id ->
        surface.components[id.componentId]?.component == "TopBar"
    }
    val bottomBarChild = children.firstOrNull { id ->
        surface.components[id.componentId]?.component == "BottomBar"
    }
    val fabChild = children.firstOrNull { id ->
        surface.components[id.componentId]?.component == "Fab"
    }
    val content = children.filter { id ->
        val comp = surface.components[id.componentId]?.component
        comp != "TopBar" && comp != "BottomBar" && comp != "Fab"
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            topBarChild?.let { child ->
                RenderComponent(child.componentId, surface, resolver, onAction)
            }
        },
        bottomBar = {
            bottomBarChild?.let { child ->
                RenderComponent(child.componentId, surface, resolver, onAction)
            }
        },
        floatingActionButton = {
            fabChild?.let { child ->
                RenderComponent(child.componentId, surface, resolver, onAction)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            for (child in content) {
                RenderComponent(child.componentId, surface, resolver, onAction)
            }
        }
    }
}

@Composable
private fun RenderBox(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        RenderChildren(component, surface, resolver, onAction)
    }
}

@Composable
private fun RenderScrollable(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        RenderChildren(component, surface, resolver, onAction)
    }
}

@Composable
private fun RenderLazyColumnExt(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val childResolver = ChildListResolver(resolver)
    val children = childResolver.resolve(component.children, surface.components)

    LazyColumn(modifier = modifier) {
        items(children) { child ->
            val cr = if (child.scopedData != null) resolver.withScopedData(child.scopedData) else resolver
            RenderComponent(child.componentId, surface, cr, onAction)
        }
    }
}

@Composable
private fun RenderLazyRowExt(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val childResolver = ChildListResolver(resolver)
    val children = childResolver.resolve(component.children, surface.components)

    LazyRow(modifier = modifier) {
        items(children) { child ->
            val cr = if (child.scopedData != null) resolver.withScopedData(child.scopedData) else resolver
            RenderComponent(child.componentId, surface, cr, onAction)
        }
    }
}

@Composable
private fun RenderSpacer(component: A2UIComponent, resolver: DynamicResolver, modifier: Modifier) {
    val height = resolver.resolveFloat(component.properties["height"])
    val width = resolver.resolveFloat(component.properties["width"])
    var mod = modifier
    if (height != null) mod = mod.height(height.dp)
    if (width != null) mod = mod.width(width.dp)
    Spacer(modifier = mod)
}

@Composable
private fun RenderSwitch(
    component: A2UIComponent,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val initialValue = resolver.resolveBoolean(component.properties["value"]) ?: false
    var checked by remember { mutableStateOf(initialValue) }
    val label = component.label(resolver)
    val enabled = component.isEnabled(resolver)
    val action = component.action()

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (label != null) {
            Text(label, modifier = Modifier.weight(1f))
        }
        Switch(
            checked = checked,
            onCheckedChange = { newValue ->
                checked = newValue
                if (action?.name != null) {
                    onAction(
                        A2UIActionEvent(
                            componentId = component.id,
                            actionName = action.name,
                            value = JsonPrimitive(newValue)
                        )
                    )
                }
            },
            enabled = enabled
        )
    }
}

@Composable
private fun RenderDropdown(
    component: A2UIComponent,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val options = component.options(resolver)
    val initialValue = resolver.resolveString(component.properties["value"]) ?: ""
    var selectedOption by remember { mutableStateOf(initialValue) }
    var expanded by remember { mutableStateOf(false) }
    val action = component.action()

    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Text(
                options.find { it.value == selectedOption }?.label
                    ?: component.placeholder(resolver)
                    ?: "Select..."
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        selectedOption = option.value
                        expanded = false
                        if (action?.name != null) {
                            onAction(
                                A2UIActionEvent(
                                    componentId = component.id,
                                    actionName = action.name,
                                    value = JsonPrimitive(option.value)
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RenderProgress(component: A2UIComponent, resolver: DynamicResolver, modifier: Modifier) {
    val progress = resolver.resolveFloat(component.properties["progress"])
    if (progress != null) {
        LinearProgressIndicator(progress = { progress }, modifier = modifier.fillMaxWidth())
    } else {
        LinearProgressIndicator(modifier = modifier.fillMaxWidth())
    }
}

@Composable
private fun RenderLoading(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenderTopBar(component: A2UIComponent, resolver: DynamicResolver, modifier: Modifier) {
    TopAppBar(
        title = { Text(component.text(resolver) ?: "") },
        modifier = modifier
    )
}

@Composable
private fun RenderBottomBar(
    component: A2UIComponent,
    surface: A2UISurface,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    NavigationBar(modifier = modifier) {
        val childResolver = ChildListResolver(resolver)
        val children = childResolver.resolve(component.children, surface.components)
        children.forEach { child ->
            val navComponent = surface.components[child.componentId] ?: return@forEach
            val action = navComponent.action()
            NavigationBarItem(
                icon = { Text("\u2022") },
                label = { Text(navComponent.label(resolver) ?: "") },
                selected = resolver.resolveBoolean(navComponent.properties["selected"]) ?: false,
                onClick = {
                    if (action?.name != null) {
                        onAction(
                            A2UIActionEvent(
                                componentId = navComponent.id,
                                actionName = action.name,
                                context = action.context
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun RenderFab(
    component: A2UIComponent,
    resolver: DynamicResolver,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    val action = component.action()
    FloatingActionButton(
        onClick = {
            if (action?.name != null) {
                onAction(
                    A2UIActionEvent(
                        componentId = component.id,
                        actionName = action.name,
                        context = action.context
                    )
                )
            }
        },
        modifier = modifier
    ) {
        val icon = component.properties["icon"]?.jsonPrimitive?.contentOrNull
        Text(icon ?: "+")
    }
}

// --- Arrangement/Alignment Helpers ---

private fun String?.toHorizontalArrangement(): Arrangement.Horizontal = when (this) {
    "start" -> Arrangement.Start
    "center" -> Arrangement.Center
    "end" -> Arrangement.End
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceAround" -> Arrangement.SpaceAround
    "spaceEvenly" -> Arrangement.SpaceEvenly
    else -> Arrangement.Start
}

private fun String?.toVerticalArrangement(): Arrangement.Vertical = when (this) {
    "start" -> Arrangement.Top
    "center" -> Arrangement.Center
    "end" -> Arrangement.Bottom
    "spaceBetween" -> Arrangement.SpaceBetween
    "spaceAround" -> Arrangement.SpaceAround
    "spaceEvenly" -> Arrangement.SpaceEvenly
    else -> Arrangement.Top
}

private fun String?.toHorizontalAlignment(): Alignment.Horizontal = when (this) {
    "start" -> Alignment.Start
    "center" -> Alignment.CenterHorizontally
    "end" -> Alignment.End
    else -> Alignment.Start
}

private fun String?.toVerticalAlignment(): Alignment.Vertical = when (this) {
    "start" -> Alignment.Top
    "center" -> Alignment.CenterVertically
    "end" -> Alignment.Bottom
    else -> Alignment.Top
}
