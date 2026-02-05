package com.a2ui.integration

import com.a2ui.core.model.A2UIActionEvent
import com.a2ui.core.model.A2UIComponent
import com.a2ui.core.model.A2UISurface
import com.a2ui.core.provider.A2UIConfig
import com.a2ui.core.registry.ComponentRegistry
import com.a2ui.core.state.A2UIStateManager
import com.a2ui.core.theme.A2UITheme
import com.a2ui.core.theme.buildA2UITheme
import com.a2ui.examples.createCustomComponentRegistry
import com.a2ui.examples.createCustomTheme
import kotlinx.serialization.json.JsonArray
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
 * Integration tests for the complete A2UI v0.9 system.
 *
 * Builds surfaces using A2UISurface(root=..., components=mapOf(...))
 * flat map format. All tests are conceptual (no compose test rule).
 */
class A2UIIntegrationTest {

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Create a single-component surface.
     */
    private fun simpleSurface(component: A2UIComponent): A2UISurface = A2UISurface(
        root = component.id,
        components = mapOf(component.id to component)
    )

    /**
     * Create a surface from a list of components; the first is root.
     */
    private fun buildSurface(vararg components: A2UIComponent): A2UISurface {
        require(components.isNotEmpty())
        return A2UISurface(
            root = components.first().id,
            components = components.associateBy { it.id }
        )
    }

    /**
     * Create a list of child IDs as a JsonArray for the children property.
     */
    private fun childIds(vararg ids: String): JsonArray =
        JsonArray(ids.map { JsonPrimitive(it) })

    // ------------------------------------------------------------------
    // Complete flow integration: custom registry + theme
    // ------------------------------------------------------------------

    @Test
    fun testCompleteFlowIntegration() {
        // Custom theme
        val customTheme = buildA2UITheme {
            colors {
                copy(
                    primary = androidx.compose.ui.graphics.Color.Blue,
                    onPrimary = androidx.compose.ui.graphics.Color.White
                )
            }
        }

        // Custom registry
        val customRegistry = ComponentRegistry().apply {
            register("Button") { _, _, _, _, _ -> }
        }

        // Surface
        val surface = A2UISurface(
            root = "root",
            components = mapOf(
                "root" to A2UIComponent(
                    id = "root",
                    component = "Column",
                    children = childIds("title", "btn1")
                ),
                "title" to A2UIComponent(
                    id = "title",
                    component = "Text",
                    properties = buildJsonObject { put("text", "Integration Test") }
                ),
                "btn1" to A2UIComponent(
                    id = "btn1",
                    component = "Button",
                    properties = buildJsonObject {
                        put("text", "Click Me")
                        putJsonObject("action") { put("name", "handleClick") }
                    }
                )
            )
        )

        // Verify surface structure
        assertEquals(3, surface.components.size)
        assertEquals("root", surface.root)
        assertNotNull(surface.components["root"])
        assertNotNull(surface.components["title"])
        assertNotNull(surface.components["btn1"])

        // Verify registry has override for Button
        assertTrue(customRegistry.has("Button"))
        assertFalse(customRegistry.has("Text"))
        assertFalse(customRegistry.has("Column"))

        // Verify config
        val config = A2UIConfig(
            componentRegistry = customRegistry,
            theme = customTheme
        )
        assertEquals(customTheme, config.theme)
        assertTrue(config.componentRegistry.has("Button"))

        // Verify action event can be constructed
        val event = A2UIActionEvent(
            componentId = "btn1",
            actionName = "handleClick"
        )
        assertEquals("btn1", event.componentId)
        assertEquals("handleClick", event.actionName)
    }

    @Test
    fun testCompleteFlowWithExampleHelpers() {
        // Use the example helpers from the examples package
        val registry = createCustomComponentRegistry()
        val theme = createCustomTheme()

        assertTrue(registry.has("Button"))
        assertTrue(registry.has("Card"))
        assertTrue(registry.has("Chip"))
        assertTrue(registry.has("AnimatedText"))
        assertNotNull(theme)
    }

    // ------------------------------------------------------------------
    // Form scenario with TextFields and Buttons
    // ------------------------------------------------------------------

    @Test
    fun testFormScenario() {
        val surface = A2UISurface(
            root = "form",
            components = mapOf(
                "form" to A2UIComponent(
                    id = "form",
                    component = "Column",
                    children = childIds("name_field", "email_field", "submit_btn")
                ),
                "name_field" to A2UIComponent(
                    id = "name_field",
                    component = "TextField",
                    properties = buildJsonObject {
                        put("label", "Name")
                        putJsonObject("action") { put("name", "updateName") }
                    }
                ),
                "email_field" to A2UIComponent(
                    id = "email_field",
                    component = "TextField",
                    properties = buildJsonObject {
                        put("label", "Email")
                        putJsonObject("action") { put("name", "updateEmail") }
                    }
                ),
                "submit_btn" to A2UIComponent(
                    id = "submit_btn",
                    component = "Button",
                    properties = buildJsonObject {
                        put("text", "Submit")
                        putJsonObject("action") { put("name", "submitForm") }
                    }
                )
            )
        )

        // Verify form structure
        assertEquals(4, surface.components.size)
        assertEquals("form", surface.root)

        // Verify field types
        assertEquals("TextField", surface.components["name_field"]?.component)
        assertEquals("TextField", surface.components["email_field"]?.component)
        assertEquals("Button", surface.components["submit_btn"]?.component)

        // Verify action events can be constructed for form fields
        val nameEvent = A2UIActionEvent(
            componentId = "name_field",
            actionName = "updateName",
            value = JsonPrimitive("John Doe")
        )
        assertEquals("name_field", nameEvent.componentId)
        assertEquals("updateName", nameEvent.actionName)
        assertEquals(JsonPrimitive("John Doe"), nameEvent.value)

        val submitEvent = A2UIActionEvent(
            componentId = "submit_btn",
            actionName = "submitForm",
            context = buildJsonObject {
                put("name", "John Doe")
                put("email", "john@example.com")
            }
        )
        assertEquals("submit_btn", submitEvent.componentId)
        assertEquals("submitForm", submitEvent.actionName)
        assertNotNull(submitEvent.context)
    }

    @Test
    fun testFormWithCustomRegistry() {
        val registry = ComponentRegistry().apply {
            register("TextField") { _, _, _, _, _ -> }
            register("Button") { _, _, _, _, _ -> }
        }

        val surface = A2UISurface(
            root = "form",
            components = mapOf(
                "form" to A2UIComponent(
                    id = "form",
                    component = "Column",
                    children = childIds("input1", "input2", "submit")
                ),
                "input1" to A2UIComponent(id = "input1", component = "TextField"),
                "input2" to A2UIComponent(id = "input2", component = "TextField"),
                "submit" to A2UIComponent(id = "submit", component = "Button")
            )
        )

        // Verify registry coverage for form components
        for ((_, comp) in surface.components) {
            if (comp.component != "Column") {
                assertTrue(
                    registry.has(comp.component),
                    "Expected registry to have '${comp.component}'"
                )
            }
        }
    }

    // ------------------------------------------------------------------
    // Surface builder helpers
    // ------------------------------------------------------------------

    @Test
    fun testSimpleSurfaceBuilder() {
        val surface = simpleSurface(
            A2UIComponent(
                id = "text1",
                component = "Text",
                properties = buildJsonObject { put("text", "Hello") }
            )
        )

        assertEquals("text1", surface.root)
        assertEquals(1, surface.components.size)
        assertEquals("Text", surface.components["text1"]?.component)
    }

    @Test
    fun testBuildSurfaceHelper() {
        val surface = buildSurface(
            A2UIComponent(id = "root", component = "Column"),
            A2UIComponent(id = "child1", component = "Text"),
            A2UIComponent(id = "child2", component = "Button")
        )

        assertEquals("root", surface.root)
        assertEquals(3, surface.components.size)
        assertEquals("Column", surface.components["root"]?.component)
        assertEquals("Text", surface.components["child1"]?.component)
        assertEquals("Button", surface.components["child2"]?.component)
    }

    @Test
    fun testChildIdsHelper() {
        val ids = childIds("a", "b", "c")
        assertEquals(3, ids.size)
        assertEquals(JsonPrimitive("a"), ids[0])
        assertEquals(JsonPrimitive("b"), ids[1])
        assertEquals(JsonPrimitive("c"), ids[2])
    }

    @Test
    fun testSurfaceWithDataModel() {
        val surface = A2UISurface(
            root = "root",
            components = mapOf(
                "root" to A2UIComponent(
                    id = "root",
                    component = "Text",
                    properties = buildJsonObject { put("text", "/userName") }
                )
            ),
            data = buildJsonObject {
                put("userName", "Alice")
                put("count", 42)
            }
        )

        assertEquals(JsonPrimitive("Alice"), surface.data["userName"])
        assertEquals(JsonPrimitive(42), surface.data["count"])
    }

    // ------------------------------------------------------------------
    // State manager operations
    // ------------------------------------------------------------------

    @Test
    fun testStateManagerSetSurface() {
        val stateManager = A2UIStateManager()
        assertNull(stateManager.surface.value)

        val surface = simpleSurface(
            A2UIComponent(id = "root", component = "Text")
        )
        stateManager.setSurface(surface)

        assertNotNull(stateManager.surface.value)
        assertEquals("root", stateManager.surface.value?.root)
    }

    @Test
    fun testStateManagerClear() {
        val stateManager = A2UIStateManager()

        stateManager.setSurface(
            simpleSurface(A2UIComponent(id = "root", component = "Text"))
        )
        assertNotNull(stateManager.surface.value)

        stateManager.clear()
        assertNull(stateManager.surface.value)
    }

    @Test
    fun testStateManagerLoading() {
        val stateManager = A2UIStateManager()

        assertFalse(stateManager.isLoading.value)

        stateManager.setLoading(true)
        assertTrue(stateManager.isLoading.value)

        stateManager.setLoading(false)
        assertFalse(stateManager.isLoading.value)
    }

    @Test
    fun testStateManagerLoadDocument() {
        val stateManager = A2UIStateManager()

        val json = """
        {
            "root": "root",
            "components": {
                "root": {
                    "id": "root",
                    "component": "Text",
                    "properties": {"text": "Hello"}
                }
            }
        }
        """.trimIndent()

        stateManager.loadDocument(json)

        val surface = stateManager.surface.value
        assertNotNull(surface)
        assertEquals("root", surface.root)
        assertEquals(1, surface.components.size)
        assertEquals("Text", surface.components["root"]?.component)
    }

    @Test
    fun testStateManagerDocumentAlias() {
        val stateManager = A2UIStateManager()

        val surface = simpleSurface(
            A2UIComponent(id = "root", component = "Text")
        )
        stateManager.setSurface(surface)

        // document should be an alias for surface
        assertEquals(stateManager.surface.value, stateManager.document.value)
    }

    // ------------------------------------------------------------------
    // Large component tree (flat map with many components)
    // ------------------------------------------------------------------

    @Test
    fun testLargeComponentTree() {
        val components = mutableMapOf<String, A2UIComponent>()

        // Root column
        val childIdsList = (1..100).map { "item_$it" }
        components["root"] = A2UIComponent(
            id = "root",
            component = "Column",
            children = JsonArray(childIdsList.map { JsonPrimitive(it) })
        )

        // 100 child components - alternating Text and Button
        for (i in 1..100) {
            val type = if (i % 2 == 0) "Text" else "Button"
            components["item_$i"] = A2UIComponent(
                id = "item_$i",
                component = type,
                properties = buildJsonObject { put("text", "Item $i") }
            )
        }

        val surface = A2UISurface(root = "root", components = components)

        // Verify structure
        assertEquals(101, surface.components.size) // root + 100 children
        assertEquals("root", surface.root)
        assertEquals("Column", surface.components["root"]?.component)

        // Spot-check specific items
        assertEquals("Button", surface.components["item_1"]?.component)
        assertEquals("Text", surface.components["item_2"]?.component)
        assertEquals("Button", surface.components["item_99"]?.component)
        assertEquals("Text", surface.components["item_100"]?.component)

        // Verify all items exist
        for (i in 1..100) {
            assertNotNull(
                surface.components["item_$i"],
                "Expected component 'item_$i' to exist"
            )
        }
    }

    @Test
    fun testLargeComponentTreeWithCustomRegistry() {
        val registry = ComponentRegistry().apply {
            register("Button") { _, _, _, _, _ -> }
            register("Text") { _, _, _, _, _ -> }
        }

        val components = mutableMapOf<String, A2UIComponent>()
        components["root"] = A2UIComponent(id = "root", component = "LazyColumn")

        for (i in 1..50) {
            components["btn_$i"] = A2UIComponent(
                id = "btn_$i",
                component = "Button",
                properties = buildJsonObject {
                    put("text", "Button $i")
                    putJsonObject("action") { put("name", "click_$i") }
                }
            )
            components["txt_$i"] = A2UIComponent(
                id = "txt_$i",
                component = "Text",
                properties = buildJsonObject { put("text", "Label $i") }
            )
        }

        val surface = A2UISurface(root = "root", components = components)

        // 1 root + 50 buttons + 50 texts = 101
        assertEquals(101, surface.components.size)

        // Registry should have renderers for Button and Text
        assertTrue(registry.has("Button"))
        assertTrue(registry.has("Text"))
        assertFalse(registry.has("LazyColumn"))
    }

    // ------------------------------------------------------------------
    // Nested surface with data binding context
    // ------------------------------------------------------------------

    @Test
    fun testSurfaceWithNestedCardsAndData() {
        val surface = A2UISurface(
            root = "root",
            components = mapOf(
                "root" to A2UIComponent(
                    id = "root",
                    component = "Column",
                    children = childIds("header", "stats_row")
                ),
                "header" to A2UIComponent(
                    id = "header",
                    component = "Text",
                    properties = buildJsonObject {
                        put("text", "/dashboard/title")
                        put("variant", "h2")
                    }
                ),
                "stats_row" to A2UIComponent(
                    id = "stats_row",
                    component = "Row",
                    children = childIds("card_users", "card_revenue")
                ),
                "card_users" to A2UIComponent(
                    id = "card_users",
                    component = "Card",
                    children = childIds("users_text")
                ),
                "users_text" to A2UIComponent(
                    id = "users_text",
                    component = "Text",
                    properties = buildJsonObject { put("text", "/dashboard/users") }
                ),
                "card_revenue" to A2UIComponent(
                    id = "card_revenue",
                    component = "Card",
                    children = childIds("revenue_text")
                ),
                "revenue_text" to A2UIComponent(
                    id = "revenue_text",
                    component = "Text",
                    properties = buildJsonObject { put("text", "/dashboard/revenue") }
                )
            ),
            data = buildJsonObject {
                putJsonObject("dashboard") {
                    put("title", "Dashboard")
                    put("users", "1,234")
                    put("revenue", "$56,789")
                }
            }
        )

        assertEquals(7, surface.components.size)
        assertNotNull(surface.data["dashboard"])

        // Verify component types
        assertEquals("Column", surface.components["root"]?.component)
        assertEquals("Row", surface.components["stats_row"]?.component)
        assertEquals("Card", surface.components["card_users"]?.component)
        assertEquals("Card", surface.components["card_revenue"]?.component)
    }
}
