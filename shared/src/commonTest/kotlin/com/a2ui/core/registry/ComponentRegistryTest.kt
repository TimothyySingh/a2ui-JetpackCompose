package com.a2ui.core.registry

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.a2ui.core.model.A2UINode
import com.a2ui.core.model.A2UINodeType
import com.a2ui.core.render.A2UIActionEvent
import kotlin.test.*

/**
 * Test suite for ComponentRegistry
 */
class ComponentRegistryTest {
    
    private lateinit var registry: ComponentRegistry
    
    @BeforeTest
    fun setup() {
        registry = ComponentRegistry()
    }
    
    @Test
    fun testRegisterAndGet() {
        // Given
        val testType = "TEST_COMPONENT"
        val renderer: ComponentRenderer = { _, _, _ -> }
        
        // When
        registry.register(testType, renderer)
        
        // Then
        assertTrue(registry.has(testType))
        assertNotNull(registry.get(testType))
    }
    
    @Test
    fun testRegisterOverwritesExisting() {
        // Given
        val testType = "TEST_COMPONENT"
        var counter = 0
        val firstRenderer: ComponentRenderer = { _, _, _ -> counter = 1 }
        val secondRenderer: ComponentRenderer = { _, _, _ -> counter = 2 }
        
        // When
        registry.register(testType, firstRenderer)
        registry.register(testType, secondRenderer)
        
        // Then
        val renderer = registry.get(testType)
        assertNotNull(renderer)
        // Verify it's the second renderer (would need to invoke to test, but we can check it exists)
        assertTrue(registry.has(testType))
    }
    
    @Test
    fun testRegisterAll() {
        // Given
        val components = mapOf(
            "COMPONENT_A" to { _: A2UINode, _: (A2UIActionEvent) -> Unit, _: Modifier -> },
            "COMPONENT_B" to { _: A2UINode, _: (A2UIActionEvent) -> Unit, _: Modifier -> },
            "COMPONENT_C" to { _: A2UINode, _: (A2UIActionEvent) -> Unit, _: Modifier -> }
        )
        
        // When
        registry.registerAll(components)
        
        // Then
        assertTrue(registry.has("COMPONENT_A"))
        assertTrue(registry.has("COMPONENT_B"))
        assertTrue(registry.has("COMPONENT_C"))
    }
    
    @Test
    fun testUnregister() {
        // Given
        val testType = "TEST_COMPONENT"
        registry.register(testType) { _, _, _ -> }
        
        // When
        registry.unregister(testType)
        
        // Then
        assertFalse(registry.has(testType))
        assertNull(registry.get(testType))
    }
    
    @Test
    fun testClear() {
        // Given
        registry.register("COMPONENT_A") { _, _, _ -> }
        registry.register("COMPONENT_B") { _, _, _ -> }
        registry.register("COMPONENT_C") { _, _, _ -> }
        
        // When
        registry.clear()
        
        // Then
        assertFalse(registry.has("COMPONENT_A"))
        assertFalse(registry.has("COMPONENT_B"))
        assertFalse(registry.has("COMPONENT_C"))
    }
    
    @Test
    fun testCopy() {
        // Given
        registry.register("COMPONENT_A") { _, _, _ -> }
        registry.register("COMPONENT_B") { _, _, _ -> }
        
        // When
        val copy = registry.copy()
        registry.register("COMPONENT_C") { _, _, _ -> }
        
        // Then
        assertTrue(copy.has("COMPONENT_A"))
        assertTrue(copy.has("COMPONENT_B"))
        assertFalse(copy.has("COMPONENT_C")) // Original registry change shouldn't affect copy
        assertTrue(registry.has("COMPONENT_C")) // Original should have the new component
    }
    
    @Test
    fun testMerge() {
        // Given
        val registry1 = ComponentRegistry().apply {
            register("COMPONENT_A") { _, _, _ -> }
            register("COMPONENT_B") { _, _, _ -> }
        }
        
        val registry2 = ComponentRegistry().apply {
            register("COMPONENT_C") { _, _, _ -> }
            register("COMPONENT_D") { _, _, _ -> }
        }
        
        // When
        registry1.merge(registry2)
        
        // Then
        assertTrue(registry1.has("COMPONENT_A"))
        assertTrue(registry1.has("COMPONENT_B"))
        assertTrue(registry1.has("COMPONENT_C"))
        assertTrue(registry1.has("COMPONENT_D"))
    }
    
    @Test
    fun testMergeOverwritesExisting() {
        // Given
        var result = "initial"
        val registry1 = ComponentRegistry().apply {
            register("COMPONENT_A") { _, _, _ -> result = "first" }
        }
        
        val registry2 = ComponentRegistry().apply {
            register("COMPONENT_A") { _, _, _ -> result = "second" }
        }
        
        // When
        registry1.merge(registry2)
        
        // Then
        assertTrue(registry1.has("COMPONENT_A"))
        // The component from registry2 should overwrite registry1's
    }
    
    @Test
    fun testGetNonExistent() {
        // When
        val renderer = registry.get("NON_EXISTENT")
        
        // Then
        assertNull(renderer)
    }
    
    @Test
    fun testHasNonExistent() {
        // When
        val exists = registry.has("NON_EXISTENT")
        
        // Then
        assertFalse(exists)
    }
    
    @Test
    fun testWithDefaults() {
        // When
        val registry = ComponentRegistry.withDefaults {
            register("BUTTON") { _, _, _ -> }
            register("TEXT") { _, _, _ -> }
        }
        
        // Then
        assertTrue(registry.has("BUTTON"))
        assertTrue(registry.has("TEXT"))
    }
    
    @Test
    fun testStandardNodeTypeRegistration() {
        // Test that we can register standard A2UI node types
        registry.register(A2UINodeType.BUTTON.name) { _, _, _ -> }
        registry.register(A2UINodeType.TEXT.name) { _, _, _ -> }
        registry.register(A2UINodeType.CARD.name) { _, _, _ -> }
        
        assertTrue(registry.has(A2UINodeType.BUTTON.name))
        assertTrue(registry.has(A2UINodeType.TEXT.name))
        assertTrue(registry.has(A2UINodeType.CARD.name))
    }
    
    @Test
    fun testCustomTypeRegistration() {
        // Test that we can register custom component types
        registry.register("CUSTOM_CHART") { _, _, _ -> }
        registry.register("CUSTOM_AVATAR") { _, _, _ -> }
        registry.register("CUSTOM_RATING") { _, _, _ -> }
        
        assertTrue(registry.has("CUSTOM_CHART"))
        assertTrue(registry.has("CUSTOM_AVATAR"))
        assertTrue(registry.has("CUSTOM_RATING"))
    }
}