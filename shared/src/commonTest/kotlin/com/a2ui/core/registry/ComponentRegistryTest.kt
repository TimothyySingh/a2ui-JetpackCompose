package com.a2ui.core.registry

import kotlin.test.*

/**
 * Test suite for ComponentRegistry - v0.9
 *
 * Uses PascalCase type strings ("Button", "Text", "Card") and the new
 * 5-param ComponentRenderer signature:
 *   (component: A2UIComponent, surface: A2UISurface, resolver: DynamicResolver,
 *    onAction: (A2UIActionEvent) -> Unit, modifier: Modifier) -> Unit
 *
 * These are conceptual tests that verify registry map operations without
 * needing the Compose runtime. Dummy renderers are registered but never invoked.
 */
class ComponentRegistryTest {

    private lateinit var registry: ComponentRegistry

    @BeforeTest
    fun setup() {
        registry = ComponentRegistry()
    }

    // ------------------------------------------------------------------
    // Register / Get with PascalCase
    // ------------------------------------------------------------------

    @Test
    fun testRegisterAndGetButton() {
        registry.register("Button") { _, _, _, _, _ -> }

        assertTrue(registry.has("Button"))
        assertNotNull(registry.get("Button"))
    }

    @Test
    fun testRegisterAndGetText() {
        registry.register("Text") { _, _, _, _, _ -> }

        assertTrue(registry.has("Text"))
        assertNotNull(registry.get("Text"))
    }

    @Test
    fun testRegisterAndGetCard() {
        registry.register("Card") { _, _, _, _, _ -> }

        assertTrue(registry.has("Card"))
        assertNotNull(registry.get("Card"))
    }

    // ------------------------------------------------------------------
    // has() / get() correct results
    // ------------------------------------------------------------------

    @Test
    fun testHasReturnsFalseForUnregistered() {
        assertFalse(registry.has("NonExistent"))
    }

    @Test
    fun testGetReturnsNullForUnregistered() {
        assertNull(registry.get("NonExistent"))
    }

    @Test
    fun testHasReturnsTrueAfterRegistration() {
        registry.register("Slider") { _, _, _, _, _ -> }
        assertTrue(registry.has("Slider"))
    }

    @Test
    fun testGetReturnsNonNullAfterRegistration() {
        registry.register("Slider") { _, _, _, _, _ -> }
        assertNotNull(registry.get("Slider"))
    }

    // ------------------------------------------------------------------
    // Overwrite existing registration
    // ------------------------------------------------------------------

    @Test
    fun testRegisterOverwritesExisting() {
        var marker = "first"
        registry.register("Button") { _, _, _, _, _ -> marker = "first" }
        registry.register("Button") { _, _, _, _, _ -> marker = "second" }

        // The key should still exist; it was overwritten, not duplicated
        assertTrue(registry.has("Button"))
        assertNotNull(registry.get("Button"))

        // After overwrite, get() must return the second renderer (identity)
        val retrieved = registry.get("Button")
        // We cannot invoke it (Composable), but we can verify it is not null
        assertNotNull(retrieved)
    }

    // ------------------------------------------------------------------
    // registerAll with map
    // ------------------------------------------------------------------

    @Test
    fun testRegisterAllWithMap() {
        val renderers: Map<String, ComponentRenderer> = mapOf(
            "Button" to { _, _, _, _, _ -> },
            "Text" to { _, _, _, _, _ -> },
            "Card" to { _, _, _, _, _ -> }
        )

        registry.registerAll(renderers)

        assertTrue(registry.has("Button"))
        assertTrue(registry.has("Text"))
        assertTrue(registry.has("Card"))
    }

    // ------------------------------------------------------------------
    // Unregister / Clear
    // ------------------------------------------------------------------

    @Test
    fun testUnregister() {
        registry.register("Button") { _, _, _, _, _ -> }
        assertTrue(registry.has("Button"))

        registry.unregister("Button")

        assertFalse(registry.has("Button"))
        assertNull(registry.get("Button"))
    }

    @Test
    fun testUnregisterNonExistentIsNoOp() {
        // Should not throw
        registry.unregister("DoesNotExist")
        assertFalse(registry.has("DoesNotExist"))
    }

    @Test
    fun testClear() {
        registry.register("Button") { _, _, _, _, _ -> }
        registry.register("Text") { _, _, _, _, _ -> }
        registry.register("Card") { _, _, _, _, _ -> }

        registry.clear()

        assertFalse(registry.has("Button"))
        assertFalse(registry.has("Text"))
        assertFalse(registry.has("Card"))
    }

    // ------------------------------------------------------------------
    // Copy / Merge
    // ------------------------------------------------------------------

    @Test
    fun testCopy() {
        registry.register("Button") { _, _, _, _, _ -> }
        registry.register("Text") { _, _, _, _, _ -> }

        val copy = registry.copy()

        // Copy should contain the same registrations
        assertTrue(copy.has("Button"))
        assertTrue(copy.has("Text"))

        // Mutating the original should not affect the copy
        registry.register("Card") { _, _, _, _, _ -> }
        assertFalse(copy.has("Card"))
        assertTrue(registry.has("Card"))
    }

    @Test
    fun testCopyIndependence() {
        registry.register("Row") { _, _, _, _, _ -> }
        val copy = registry.copy()

        // Mutating the copy should not affect the original
        copy.register("Column") { _, _, _, _, _ -> }
        assertTrue(copy.has("Column"))
        assertFalse(registry.has("Column"))
    }

    @Test
    fun testMerge() {
        val registry1 = ComponentRegistry().apply {
            register("Button") { _, _, _, _, _ -> }
            register("Text") { _, _, _, _, _ -> }
        }

        val registry2 = ComponentRegistry().apply {
            register("Card") { _, _, _, _, _ -> }
            register("Slider") { _, _, _, _, _ -> }
        }

        registry1.merge(registry2)

        assertTrue(registry1.has("Button"))
        assertTrue(registry1.has("Text"))
        assertTrue(registry1.has("Card"))
        assertTrue(registry1.has("Slider"))
    }

    @Test
    fun testMergeOverwritesExisting() {
        val registry1 = ComponentRegistry().apply {
            register("Button") { _, _, _, _, _ -> }
        }

        val registry2 = ComponentRegistry().apply {
            register("Button") { _, _, _, _, _ -> }
        }

        registry1.merge(registry2)

        // Should still have the key; the renderer from registry2 overwrites
        assertTrue(registry1.has("Button"))
    }

    // ------------------------------------------------------------------
    // Custom types: "Chart", "Avatar", "Rating"
    // ------------------------------------------------------------------

    @Test
    fun testCustomTypeChart() {
        registry.register("Chart") { _, _, _, _, _ -> }
        assertTrue(registry.has("Chart"))
        assertNotNull(registry.get("Chart"))
    }

    @Test
    fun testCustomTypeAvatar() {
        registry.register("Avatar") { _, _, _, _, _ -> }
        assertTrue(registry.has("Avatar"))
        assertNotNull(registry.get("Avatar"))
    }

    @Test
    fun testCustomTypeRating() {
        registry.register("Rating") { _, _, _, _, _ -> }
        assertTrue(registry.has("Rating"))
        assertNotNull(registry.get("Rating"))
    }

    @Test
    fun testMultipleCustomTypes() {
        registry.register("Chart") { _, _, _, _, _ -> }
        registry.register("Avatar") { _, _, _, _, _ -> }
        registry.register("Rating") { _, _, _, _, _ -> }

        assertTrue(registry.has("Chart"))
        assertTrue(registry.has("Avatar"))
        assertTrue(registry.has("Rating"))
    }

    // ------------------------------------------------------------------
    // withDefaults builder
    // ------------------------------------------------------------------

    @Test
    fun testWithDefaults() {
        val built = ComponentRegistry.withDefaults {
            register("Button") { _, _, _, _, _ -> }
            register("Text") { _, _, _, _, _ -> }
        }

        assertTrue(built.has("Button"))
        assertTrue(built.has("Text"))
        assertFalse(built.has("Card")) // not registered
    }

    @Test
    fun testWithDefaultsBuilderReturnsNewRegistry() {
        val built = ComponentRegistry.withDefaults {
            register("Card") { _, _, _, _, _ -> }
        }

        // The class-level registry should be unaffected
        assertFalse(registry.has("Card"))
        assertTrue(built.has("Card"))
    }

    // ------------------------------------------------------------------
    // PascalCase standard component types
    // ------------------------------------------------------------------

    @Test
    fun testStandardPascalCaseTypes() {
        val standardTypes = listOf(
            "Text", "Image", "Icon", "Video", "AudioPlayer",
            "Row", "Column", "Card", "List", "Tabs", "Modal", "Divider",
            "Button", "TextField", "CheckBox", "ChoicePicker", "Slider", "DateTimeInput",
            "Scaffold", "Box", "Scrollable", "LazyColumn", "LazyRow",
            "Spacer", "Switch", "Dropdown", "Progress", "Loading",
            "TopBar", "BottomBar", "Fab"
        )

        for (type in standardTypes) {
            registry.register(type) { _, _, _, _, _ -> }
        }

        for (type in standardTypes) {
            assertTrue(registry.has(type), "Expected registry to have type '$type'")
        }
    }
}
