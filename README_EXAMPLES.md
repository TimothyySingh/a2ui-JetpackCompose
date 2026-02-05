# A2UI Mobile - Real-World Examples

This guide shows how to handle actual A2UI responses from your agent and configure custom components accordingly.

## 📝 Table of Contents

1. [Basic Form Response](#1-basic-form-response)
2. [Dashboard with Cards](#2-dashboard-with-cards)
3. [Chat Interface](#3-chat-interface)
4. [Settings Screen](#4-settings-screen)
5. [E-commerce Product List](#5-e-commerce-product-list)
6. [Multi-step Wizard](#6-multi-step-wizard)

---

## 1. Basic Form Response

### A2UI Response from Agent:
```json
{
  "version": "0.8",
  "root": {
    "type": "scaffold",
    "children": [
      {
        "type": "top_bar",
        "props": { "text": "Contact Form" }
      },
      {
        "type": "column",
        "props": {
          "padding": { "all": 16 },
          "arrangement": "space_between"
        },
        "children": [
          {
            "id": "name_field",
            "type": "text_field",
            "props": {
              "label": "Full Name",
              "hint": "Enter your full name",
              "required": true
            },
            "actions": [
              {
                "event": "change",
                "handler": "updateName"
              }
            ]
          },
          {
            "id": "email_field",
            "type": "text_field",
            "props": {
              "label": "Email",
              "hint": "your@email.com",
              "inputType": "email",
              "required": true
            },
            "actions": [
              {
                "event": "change",
                "handler": "updateEmail"
              }
            ]
          },
          {
            "id": "message_field",
            "type": "text_field",
            "props": {
              "label": "Message",
              "hint": "Type your message here",
              "maxLines": 5,
              "multiline": true
            },
            "actions": [
              {
                "event": "change",
                "handler": "updateMessage"
              }
            ]
          },
          {
            "type": "row",
            "props": {
              "arrangement": "end",
              "padding": { "top": 16 }
            },
            "children": [
              {
                "id": "cancel_btn",
                "type": "button",
                "props": {
                  "text": "Cancel",
                  "variant": "outlined"
                },
                "actions": [
                  {
                    "event": "click",
                    "handler": "cancelForm"
                  }
                ]
              },
              {
                "type": "spacer",
                "props": { "width": { "type": "dp", "value": 8 } }
              },
              {
                "id": "submit_btn",
                "type": "button",
                "props": {
                  "text": "Submit",
                  "variant": "filled",
                  "enabled": false
                },
                "actions": [
                  {
                    "event": "click",
                    "handler": "submitForm",
                    "payload": { "formId": "contact" }
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
}
```

### How to Configure Custom Components:

```kotlin
@Composable
fun ContactFormApp() {
    // Track form data
    val formData = remember { mutableStateMapOf<String, String>() }
    val isFormValid = remember { derivedStateOf {
        formData["name"]?.isNotEmpty() == true &&
        formData["email"]?.contains("@") == true
    }}
    
    // Create custom registry for form components
    val customRegistry = remember {
        ComponentRegistry().apply {
            // Custom text field with validation
            register(A2UINodeType.TEXT_FIELD.name) { node, onAction, modifier ->
                CustomFormTextField(
                    node = node,
                    value = formData[node.id] ?: "",
                    onValueChange = { value ->
                        formData[node.id ?: ""] = value
                        node.actions?.find { it.event == A2UIEventType.CHANGE }?.let {
                            onAction(A2UIActionEvent(node.id, it.handler, JsonPrimitive(value)))
                        }
                    },
                    isError = node.props?.required == true && 
                              formData[node.id]?.isEmpty() == true,
                    modifier = modifier
                )
            }
            
            // Custom submit button that knows about form validity
            register(A2UINodeType.BUTTON.name) { node, onAction, modifier ->
                when (node.props?.variant) {
                    "outlined" -> OutlinedButton(
                        onClick = {
                            node.actions?.find { it.event == A2UIEventType.CLICK }?.let {
                                onAction(A2UIActionEvent(node.id, it.handler, it.payload))
                            }
                        },
                        modifier = modifier
                    ) {
                        Text(node.props?.text ?: "")
                    }
                    else -> Button(
                        onClick = {
                            if (node.id == "submit_btn") {
                                // Include form data in submission
                                val payload = JsonObject(
                                    mapOf(
                                        "formId" to JsonPrimitive("contact"),
                                        "data" to JsonObject(
                                            formData.mapValues { JsonPrimitive(it.value) }
                                        )
                                    )
                                )
                                node.actions?.find { it.event == A2UIEventType.CLICK }?.let {
                                    onAction(A2UIActionEvent(node.id, it.handler, payload))
                                }
                            } else {
                                node.actions?.find { it.event == A2UIEventType.CLICK }?.let {
                                    onAction(A2UIActionEvent(node.id, it.handler, it.payload))
                                }
                            }
                        },
                        enabled = if (node.id == "submit_btn") isFormValid.value 
                                 else node.props?.enabled ?: true,
                        modifier = modifier
                    ) {
                        Text(node.props?.text ?: "")
                    }
                }
            }
        }
    }
    
    // Custom theme for forms
    val formTheme = buildA2UITheme {
        colors {
            copy(
                primary = Color(0xFF2196F3),
                error = Color(0xFFF44336)
            )
        }
        components {
            copy(
                textField = textField.copy(
                    padding = 12.dp,
                    cornerRadius = 8.dp
                ),
                button = button.copy(
                    minHeight = 48.dp,
                    cornerRadius = 8.dp
                )
            )
        }
    }
    
    // Parse and render
    val document = A2UIParser.parse(formJsonResponse)
    
    A2UIProvider(
        componentRegistry = customRegistry,
        theme = formTheme
    ) {
        A2UIExtendedRenderer(
            document = document,
            onAction = { event ->
                when (event.handler) {
                    "submitForm" -> {
                        val formData = (event.payload as? JsonObject)
                            ?.get("data") as? JsonObject
                        submitToServer(formData)
                    }
                    "cancelForm" -> navigateBack()
                }
            }
        )
    }
}

@Composable
fun CustomFormTextField(
    node: A2UINode,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(node.props?.label ?: "") },
        placeholder = { Text(node.props?.hint ?: "") },
        isError = isError,
        singleLine = node.props?.multiline != true,
        maxLines = node.props?.maxLines ?: 1,
        keyboardOptions = KeyboardOptions(
            keyboardType = when (node.props?.inputType) {
                "email" -> KeyboardType.Email
                "number" -> KeyboardType.Number
                "phone" -> KeyboardType.Phone
                else -> KeyboardType.Text
            }
        ),
        modifier = modifier.fillMaxWidth()
    )
}
```

---

## 2. Dashboard with Cards

### A2UI Response from Agent:
```json
{
  "version": "0.8",
  "root": {
    "type": "scaffold",
    "children": [
      {
        "type": "top_bar",
        "props": { "text": "Analytics Dashboard" }
      },
      {
        "type": "scrollable",
        "children": [
          {
            "type": "column",
            "props": { "padding": { "all": 16 } },
            "children": [
              {
                "type": "row",
                "props": { "arrangement": "space_between" },
                "children": [
                  {
                    "type": "card",
                    "props": {
                      "elevation": 4,
                      "cornerRadius": 12,
                      "weight": 1,
                      "customType": "stat_card"
                    },
                    "children": [
                      {
                        "type": "column",
                        "props": { "padding": { "all": 16 } },
                        "children": [
                          {
                            "type": "text",
                            "props": {
                              "text": "Revenue",
                              "style": { "size": 14, "color": "#666666" }
                            }
                          },
                          {
                            "type": "text",
                            "props": {
                              "text": "$45,234",
                              "style": { 
                                "size": 28, 
                                "weight": "bold",
                                "color": "#2196F3"
                              }
                            }
                          },
                          {
                            "type": "text",
                            "props": {
                              "text": "↑ 12% from last month",
                              "style": { "size": 12, "color": "#4CAF50" }
                            }
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
}
```

### How to Configure Dashboard Components:

```kotlin
@Composable
fun DashboardApp() {
    val customRegistry = remember {
        ComponentRegistry().apply {
            // Animated stat card
            register(A2UINodeType.CARD.name) { node, onAction, modifier ->
                if (node.props?.customType == "stat_card") {
                    AnimatedStatCard(node, onAction, modifier)
                } else {
                    // Default card for other types
                    DefaultCard(node, onAction, modifier)
                }
            }
            
            // Custom text with number animation
            register(A2UINodeType.TEXT.name) { node, onAction, modifier ->
                val text = node.props?.text ?: ""
                when {
                    text.startsWith("$") -> {
                        AnimatedMoneyText(text, node.props?.style, modifier)
                    }
                    text.startsWith("↑") || text.startsWith("↓") -> {
                        TrendIndicator(text, node.props?.style, modifier)
                    }
                    else -> {
                        DefaultText(node, modifier)
                    }
                }
            }
        }
    }
    
    val dashboardTheme = buildA2UITheme {
        colors {
            copy(
                background = Color(0xFFF5F5F5),
                surface = Color.White,
                primary = Color(0xFF2196F3)
            )
        }
    }
    
    val document = A2UIParser.parse(dashboardJsonResponse)
    
    A2UIProvider(
        componentRegistry = customRegistry,
        theme = dashboardTheme
    ) {
        A2UIExtendedRenderer(document = document)
    }
}

@Composable
fun AnimatedStatCard(
    node: A2UINode,
    onAction: (A2UIActionEvent) -> Unit,
    modifier: Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500)
    )
    
    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }
    
    Card(
        modifier = modifier
            .alpha(animatedAlpha)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (node.props?.elevation ?: 4).dp
        ),
        shape = RoundedCornerShape((node.props?.cornerRadius ?: 8).dp)
    ) {
        node.children?.forEach { child ->
            RenderNodeWithRegistry(child, onAction)
        }
    }
}
```

---

## 3. Chat Interface

### A2UI Response from Agent:
```json
{
  "version": "0.8",
  "root": {
    "type": "column",
    "children": [
      {
        "type": "lazy_column",
        "props": {
          "weight": 1,
          "reverseLayout": true
        },
        "children": [
          {
            "type": "custom",
            "props": {
              "customType": "chat_message",
              "sender": "agent",
              "text": "Hello! How can I help you today?",
              "timestamp": "10:30 AM"
            }
          },
          {
            "type": "custom",
            "props": {
              "customType": "chat_message",
              "sender": "user",
              "text": "I need help with my order",
              "timestamp": "10:32 AM"
            }
          }
        ]
      },
      {
        "type": "row",
        "props": {
          "padding": { "all": 8 },
          "alignment": "center_vertical"
        },
        "children": [
          {
            "id": "message_input",
            "type": "text_field",
            "props": {
              "hint": "Type a message...",
              "weight": 1
            }
          },
          {
            "id": "send_btn",
            "type": "button",
            "props": {
              "icon": "send",
              "variant": "icon"
            },
            "actions": [
              {
                "event": "click",
                "handler": "sendMessage"
              }
            ]
          }
        ]
      }
    ]
  }
}
```

### How to Configure Chat Components:

```kotlin
@Composable
fun ChatInterfaceApp() {
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var currentInput by remember { mutableStateOf("") }
    
    val chatRegistry = remember {
        ComponentRegistry().apply {
            // Custom chat message bubble
            register("chat_message") { node, _, modifier ->
                ChatMessageBubble(
                    sender = node.props?.sender ?: "unknown",
                    text = node.props?.text ?: "",
                    timestamp = node.props?.timestamp ?: "",
                    modifier = modifier
                )
            }
            
            // Custom input field for chat
            register(A2UINodeType.TEXT_FIELD.name) { node, onAction, modifier ->
                if (node.id == "message_input") {
                    ChatInputField(
                        value = currentInput,
                        onValueChange = { currentInput = it },
                        onSend = {
                            if (currentInput.isNotEmpty()) {
                                messages.add(ChatMessage(
                                    sender = "user",
                                    text = currentInput,
                                    timestamp = getCurrentTime()
                                ))
                                node.actions?.find { it.event == A2UIEventType.CHANGE }?.let {
                                    onAction(A2UIActionEvent(
                                        node.id,
                                        "sendMessage",
                                        JsonPrimitive(currentInput)
                                    ))
                                }
                                currentInput = ""
                            }
                        },
                        modifier = modifier
                    )
                } else {
                    DefaultTextField(node, onAction, modifier)
                }
            }
            
            // Icon button for send
            register(A2UINodeType.BUTTON.name) { node, onAction, modifier ->
                if (node.props?.variant == "icon") {
                    IconButton(
                        onClick = {
                            node.actions?.find { it.event == A2UIEventType.CLICK }?.let {
                                onAction(A2UIActionEvent(node.id, it.handler, it.payload))
                            }
                        },
                        modifier = modifier
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                    }
                } else {
                    DefaultButton(node, onAction, modifier)
                }
            }
        }
    }
    
    val document = A2UIParser.parse(chatJsonResponse)
    
    A2UIProvider(componentRegistry = chatRegistry) {
        A2UIExtendedRenderer(
            document = document,
            onAction = { event ->
                when (event.handler) {
                    "sendMessage" -> {
                        val message = (event.payload as? JsonPrimitive)?.content
                        // Send to backend
                        sendMessageToAgent(message)
                    }
                }
            }
        )
    }
}

@Composable
fun ChatMessageBubble(
    sender: String,
    text: String,
    timestamp: String,
    modifier: Modifier
) {
    val isUser = sender == "user"
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) Color(0xFF2196F3) else Color(0xFFE0E0E0),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = text,
                    color = if (isUser) Color.White else Color.Black
                )
                Text(
                    text = timestamp,
                    fontSize = 10.sp,
                    color = if (isUser) Color.White.copy(alpha = 0.7f) 
                           else Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
```

---

## 4. Settings Screen

### A2UI Response from Agent:
```json
{
  "version": "0.8",
  "root": {
    "type": "scaffold",
    "children": [
      {
        "type": "top_bar",
        "props": { "text": "Settings" }
      },
      {
        "type": "scrollable",
        "children": [
          {
            "type": "column",
            "children": [
              {
                "type": "custom",
                "props": {
                  "customType": "settings_section",
                  "title": "Notifications"
                },
                "children": [
                  {
                    "id": "push_notifications",
                    "type": "switch",
                    "props": {
                      "label": "Push Notifications",
                      "checked": true
                    },
                    "actions": [
                      {
                        "event": "change",
                        "handler": "togglePushNotifications"
                      }
                    ]
                  },
                  {
                    "id": "email_notifications",
                    "type": "switch",
                    "props": {
                      "label": "Email Notifications",
                      "checked": false
                    },
                    "actions": [
                      {
                        "event": "change",
                        "handler": "toggleEmailNotifications"
                      }
                    ]
                  }
                ]
              },
              {
                "type": "divider"
              },
              {
                "type": "custom",
                "props": {
                  "customType": "settings_section",
                  "title": "Appearance"
                },
                "children": [
                  {
                    "id": "dark_mode",
                    "type": "switch",
                    "props": {
                      "label": "Dark Mode",
                      "checked": false
                    },
                    "actions": [
                      {
                        "event": "change",
                        "handler": "toggleDarkMode"
                      }
                    ]
                  },
                  {
                    "id": "font_size",
                    "type": "slider",
                    "props": {
                      "label": "Font Size",
                      "min": 12,
                      "max": 24,
                      "value": 16
                    },
                    "actions": [
                      {
                        "event": "change",
                        "handler": "updateFontSize"
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
}
```

### How to Configure Settings Components:

```kotlin
@Composable
fun SettingsApp() {
    val preferences = remember { mutableStateMapOf<String, Any>() }
    var isDarkMode by remember { mutableStateOf(false) }
    
    val settingsRegistry = remember {
        ComponentRegistry().apply {
            // Settings section with title
            register("settings_section") { node, onAction, modifier ->
                SettingsSection(
                    title = node.props?.title ?: "",
                    modifier = modifier
                ) {
                    node.children?.forEach { child ->
                        RenderNodeWithRegistry(child, onAction)
                    }
                }
            }
            
            // Enhanced switch for settings
            register(A2UINodeType.SWITCH.name) { node, onAction, modifier ->
                SettingsSwitch(
                    label = node.props?.label ?: "",
                    checked = preferences[node.id] as? Boolean 
                              ?: node.props?.checked ?: false,
                    onCheckedChange = { checked ->
                        preferences[node.id ?: ""] = checked
                        if (node.id == "dark_mode") {
                            isDarkMode = checked
                        }
                        node.actions?.find { it.event == A2UIEventType.CHANGE }?.let {
                            onAction(A2UIActionEvent(
                                node.id, 
                                it.handler, 
                                JsonPrimitive(checked)
                            ))
                        }
                    },
                    modifier = modifier
                )
            }
            
            // Custom slider with label
            register(A2UINodeType.SLIDER.name) { node, onAction, modifier ->
                LabeledSlider(
                    label = node.props?.label ?: "",
                    value = (preferences[node.id] as? Float) 
                            ?: node.props?.value?.toFloat() ?: 0f,
                    onValueChange = { value ->
                        preferences[node.id ?: ""] = value
                        node.actions?.find { it.event == A2UIEventType.CHANGE }?.let {
                            onAction(A2UIActionEvent(
                                node.id, 
                                it.handler, 
                                JsonPrimitive(value)
                            ))
                        }
                    },
                    valueRange = (node.props?.min?.toFloat() ?: 0f)..
                                (node.props?.max?.toFloat() ?: 100f),
                    modifier = modifier
                )
            }
        }
    }
    
    val theme = if (isDarkMode) A2UITheme.Dark else A2UITheme.Default
    
    val document = A2UIParser.parse(settingsJsonResponse)
    
    A2UIProvider(
        componentRegistry = settingsRegistry,
        theme = theme
    ) {
        A2UIExtendedRenderer(
            document = document,
            onAction = { event ->
                // Save preferences to backend
                savePreference(event.nodeId, event.payload)
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
```

---

## 5. E-commerce Product List

### A2UI Response from Agent:
```json
{
  "version": "0.8",
  "root": {
    "type": "lazy_column",
    "children": [
      {
        "type": "custom",
        "props": {
          "customType": "product_card",
          "productId": "123",
          "image": "https://example.com/product1.jpg",
          "title": "Premium Headphones",
          "price": "$299.99",
          "rating": 4.5,
          "reviews": 234,
          "inStock": true
        },
        "actions": [
          {
            "event": "click",
            "handler": "viewProduct",
            "payload": { "productId": "123" }
          }
        ]
      }
    ]
  }
}
```

### How to Configure E-commerce Components:

```kotlin
@Composable
fun EcommerceApp() {
    val cartItems = remember { mutableStateListOf<String>() }
    
    val ecommerceRegistry = remember {
        ComponentRegistry().apply {
            register("product_card") { node, onAction, modifier ->
                ProductCard(
                    imageUrl = node.props?.image ?: "",
                    title = node.props?.title ?: "",
                    price = node.props?.price ?: "",
                    rating = node.props?.rating?.toFloat() ?: 0f,
                    reviews = node.props?.reviews?.toInt() ?: 0,
                    inStock = node.props?.inStock ?: false,
                    onAddToCart = {
                        cartItems.add(node.props?.productId ?: "")
                        onAction(A2UIActionEvent(
                            node.id,
                            "addToCart",
                            JsonObject(mapOf(
                                "productId" to JsonPrimitive(node.props?.productId),
                                "quantity" to JsonPrimitive(1)
                            ))
                        ))
                    },
                    onClick = {
                        node.actions?.find { it.event == A2UIEventType.CLICK }?.let {
                            onAction(A2UIActionEvent(node.id, it.handler, it.payload))
                        }
                    },
                    modifier = modifier
                )
            }
        }
    }
    
    val document = A2UIParser.parse(ecommerceJsonResponse)
    
    A2UIProvider(componentRegistry = ecommerceRegistry) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Shop") },
                    actions = {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge { Text(cartItems.size.toString()) }
                                }
                            }
                        ) {
                            IconButton(onClick = { /* Open cart */ }) {
                                Icon(Icons.Default.ShoppingCart, "Cart")
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                A2UIExtendedRenderer(
                    document = document,
                    onAction = { event ->
                        when (event.handler) {
                            "viewProduct" -> {
                                val productId = (event.payload as? JsonObject)
                                    ?.get("productId")?.toString()
                                navigateToProduct(productId)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    imageUrl: String,
    title: String,
    price: String,
    rating: Float,
    reviews: Int,
    inStock: Boolean,
    onAddToCart: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Product image
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title and price
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = price,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    RatingBar(rating = rating, size = 16.dp)
                    Text(
                        text = "($reviews reviews)",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                // Add to cart button
                Button(
                    onClick = onAddToCart,
                    enabled = inStock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(if (inStock) "Add to Cart" else "Out of Stock")
                }
            }
        }
    }
}
```

---

## 6. Multi-step Wizard

### A2UI Response from Agent:
```json
{
  "version": "0.8",
  "root": {
    "type": "column",
    "children": [
      {
        "type": "custom",
        "props": {
          "customType": "step_indicator",
          "currentStep": 2,
          "totalSteps": 4,
          "steps": ["Account", "Details", "Payment", "Review"]
        }
      },
      {
        "type": "custom",
        "props": {
          "customType": "wizard_page",
          "step": 2
        },
        "children": [
          {
            "type": "column",
            "props": { "padding": { "all": 16 } },
            "children": [
              {
                "type": "text",
                "props": {
                  "text": "Step 2: Enter Your Details",
                  "style": { "size": 24, "weight": "bold" }
                }
              },
              {
                "id": "address",
                "type": "text_field",
                "props": {
                  "label": "Address",
                  "required": true
                }
              }
            ]
          }
        ]
      },
      {
        "type": "row",
        "props": {
          "padding": { "all": 16 },
          "arrangement": "space_between"
        },
        "children": [
          {
            "type": "button",
            "props": {
              "text": "Previous",
              "variant": "outlined"
            },
            "actions": [
              {
                "event": "click",
                "handler": "previousStep"
              }
            ]
          },
          {
            "type": "button",
            "props": {
              "text": "Next",
              "variant": "filled"
            },
            "actions": [
              {
                "event": "click",
                "handler": "nextStep"
              }
            ]
          }
        ]
      }
    ]
  }
}
```

### How to Configure Wizard Components:

```kotlin
@Composable
fun WizardApp() {
    var currentStep by remember { mutableStateOf(1) }
    val wizardData = remember { mutableStateMapOf<String, Any>() }
    
    val wizardRegistry = remember {
        ComponentRegistry().apply {
            // Step indicator component
            register("step_indicator") { node, _, modifier ->
                StepIndicator(
                    currentStep = node.props?.currentStep?.toInt() ?: 1,
                    totalSteps = node.props?.totalSteps?.toInt() ?: 1,
                    stepLabels = node.props?.steps as? List<String> ?: emptyList(),
                    modifier = modifier
                )
            }
            
            // Wizard page wrapper
            register("wizard_page") { node, onAction, modifier ->
                AnimatedContent(
                    targetState = node.props?.step?.toInt() ?: 1,
                    transitionSpec = {
                        slideInHorizontally { it } togetherWith 
                        slideOutHorizontally { -it }
                    },
                    modifier = modifier
                ) { step ->
                    Column {
                        node.children?.forEach { child ->
                            RenderNodeWithRegistry(child, onAction)
                        }
                    }
                }
            }
            
            // Navigation buttons with step tracking
            register(A2UINodeType.BUTTON.name) { node, onAction, modifier ->
                Button(
                    onClick = {
                        when (node.actions?.firstOrNull()?.handler) {
                            "previousStep" -> {
                                if (currentStep > 1) currentStep--
                            }
                            "nextStep" -> {
                                if (validateCurrentStep()) {
                                    saveStepData()
                                    currentStep++
                                }
                            }
                            else -> {}
                        }
                        node.actions?.find { it.event == A2UIEventType.CLICK }?.let {
                            onAction(A2UIActionEvent(
                                node.id, 
                                it.handler, 
                                JsonPrimitive(currentStep)
                            ))
                        }
                    },
                    enabled = when (node.actions?.firstOrNull()?.handler) {
                        "previousStep" -> currentStep > 1
                        "nextStep" -> currentStep < 4
                        else -> true
                    },
                    modifier = modifier
                ) {
                    Text(node.props?.text ?: "")
                }
            }
        }
    }
    
    val document = A2UIParser.parse(wizardJsonResponse)
    
    A2UIProvider(componentRegistry = wizardRegistry) {
        A2UIExtendedRenderer(
            document = document,
            onAction = { event ->
                when (event.handler) {
                    "submitWizard" -> {
                        submitWizardData(wizardData.toMap())
                    }
                }
            }
        )
    }
}

@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    stepLabels: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (i in 1..totalSteps) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = when {
                                i < currentStep -> Color(0xFF4CAF50)
                                i == currentStep -> Color(0xFF2196F3)
                                else -> Color(0xFFE0E0E0)
                            },
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = i.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (stepLabels.size >= i) {
                    Text(
                        text = stepLabels[i - 1],
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                if (i < totalSteps) {
                    Divider(
                        color = if (i < currentStep) Color(0xFF4CAF50) 
                               else Color(0xFFE0E0E0),
                        thickness = 2.dp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}
```

---

## 🔑 Key Patterns

### 1. Dynamic Component Selection
```kotlin
register(A2UINodeType.CARD.name) { node, onAction, modifier ->
    when (node.props?.customType) {
        "stat_card" -> StatCard(node, onAction, modifier)
        "product_card" -> ProductCard(node, onAction, modifier)
        else -> DefaultCard(node, onAction, modifier)
    }
}
```

### 2. State Management
```kotlin
val formData = remember { mutableStateMapOf<String, String>() }
// Use throughout custom components
```

### 3. Conditional Rendering
```kotlin
register(A2UINodeType.BUTTON.name) { node, onAction, modifier ->
    if (node.props?.variant == "outlined") {
        OutlinedButton(/* ... */)
    } else {
        Button(/* ... */)
    }
}
```

### 4. Data Enrichment
```kotlin
onAction(A2UIActionEvent(
    nodeId = node.id,
    handler = "submitForm",
    payload = JsonObject(
        mapOf(
            "formId" to JsonPrimitive("contact"),
            "data" to JsonObject(formData.mapValues { JsonPrimitive(it.value) }),
            "timestamp" to JsonPrimitive(System.currentTimeMillis())
        )
    )
))
```

### 5. Theme Switching
```kotlin
val theme = if (isDarkMode) A2UITheme.Dark else A2UITheme.Default
A2UIProvider(theme = theme) { /* ... */ }
```

## 📚 Best Practices

1. **Use `customType` for specialized components** - Add custom type identifiers in your A2UI responses to trigger specific component implementations.

2. **Maintain state outside the registry** - Use `remember` and state hoisting for data that needs to persist across recompositions.

3. **Provide fallbacks** - Always have a default implementation for components that might not have custom overrides.

4. **Validate on the client** - Add client-side validation for forms before sending to the agent.

5. **Animate transitions** - Use Compose animations to make the UI feel more responsive.

6. **Handle loading states** - Show progress indicators while waiting for agent responses.

7. **Test with real data** - Use actual A2UI responses from your agent to ensure compatibility.

## 🚀 Quick Start Template

```kotlin
@Composable
fun MyCustomA2UIApp(jsonResponse: String) {
    // 1. Parse the A2UI document
    val document = A2UIParser.parse(jsonResponse)
    
    // 2. Create custom registry
    val customRegistry = remember {
        ComponentRegistry().apply {
            // Add your custom components here
            register("my_custom_type") { node, onAction, modifier ->
                MyCustomComponent(node, onAction, modifier)
            }
        }
    }
    
    // 3. Create custom theme (optional)
    val customTheme = buildA2UITheme {
        // Configure your theme
    }
    
    // 4. Render with provider
    A2UIProvider(
        componentRegistry = customRegistry,
        theme = customTheme
    ) {
        A2UIExtendedRenderer(
            document = document,
            onAction = { event ->
                // Handle actions from components
                handleAction(event)
            }
        )
    }
}
```

This approach allows you to handle any A2UI response from your agent with custom styling and behavior!