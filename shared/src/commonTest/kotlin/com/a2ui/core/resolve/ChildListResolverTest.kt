package com.a2ui.core.resolve

import com.a2ui.core.model.A2UIComponent
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChildListResolverTest {

    private fun makeResolver(data: JsonObject = JsonObject(emptyMap())): DynamicResolver {
        return DynamicResolver(ResolverContext(data))
    }

    private fun emptyComponents(): Map<String, A2UIComponent> = emptyMap()

    @Test
    fun testResolveStaticList() {
        val resolver = makeResolver()
        val childListResolver = ChildListResolver(resolver)

        val children = buildJsonArray {
            add("a")
            add("b")
            add("c")
        }

        val result = childListResolver.resolve(children, emptyComponents())

        assertEquals(3, result.size)
        assertEquals("a", result[0].componentId)
        assertEquals("b", result[1].componentId)
        assertEquals("c", result[2].componentId)
    }

    @Test
    fun testResolveNull() {
        val resolver = makeResolver()
        val childListResolver = ChildListResolver(resolver)

        val result = childListResolver.resolve(null, emptyComponents())

        assertTrue(result.isEmpty())
    }

    @Test
    fun testResolveSingleString() {
        val resolver = makeResolver()
        val childListResolver = ChildListResolver(resolver)

        val result = childListResolver.resolve(JsonPrimitive("myId"), emptyComponents())

        assertEquals(1, result.size)
        assertEquals("myId", result[0].componentId)
    }

    @Test
    fun testResolveTemplate() {
        val data = buildJsonObject {
            putJsonArray("items") {
                addJsonObject { put("title", "First") }
                addJsonObject { put("title", "Second") }
                addJsonObject { put("title", "Third") }
            }
        }
        val resolver = makeResolver(data)
        val childListResolver = ChildListResolver(resolver)

        val template = buildJsonObject {
            put("componentId", "tmpl")
            put("path", "/items")
        }

        val result = childListResolver.resolve(template, emptyComponents())

        assertEquals(3, result.size)
        result.forEach { ref ->
            assertEquals("tmpl", ref.componentId)
            assertNotNull(ref.scopedData)
        }
        assertEquals("First", result[0].scopedData?.get("title")?.jsonPrimitive?.content)
        assertEquals("Second", result[1].scopedData?.get("title")?.jsonPrimitive?.content)
        assertEquals("Third", result[2].scopedData?.get("title")?.jsonPrimitive?.content)
    }
}
