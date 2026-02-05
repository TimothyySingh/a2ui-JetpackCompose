package com.a2ui.core.render

import com.a2ui.core.model.A2UIActionEvent
import com.a2ui.core.model.A2UIComponent
import com.a2ui.core.model.A2UISurface
import com.a2ui.core.registry.ComponentRegistry
import com.a2ui.core.theme.A2UITheme
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for A2UIExtendedRenderer - v0.9
 *
 * Uses A2UISurface (flat component map) instead of A2UIDocument / A2UINode.
 * Tests are conceptual (no compose test rule). They verify custom renderer
 * lookup, default fallback, action handling, and registry operations.
 */
class A2UIExtendedRendererTest {

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Create a surface with a single root component.
     */
    private fun simpleSurface(component: A2UIComponent): A2UISurface = A2UISurface(
        root = component.id,
        components = mapOf(component.id to component)
    )

    /**
     * Create a surface with multiple components; first component is root.
     */
    private fun multiSurface(vararg components: A2UIComponent): A2UISurface {
        require(components.isNotEmpty())
        return A2UISurface(
            root = components.first().id,
            components = components.associateBy { it.id }
        )
    }

    // ------------------------------------------------------------------
    // Custom renderer lookup
    // ------------------------------------------------------------------

    @Test
    fun testCustomRendererIsRegistered() {
        val registry = ComponentRegistry().apply {
            register("Button") { _, _, _, _, _ -> }
        }

        assertTrue(registry.has("Button"))
        assertNotNull(registry.get("Button"))
    }

    @Test
    fun testCustomRendererLookupByComponentType() {
        val registry = ComponentRegistry().apply {
            register("Card") { _, _, _, _, _ -> }
            register("Text") { _, _, _, _, _ -> }
        }

        val surface = simpleSurface(
            A2UIComponent(id = "card1", component = "Card")
        )

        val rootComponent = surface.components[surface.root]
        assertNotNull(rootComponent)
        assertTrue(registry.has(rootComponent.component))
    }

    @Test
    fun testCustomRendererNotFoundForUnregistered() {
        val registry = ComponentRegistry()

        val surface = simpleSurface(
            A2UIComponent(id = "text1", component = "Text")
        )

        val rootComponent = surface.components[surface.root]
        assertNotNull(rootComponent)
        assertFalse(registry.has(rootComponent.component))
        assertNull(registry.get(rootComponent.component))
    }

    // ------------------------------------------------------------------
    // Default fallback
    // ------------------------------------------------------------------

    @Test
    fun testDefaultFallbackWhenNoOverride() {
        val registry = ComponentRegistry() // Empty

        val surface = simpleSurface(
            A2UIComponent(
                id = "text1",
                component = "Text",
                properties = buildJsonObject { put("text", "Default Text") }
            )
        )

        // No custom renderer registered -> should fall back to default
        val rootComponent = surface.components[surface.root]
        assertNotNull(rootComponent)
        assertNull(registry.get(rootComponent.component))
        // In real rendering, RenderComponent() handles this component type
    }

    @Test
    fun testMixedCustomAndDefaultComponents() {
        val registry = ComponentRegistry().apply {
            register("Card") { _, _, _, _, _ -> }
            // "Text" and "Button" are NOT registered -> default renderers
        }

        val surface = multiSurface(
            A2UIComponent(id = "root", component = "Column"),
            A2UIComponent(id = "text1", component = "Text"),
            A2UIComponent(id = "card1", component = "Card"),
            A2UIComponent(id = "btn1", component = "Button")
        )

        // Card has a custom renderer
        assertTrue(registry.has("Card"))
        // Text and Button fall back to defaults
        assertFalse(registry.has("Text"))
        assertFalse(registry.has("Button"))
        assertFalse(registry.has("Column"))

        // All components are present in the surface
        assertEquals(4, surface.components.size)
    }

    // ------------------------------------------------------------------
    // Action handling
    // ------------------------------------------------------------------

    @Test
    fun testA2UIActionEventConstruction() {
        val event = A2UIActionEvent(
            componentId = "btn1",
            actionName = "handleClick",
            context = buildJsonObject { put("source", "test") }
        )

        assertEquals("btn1", event.componentId)
        assertEquals("handleClick", event.actionName)
        assertNotNull(event.context)
        assertEquals(
            JsonPrimitive("test"),
            event.context!!["source"]
        )
    }

    @Test
    fun testA2UIActionEventWithValue() {
        val event = A2UIActionEvent(
            componentId = "input1",
            actionName = "onChange",
            value = JsonPrimitive("new text")
        )

        assertEquals("input1", event.componentId)
        assertEquals("onChange", event.actionName)
        assertEquals(JsonPrimitive("new text"), event.value)
    }

    @Test
    fun testActionEventWithNullContext() {
        val event = A2UIActionEvent(
            componentId = "btn1",
            actionName = "click"
        )

        assertNull(event.context)
        assertNull(event.value)
    }

    @Test
    fun testButtonSurfaceWithAction() {
        val surface = simpleSurface(
            A2UIComponent(
                id = "test-btn",
                component = "Button",
                properties = buildJsonObject {
                    put("text", "Click Me")
                    putJsonObject("action") {
                        put("name", "handleClick")
                    }
                }
            )
        )

        val rootComponent = surface.components[surface.root]
        assertNotNull(rootComponent)
        assertEquals("Button", rootComponent.component)
        assertEquals("test-btn", rootComponent.id)

        // Verify the action property is embedded in properties
        val actionObj = rootComponent.properties["action"]
        assertNotNull(actionObj)
    }

    // ------------------------------------------------------------------
    // Registry operations
    // ------------------------------------------------------------------

    @Test
    fun testRegistryOverrideForSurfaceComponent() {
        val registry = ComponentRegistry().apply {
            register("Text") { _, _, _, _, _ -> }
        }

        val surface = simpleSurface(
            A2UIComponent(
                id = "text1",
                component = "Text",
                properties = buildJsonObject { put("text", "Themed Text") }
            )
        )

        val rootComponent = surface.components[surface.root]
        assertNotNull(rootComponent)
        assertTrue(registry.has(rootComponent.component))
    }

    @Test
    fun testRegistryClearRemovesOverrides() {
        val registry = ComponentRegistry().apply {
            register("Button") { _, _, _, _, _ -> }
            register("Card") { _, _, _, _, _ -> }
        }

        assertTrue(registry.has("Button"))
        assertTrue(registry.has("Card"))

        registry.clear()

        assertFalse(registry.has("Button"))
        assertFalse(registry.has("Card"))
    }

    @Test
    fun testRegistryCopyForRendererContext() {
        val original = ComponentRegistry().apply {
            register("Button") { _, _, _, _, _ -> }
        }

        val copy = original.copy()
        original.register("Text") { _, _, _, _, _ -> }

        // Copy should not have Text
        assertTrue(copy.has("Button"))
        assertFalse(copy.has("Text"))

        // Original should have both
        assertTrue(original.has("Button"))
        assertTrue(original.has("Text"))
    }

    // ------------------------------------------------------------------
    // Surface structure tests
    // ------------------------------------------------------------------

    @Test
    fun testSurfaceWithNestedComponents() {
        val surface = A2UISurface(
            root = "root",
            components = mapOf(
                "root" to A2UIComponent(
                    id = "root",
                    component = "Column",
                    children = kotlinx.serialization.json.JsonArray(
                        listOf(JsonPrimitive("header"), JsonPrimitive("card1"), JsonPrimitive("btn1"))
                    )
                ),
                "header" to A2UIComponent(
                    id = "header",
                    component = "Text",
                    properties = buildJsonObject { put("text", "Header") }
                ),
                "card1" to A2UIComponent(
                    id = "card1",
                    component = "Card",
                    children = kotlinx.serialization.json.JsonArray(
                        listOf(JsonPrimitive("card_text"))
                    )
                ),
                "card_text" to A2UIComponent(
                    id = "card_text",
                    component = "Text",
                    properties = buildJsonObject { put("text", "Card Content") }
                ),
                "btn1" to A2UIComponent(
                    id = "btn1",
                    component = "Button",
                    properties = buildJsonObject { put("text", "Submit") }
                )
            )
        )

        assertEquals(5, surface.components.size)
        assertEquals("root", surface.root)
        assertNotNull(surface.components["root"])
        assertNotNull(surface.components["header"])
        assertNotNull(surface.components["card1"])
        assertNotNull(surface.components["card_text"])
        assertNotNull(surface.components["btn1"])
    }

    @Test
    fun testSurfaceWithData() {
        val data = buildJsonObject {
            put("userName", "Alice")
            put("itemCount", 5)
        }

        val surface = A2UISurface(
            root = "root",
            components = mapOf(
                "root" to A2UIComponent(id = "root", component = "Text")
            ),
            data = data
        )

        assertEquals(JsonPrimitive("Alice"), surface.data["userName"])
        assertEquals(JsonPrimitive(5), surface.data["itemCount"])
    }

    // ------------------------------------------------------------------
    // Test data builders
    // ------------------------------------------------------------------

    @Test
    fun testSimpleSurfaceHelper() {
        val component = A2UIComponent(
            id = "btn1",
            component = "Button",
            properties = buildJsonObject {
                put("text", "Click Me")
                putJsonObject("action") { put("name", "onClick") }
            }
        )

        val surface = simpleSurface(component)

        assertEquals("btn1", surface.root)
        assertEquals(1, surface.components.size)
        assertEquals(component, surface.components["btn1"])
    }

    @Test
    fun testMultiSurfaceHelper() {
        val surface = multiSurface(
            A2UIComponent(id = "root", component = "Column"),
            A2UIComponent(id = "child1", component = "Text"),
            A2UIComponent(id = "child2", component = "Button")
        )

        assertEquals("root", surface.root)
        assertEquals(3, surface.components.size)
    }
}
