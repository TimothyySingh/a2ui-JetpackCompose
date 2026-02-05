package com.a2ui.core.resolve

import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DynamicResolverTest {

    @Test
    fun testResolveLiteralString() {
        val context = ResolverContext(JsonObject(emptyMap()))
        val resolver = DynamicResolver(context)

        val result = resolver.resolve(JsonPrimitive("hello"))

        assertNotNull(result)
        assertTrue(result is JsonPrimitive)
        assertEquals("hello", result.content)
    }

    @Test
    fun testResolveLiteralNumber() {
        val context = ResolverContext(JsonObject(emptyMap()))
        val resolver = DynamicResolver(context)

        val result = resolver.resolve(JsonPrimitive(42))

        assertNotNull(result)
        assertTrue(result is JsonPrimitive)
        assertEquals(42, result.int)
    }

    @Test
    fun testResolveLiteralBoolean() {
        val context = ResolverContext(JsonObject(emptyMap()))
        val resolver = DynamicResolver(context)

        val result = resolver.resolve(JsonPrimitive(true))

        assertNotNull(result)
        assertTrue(result is JsonPrimitive)
        assertEquals(true, result.boolean)
    }

    @Test
    fun testResolveNull() {
        val context = ResolverContext(JsonObject(emptyMap()))
        val resolver = DynamicResolver(context)

        val result = resolver.resolve(null)

        assertNull(result)
    }

    @Test
    fun testResolveJsonPointer() {
        val data = buildJsonObject {
            putJsonObject("user") {
                put("name", "Alice")
            }
        }
        val context = ResolverContext(data)
        val resolver = DynamicResolver(context)

        val result = resolver.resolve(JsonPrimitive("/user/name"))

        assertNotNull(result)
        assertTrue(result is JsonPrimitive)
        assertEquals("Alice", result.content)
    }

    @Test
    fun testResolveNestedPointer() {
        val data = buildJsonObject {
            putJsonObject("a") {
                putJsonObject("b") {
                    put("c", "deep")
                }
            }
        }
        val context = ResolverContext(data)
        val resolver = DynamicResolver(context)

        val result = resolver.resolve(JsonPrimitive("/a/b/c"))

        assertNotNull(result)
        assertTrue(result is JsonPrimitive)
        assertEquals("deep", result.content)
    }

    @Test
    fun testResolveArrayIndex() {
        val data = buildJsonObject {
            putJsonArray("items") {
                add("x")
                add("y")
                add("z")
            }
        }
        val context = ResolverContext(data)
        val resolver = DynamicResolver(context)

        val result = resolver.resolve(JsonPrimitive("/items/1"))

        assertNotNull(result)
        assertTrue(result is JsonPrimitive)
        assertEquals("y", result.content)
    }

    @Test
    fun testResolvePointerMissing() {
        val data = buildJsonObject {
            put("exists", "yes")
        }
        val context = ResolverContext(data)
        val resolver = DynamicResolver(context)

        val result = resolver.resolve(JsonPrimitive("/nonexistent"))

        assertNull(result)
    }

    @Test
    fun testResolveFunctionCall() {
        val data = JsonObject(emptyMap())
        val context = ResolverContext(data)
        val resolver = DynamicResolver(context)

        val functionCall = buildJsonObject {
            put("call", "required")
            putJsonObject("args") {
                put("value", "hello")
            }
        }

        val result = resolver.resolve(functionCall)

        assertNotNull(result)
        assertTrue(result is JsonPrimitive)
        assertEquals(true, result.boolean)
    }

    @Test
    fun testResolveArray() {
        val context = ResolverContext(JsonObject(emptyMap()))
        val resolver = DynamicResolver(context)

        val array = buildJsonArray {
            add("a")
            add("b")
            add("c")
        }

        val result = resolver.resolve(array)

        assertNotNull(result)
        assertTrue(result is JsonArray)
        assertEquals(3, result.jsonArray.size)
        assertEquals("a", result.jsonArray[0].jsonPrimitive.content)
        assertEquals("b", result.jsonArray[1].jsonPrimitive.content)
        assertEquals("c", result.jsonArray[2].jsonPrimitive.content)
    }

    @Test
    fun testResolveStringList() {
        val context = ResolverContext(JsonObject(emptyMap()))
        val resolver = DynamicResolver(context)

        val array = buildJsonArray {
            add("alpha")
            add("beta")
            add("gamma")
        }

        val result = resolver.resolveStringList(array)

        assertNotNull(result)
        assertEquals(3, result.size)
        assertEquals("alpha", result[0])
        assertEquals("beta", result[1])
        assertEquals("gamma", result[2])
    }

    @Test
    fun testScopedDataPriority() {
        val data = buildJsonObject {
            put("name", "RootName")
        }
        val scopedData = buildJsonObject {
            put("name", "ScopedName")
        }
        val context = ResolverContext(data, scopedData)
        val resolver = DynamicResolver(context)

        val result = resolver.resolve(JsonPrimitive("/name"))

        assertNotNull(result)
        assertTrue(result is JsonPrimitive)
        assertEquals("ScopedName", result.content)
    }
}
