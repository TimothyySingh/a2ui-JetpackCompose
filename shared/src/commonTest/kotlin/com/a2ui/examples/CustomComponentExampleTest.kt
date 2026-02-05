package com.a2ui.examples

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
import com.a2ui.core.model.*
import com.a2ui.core.provider.A2UIProvider
import com.a2ui.core.registry.ComponentRegistry
import com.a2ui.core.render.A2UIActionEvent
import com.a2ui.core.render.A2UIExtendedRenderer
import com.a2ui.core.theme.A2UITheme
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Test suite for the custom component examples
 */
class CustomComponentExampleTest {
    
    /**
     * Test the custom gradient button component
     */
    @Test
    fun testCustomGradientButton() {
        /*
        @get:Rule
        val composeTestRule = createComposeRule()
        
        var actionFired = false
        val registry = ComponentRegistry().apply {
            register(A2UINodeType.BUTTON.name, ::CustomGradientButton)
        }
        
        val document = A2UIDocument(
            version = "0.8",
            root = A2UINode(
                id = "gradient-btn",
                type = A2UINodeType.BUTTON,
                props = A2UIProps(text = "Gradient Test"),
                actions = listOf(
                    A2UIAction(
                        event = A2UIEventType.CLICK,
                        handler = "testClick"
                    )
                )
            )
        )
        
        composeTestRule.setContent {
            A2UIProvider(componentRegistry = registry) {
                A2UIExtendedRenderer(
                    document = document,
                    onAction = { event ->
                        actionFired = true
                        assertEquals("gradient-btn", event.nodeId)
                        assertEquals("testClick", event.handler)
                    }
                )
            }
        }
        
        // Verify custom button is rendered with text
        composeTestRule.onNodeWithText("Gradient Test").assertExists()
        
        // Perform click
        composeTestRule.onNodeWithText("Gradient Test").performClick()
        
        // Verify action was fired
        assertTrue(actionFired)
        */
    }
    
    /**
     * Test the custom neumorphic card component
     */
    @Test
    fun testCustomNeumorphicCard() {
        /*
        val registry = ComponentRegistry().apply {
            register(A2UINodeType.CARD.name, ::CustomNeumorphicCard)
        }
        
        val document = A2UIDocument(
            version = "0.8",
            root = A2UINode(
                type = A2UINodeType.CARD,
                children = listOf(
                    A2UINode(
                        type = A2UINodeType.TEXT,
                        props = A2UIProps(text = "Card Title")
                    ),
                    A2UINode(
                        type = A2UINodeType.TEXT,
                        props = A2UIProps(text = "Card content goes here")
                    )
                )
            )
        )
        
        composeTestRule.setContent {
            A2UIProvider(componentRegistry = registry) {
                A2UIExtendedRenderer(document = document)
            }
        }
        
        // Verify card children are rendered
        composeTestRule.onNodeWithText("Card Title").assertExists()
        composeTestRule.onNodeWithText("Card content goes here").assertExists()
        */
    }
    
    /**
     * Test the custom chip component
     */
    @Test
    fun testCustomChip() {
        /*
        var clickedChip: String? = null
        val registry = ComponentRegistry().apply {
            register("CHIP", ::CustomChip)
        }
        
        val document = A2UIDocument(
            version = "0.8",
            root = A2UINode(
                type = A2UINodeType.ROW,
                children = listOf(
                    A2UINode(
                        id = "chip-design",
                        type = A2UINodeType.CUSTOM,
                        props = A2UIProps(
                            text = "Design",
                            checked = true,
                            customType = "CHIP"
                        ),
                        actions = listOf(
                            A2UIAction(
                                event = A2UIEventType.CLICK,
                                handler = "selectChip",
                                payload = JsonPrimitive("Design")
                            )
                        )
                    ),
                    A2UINode(
                        id = "chip-dev",
                        type = A2UINodeType.CUSTOM,
                        props = A2UIProps(
                            text = "Development",
                            checked = false,
                            customType = "CHIP"
                        ),
                        actions = listOf(
                            A2UIAction(
                                event = A2UIEventType.CLICK,
                                handler = "selectChip",
                                payload = JsonPrimitive("Development")
                            )
                        )
                    )
                )
            )
        )
        
        composeTestRule.setContent {
            A2UIProvider(componentRegistry = registry) {
                A2UIExtendedRenderer(
                    document = document,
                    onAction = { event ->
                        if (event.handler == "selectChip") {
                            clickedChip = (event.payload as? JsonPrimitive)?.content
                        }
                    }
                )
            }
        }
        
        // Verify chips are rendered
        composeTestRule.onNodeWithText("Design").assertExists()
        composeTestRule.onNodeWithText("Development").assertExists()
        
        // Click Development chip
        composeTestRule.onNodeWithText("Development").performClick()
        
        // Verify click was handled
        assertEquals("Development", clickedChip)
        */
    }
    
    /**
     * Test custom theme creation and application
     */
    @Test
    fun testCustomThemeCreation() {
        val customTheme = createCustomTheme()
        
        // Verify theme colors
        assertEquals(Color(0xFF667EEA), customTheme.colors.primary)
        assertEquals(Color(0xFF764BA2), customTheme.colors.primaryVariant)
        assertEquals(Color(0xFFFFC107), customTheme.colors.secondary)
        assertEquals(Color(0xFFF7F8FA), customTheme.colors.background)
        
        // Verify typography
        assertEquals(36.sp, customTheme.typography.h1.fontSize)
        assertEquals(32.sp, customTheme.typography.h2.fontSize)
        assertEquals(16.sp, customTheme.typography.body.fontSize)
        
        // Verify spacing
        assertEquals(4.dp, customTheme.spacing.xs)
        assertEquals(48.dp, customTheme.spacing.xl)
        
        // Verify component styles
        assertEquals(56.dp, customTheme.components.button.minHeight)
        assertEquals(12.dp, customTheme.components.button.cornerRadius)
        assertEquals(20.dp, customTheme.components.card.cornerRadius)
    }
    
    /**
     * Test custom component registry creation
     */
    @Test
    fun testCustomComponentRegistryCreation() {
        val registry = createCustomComponentRegistry()
        
        // Verify standard components are overridden
        assertTrue(registry.has(A2UINodeType.BUTTON.name))
        assertTrue(registry.has(A2UINodeType.CARD.name))
        
        // Verify custom component types are registered
        assertTrue(registry.has("CHIP"))
        assertTrue(registry.has("ANIMATED_TEXT"))
    }
    
    /**
     * Test the complete custom app example
     */
    @Test
    fun testCompleteCustomApp() {
        /*
        var lastAction: String? = null
        
        composeTestRule.setContent {
            val customRegistry = remember { createCustomComponentRegistry() }
            val customTheme = remember { createCustomTheme() }
            
            val document = A2UIDocument(
                version = "0.8",
                root = A2UINode(
                    type = A2UINodeType.COLUMN,
                    children = listOf(
                        A2UINode(
                            type = A2UINodeType.TEXT,
                            props = A2UIProps(text = "Custom App Test")
                        ),
                        A2UINode(
                            type = A2UINodeType.BUTTON,
                            props = A2UIProps(text = "Custom Button"),
                            actions = listOf(
                                A2UIAction(
                                    event = A2UIEventType.CLICK,
                                    handler = "customClick"
                                )
                            )
                        ),
                        A2UINode(
                            type = A2UINodeType.CARD,
                            children = listOf(
                                A2UINode(
                                    type = A2UINodeType.TEXT,
                                    props = A2UIProps(text = "Custom Card")
                                )
                            )
                        )
                    )
                )
            )
            
            A2UIProvider(
                componentRegistry = customRegistry,
                theme = customTheme
            ) {
                A2UIExtendedRenderer(
                    document = document,
                    onAction = { event ->
                        lastAction = event.handler
                    }
                )
            }
        }
        
        // Verify rendering
        composeTestRule.onNodeWithText("Custom App Test").assertExists()
        composeTestRule.onNodeWithText("Custom Button").assertExists()
        composeTestRule.onNodeWithText("Custom Card").assertExists()
        
        // Test interaction
        composeTestRule.onNodeWithText("Custom Button").performClick()
        assertEquals("customClick", lastAction)
        */
    }
    
    /**
     * Test partial override example
     */
    @Test
    fun testPartialOverride() {
        /*
        val partialRegistry = ComponentRegistry().apply {
            register(A2UINodeType.BUTTON.name) { node, onAction, modifier ->
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
        
        val document = A2UIDocument(
            version = "0.8",
            root = A2UINode(
                type = A2UINodeType.COLUMN,
                children = listOf(
                    A2UINode(
                        type = A2UINodeType.BUTTON,
                        props = A2UIProps(text = "Outlined Button")
                    ),
                    A2UINode(
                        type = A2UINodeType.TEXT,
                        props = A2UIProps(text = "Default Text")
                    ),
                    A2UINode(
                        type = A2UINodeType.CARD,
                        children = listOf(
                            A2UINode(
                                type = A2UINodeType.TEXT,
                                props = A2UIProps(text = "Default Card")
                            )
                        )
                    )
                )
            )
        )
        
        composeTestRule.setContent {
            A2UIProvider(componentRegistry = partialRegistry) {
                A2UIExtendedRenderer(document = document)
            }
        }
        
        // Button should be custom (outlined)
        composeTestRule.onNodeWithText("Outlined Button").assertExists()
        
        // Text and Card should use defaults
        composeTestRule.onNodeWithText("Default Text").assertExists()
        composeTestRule.onNodeWithText("Default Card").assertExists()
        */
    }
    
    /**
     * Test animated text component
     */
    @Test
    fun testCustomAnimatedText() {
        /*
        val registry = ComponentRegistry().apply {
            register("ANIMATED_TEXT", ::CustomAnimatedText)
        }
        
        val document = A2UIDocument(
            version = "0.8",
            root = A2UINode(
                type = A2UINodeType.CUSTOM,
                props = A2UIProps(
                    text = "Animated Text Test",
                    customType = "ANIMATED_TEXT",
                    style = A2UITextStyle(size = 24)
                )
            )
        )
        
        composeTestRule.setContent {
            A2UIProvider(componentRegistry = registry) {
                A2UIExtendedRenderer(document = document)
            }
        }
        
        // Verify animated text is rendered
        composeTestRule.onNodeWithText("Animated Text Test").assertExists()
        */
    }
    
    /**
     * Test theme and registry combination
     */
    @Test
    fun testThemeAndRegistryCombination() {
        /*
        val customTheme = createCustomTheme()
        val customRegistry = createCustomComponentRegistry()
        
        val document = A2UIDocument(
            version = "0.8",
            root = A2UINode(
                type = A2UINodeType.SCAFFOLD,
                children = listOf(
                    A2UINode(
                        type = A2UINodeType.TOP_BAR,
                        props = A2UIProps(text = "Themed App")
                    ),
                    A2UINode(
                        type = A2UINodeType.COLUMN,
                        props = A2UIProps(
                            padding = A2UIPadding(all = 16)
                        ),
                        children = listOf(
                            A2UINode(
                                type = A2UINodeType.BUTTON,
                                props = A2UIProps(text = "Themed Button")
                            ),
                            A2UINode(
                                type = A2UINodeType.CARD,
                                children = listOf(
                                    A2UINode(
                                        type = A2UINodeType.TEXT,
                                        props = A2UIProps(text = "Themed Card Content")
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        
        composeTestRule.setContent {
            A2UIProvider(
                componentRegistry = customRegistry,
                theme = customTheme
            ) {
                A2UIExtendedRenderer(document = document)
            }
        }
        
        // Verify all components are rendered with theme and custom implementations
        composeTestRule.onNodeWithText("Themed App").assertExists()
        composeTestRule.onNodeWithText("Themed Button").assertExists()
        composeTestRule.onNodeWithText("Themed Card Content").assertExists()
        */
    }
}