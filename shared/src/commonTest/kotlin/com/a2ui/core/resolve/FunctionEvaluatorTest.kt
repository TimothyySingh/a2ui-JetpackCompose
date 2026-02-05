package com.a2ui.core.resolve

import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FunctionEvaluatorTest {

    private fun makeResolver(data: JsonObject = JsonObject(emptyMap())): DynamicResolver {
        return DynamicResolver(ResolverContext(data))
    }

    private fun callFunction(
        resolver: DynamicResolver,
        name: String,
        args: JsonObject
    ): JsonElement? {
        val functionCall = buildJsonObject {
            put("call", name)
            put("args", args)
        }
        return resolver.resolve(functionCall)
    }

    // --- required ---

    @Test
    fun testRequired_withValue() {
        val resolver = makeResolver()
        val args = buildJsonObject { put("value", "hello") }

        val result = callFunction(resolver, "required", args)

        assertNotNull(result)
        assertEquals(true, result.jsonPrimitive.boolean)
    }

    @Test
    fun testRequired_empty() {
        val resolver = makeResolver()
        val args = buildJsonObject { put("value", "") }

        val result = callFunction(resolver, "required", args)

        assertNotNull(result)
        assertEquals(false, result.jsonPrimitive.boolean)
    }

    @Test
    fun testRequired_null() {
        val data = buildJsonObject {
            // "missing" key is intentionally absent
        }
        val resolver = makeResolver(data)
        val args = buildJsonObject { put("value", "/missing") }

        val result = callFunction(resolver, "required", args)

        assertNotNull(result)
        assertEquals(false, result.jsonPrimitive.boolean)
    }

    // --- regex ---

    @Test
    fun testRegex_match() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("pattern", "^[A-Z]")
            put("value", "Hello")
        }

        val result = callFunction(resolver, "regex", args)

        assertNotNull(result)
        assertEquals(true, result.jsonPrimitive.boolean)
    }

    @Test
    fun testRegex_noMatch() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("pattern", "^[A-Z]")
            put("value", "hello")
        }

        val result = callFunction(resolver, "regex", args)

        assertNotNull(result)
        assertEquals(false, result.jsonPrimitive.boolean)
    }

    // --- length ---

    @Test
    fun testLength_valid() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("value", "test")
            put("min", 2)
            put("max", 10)
        }

        val result = callFunction(resolver, "length", args)

        assertNotNull(result)
        assertEquals(true, result.jsonPrimitive.boolean)
    }

    @Test
    fun testLength_tooShort() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("value", "a")
            put("min", 2)
            put("max", 10)
        }

        val result = callFunction(resolver, "length", args)

        assertNotNull(result)
        assertEquals(false, result.jsonPrimitive.boolean)
    }

    // --- numeric ---

    @Test
    fun testNumeric_inRange() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("value", 50)
            put("min", 0)
            put("max", 100)
        }

        val result = callFunction(resolver, "numeric", args)

        assertNotNull(result)
        assertEquals(true, result.jsonPrimitive.boolean)
    }

    @Test
    fun testNumeric_outOfRange() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("value", 200)
            put("min", 0)
            put("max", 100)
        }

        val result = callFunction(resolver, "numeric", args)

        assertNotNull(result)
        assertEquals(false, result.jsonPrimitive.boolean)
    }

    // --- email ---

    @Test
    fun testEmail_valid() {
        val resolver = makeResolver()
        val args = buildJsonObject { put("value", "test@example.com") }

        val result = callFunction(resolver, "email", args)

        assertNotNull(result)
        assertEquals(true, result.jsonPrimitive.boolean)
    }

    @Test
    fun testEmail_invalid() {
        val resolver = makeResolver()
        val args = buildJsonObject { put("value", "not-email") }

        val result = callFunction(resolver, "email", args)

        assertNotNull(result)
        assertEquals(false, result.jsonPrimitive.boolean)
    }

    // --- formatString ---

    @Test
    fun testFormatString() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("template", "Hello \${name}")
            put("name", "World")
        }

        val result = callFunction(resolver, "formatString", args)

        assertNotNull(result)
        assertEquals("Hello World", result.jsonPrimitive.content)
    }

    // --- formatNumber ---

    @Test
    fun testFormatNumber() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("value", 1234.567)
            put("decimals", 2)
        }

        val result = callFunction(resolver, "formatNumber", args)

        assertNotNull(result)
        // 1234.567 rounded to 2 decimals = 1234.57
        val formatted = result.jsonPrimitive.content
        assertTrue(formatted.contains("1234.57"), "Expected formatted number to contain '1234.57', got '$formatted'")
    }

    // --- formatCurrency ---

    @Test
    fun testFormatCurrency() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("value", 29.99)
            put("currency", "USD")
        }

        val result = callFunction(resolver, "formatCurrency", args)

        assertNotNull(result)
        assertEquals("\$29.99", result.jsonPrimitive.content)
    }

    // --- pluralize ---

    @Test
    fun testPluralize_one() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("count", 1)
            put("one", "item")
            put("other", "items")
        }

        val result = callFunction(resolver, "pluralize", args)

        assertNotNull(result)
        assertEquals("item", result.jsonPrimitive.content)
    }

    @Test
    fun testPluralize_many() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("count", 5)
            put("one", "item")
            put("other", "items")
        }

        val result = callFunction(resolver, "pluralize", args)

        assertNotNull(result)
        assertEquals("items", result.jsonPrimitive.content)
    }

    // --- and ---

    @Test
    fun testAnd_allTrue() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            putJsonArray("values") {
                add(true)
                add(true)
            }
        }

        val result = callFunction(resolver, "and", args)

        assertNotNull(result)
        assertEquals(true, result.jsonPrimitive.boolean)
    }

    @Test
    fun testAnd_oneFalse() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            putJsonArray("values") {
                add(true)
                add(false)
            }
        }

        val result = callFunction(resolver, "and", args)

        assertNotNull(result)
        assertEquals(false, result.jsonPrimitive.boolean)
    }

    // --- or ---

    @Test
    fun testOr_anyTrue() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            putJsonArray("values") {
                add(false)
                add(true)
            }
        }

        val result = callFunction(resolver, "or", args)

        assertNotNull(result)
        assertEquals(true, result.jsonPrimitive.boolean)
    }

    // --- not ---

    @Test
    fun testNot_true() {
        val resolver = makeResolver()
        val args = buildJsonObject {
            put("value", true)
        }

        val result = callFunction(resolver, "not", args)

        assertNotNull(result)
        assertEquals(false, result.jsonPrimitive.boolean)
    }
}
