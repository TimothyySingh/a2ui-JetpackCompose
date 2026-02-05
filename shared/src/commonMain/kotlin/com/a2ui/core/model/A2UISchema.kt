package com.a2ui.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * A2UI Schema - Agent-to-UI specification
 * 
 * Defines the structure for declarative UI components that can be
 * rendered natively on Android and iOS via Compose Multiplatform.
 */

@Serializable
data class A2UIDocument(
    val version: String = "1.0",
    val root: A2UINode
)

@Serializable
data class A2UINode(
    val type: A2UINodeType,
    val id: String? = null,
    val props: A2UIProps? = null,
    val children: List<A2UINode>? = null,
    val actions: List<A2UIAction>? = null
)

@Serializable
enum class A2UINodeType {
    // Layout
    @SerialName("column") COLUMN,
    @SerialName("row") ROW,
    @SerialName("box") BOX,
    @SerialName("card") CARD,
    @SerialName("scaffold") SCAFFOLD,
    @SerialName("scrollable") SCROLLABLE,
    @SerialName("lazy_column") LAZY_COLUMN,
    @SerialName("lazy_row") LAZY_ROW,
    
    // Content
    @SerialName("text") TEXT,
    @SerialName("image") IMAGE,
    @SerialName("icon") ICON,
    @SerialName("spacer") SPACER,
    @SerialName("divider") DIVIDER,
    
    // Input
    @SerialName("button") BUTTON,
    @SerialName("text_field") TEXT_FIELD,
    @SerialName("checkbox") CHECKBOX,
    @SerialName("switch") SWITCH,
    @SerialName("slider") SLIDER,
    @SerialName("dropdown") DROPDOWN,
    
    // Feedback
    @SerialName("progress") PROGRESS,
    @SerialName("loading") LOADING,
    @SerialName("snackbar") SNACKBAR,
    
    // Navigation
    @SerialName("top_bar") TOP_BAR,
    @SerialName("bottom_bar") BOTTOM_BAR,
    @SerialName("nav_item") NAV_ITEM,
    @SerialName("fab") FAB,
    
    // Custom
    @SerialName("custom") CUSTOM
}

@Serializable
data class A2UIProps(
    // Text
    val text: String? = null,
    val hint: String? = null,
    val label: String? = null,
    
    // Styling
    val style: A2UITextStyle? = null,
    val background: String? = null,  // Color hex
    val padding: A2UIPadding? = null,
    val margin: A2UIPadding? = null,
    val cornerRadius: Int? = null,
    val elevation: Int? = null,
    
    // Layout
    val width: A2UIDimension? = null,
    val height: A2UIDimension? = null,
    val weight: Float? = null,
    val alignment: A2UIAlignment? = null,
    val arrangement: A2UIArrangement? = null,
    
    // Image/Icon
    val src: String? = null,  // URL or resource name
    val icon: String? = null,  // Material icon name
    val contentDescription: String? = null,
    
    // State
    val enabled: Boolean? = null,
    val checked: Boolean? = null,
    val value: JsonElement? = null,
    val progress: Float? = null,
    
    // Input
    val inputType: A2UIInputType? = null,
    val maxLines: Int? = null,
    val options: List<A2UIOption>? = null,
    
    // Custom data
    val data: JsonElement? = null
)

@Serializable
data class A2UITextStyle(
    val size: Int? = null,
    val color: String? = null,
    val weight: A2UIFontWeight? = null,
    val align: A2UITextAlign? = null,
    val maxLines: Int? = null,
    val overflow: A2UITextOverflow? = null
)

@Serializable
enum class A2UIFontWeight {
    @SerialName("thin") THIN,
    @SerialName("light") LIGHT,
    @SerialName("normal") NORMAL,
    @SerialName("medium") MEDIUM,
    @SerialName("semibold") SEMIBOLD,
    @SerialName("bold") BOLD
}

@Serializable
enum class A2UITextAlign {
    @SerialName("start") START,
    @SerialName("center") CENTER,
    @SerialName("end") END
}

@Serializable
enum class A2UITextOverflow {
    @SerialName("clip") CLIP,
    @SerialName("ellipsis") ELLIPSIS
}

@Serializable
data class A2UIPadding(
    val all: Int? = null,
    val horizontal: Int? = null,
    val vertical: Int? = null,
    val start: Int? = null,
    val end: Int? = null,
    val top: Int? = null,
    val bottom: Int? = null
)

@Serializable
data class A2UIDimension(
    val value: Int? = null,
    val type: A2UIDimensionType = A2UIDimensionType.DP
)

@Serializable
enum class A2UIDimensionType {
    @SerialName("dp") DP,
    @SerialName("fill") FILL,
    @SerialName("wrap") WRAP
}

@Serializable
enum class A2UIAlignment {
    @SerialName("start") START,
    @SerialName("center") CENTER,
    @SerialName("end") END,
    @SerialName("top") TOP,
    @SerialName("bottom") BOTTOM,
    @SerialName("center_horizontal") CENTER_HORIZONTAL,
    @SerialName("center_vertical") CENTER_VERTICAL
}

@Serializable
enum class A2UIArrangement {
    @SerialName("start") START,
    @SerialName("center") CENTER,
    @SerialName("end") END,
    @SerialName("space_between") SPACE_BETWEEN,
    @SerialName("space_around") SPACE_AROUND,
    @SerialName("space_evenly") SPACE_EVENLY
}

@Serializable
enum class A2UIInputType {
    @SerialName("text") TEXT,
    @SerialName("number") NUMBER,
    @SerialName("email") EMAIL,
    @SerialName("password") PASSWORD,
    @SerialName("phone") PHONE,
    @SerialName("multiline") MULTILINE
}

@Serializable
data class A2UIOption(
    val value: String,
    val label: String,
    val icon: String? = null
)

@Serializable
data class A2UIAction(
    val event: A2UIEventType,
    val handler: String,  // Handler ID for callback routing
    val payload: JsonElement? = null
)

@Serializable
enum class A2UIEventType {
    @SerialName("click") CLICK,
    @SerialName("long_click") LONG_CLICK,
    @SerialName("change") CHANGE,
    @SerialName("submit") SUBMIT,
    @SerialName("focus") FOCUS,
    @SerialName("blur") BLUR
}
