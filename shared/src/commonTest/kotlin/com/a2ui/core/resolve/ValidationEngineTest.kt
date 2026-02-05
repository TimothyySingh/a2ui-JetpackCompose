package com.a2ui.core.resolve

import com.a2ui.core.model.A2UICheck
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidationEngineTest {

    private fun makeResolver(data: JsonObject = JsonObject(emptyMap())): DynamicResolver {
        return DynamicResolver(ResolverContext(data))
    }

    @Test
    fun testValidateEmptyChecks() {
        val resolver = makeResolver()
        val engine = ValidationEngine(resolver)

        val result = engine.validate(null)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())

        val resultEmpty = engine.validate(emptyList())

        assertTrue(resultEmpty.isValid)
        assertTrue(resultEmpty.errors.isEmpty())
    }

    @Test
    fun testValidateAllPass() {
        val resolver = makeResolver()
        val engine = ValidationEngine(resolver)

        val checks = listOf(
            A2UICheck(
                condition = JsonPrimitive(true),
                message = JsonPrimitive("Should not appear")
            ),
            A2UICheck(
                condition = JsonPrimitive(true),
                message = JsonPrimitive("Also should not appear")
            )
        )

        val result = engine.validate(checks)

        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun testValidateOneFails() {
        val resolver = makeResolver()
        val engine = ValidationEngine(resolver)

        val checks = listOf(
            A2UICheck(
                condition = JsonPrimitive(true),
                message = JsonPrimitive("This passes")
            ),
            A2UICheck(
                condition = JsonPrimitive(false),
                message = JsonPrimitive("This field is required")
            )
        )

        val result = engine.validate(checks)

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        assertEquals("This field is required", result.errors[0])
    }

    @Test
    fun testValidateMultipleFail() {
        val resolver = makeResolver()
        val engine = ValidationEngine(resolver)

        val checks = listOf(
            A2UICheck(
                condition = JsonPrimitive(false),
                message = JsonPrimitive("Error one")
            ),
            A2UICheck(
                condition = JsonPrimitive(false),
                message = JsonPrimitive("Error two")
            )
        )

        val result = engine.validate(checks)

        assertFalse(result.isValid)
        assertEquals(2, result.errors.size)
        assertEquals("Error one", result.errors[0])
        assertEquals("Error two", result.errors[1])
    }

    @Test
    fun testValidateWithFunctionCondition() {
        val data = buildJsonObject {
            put("name", "")
        }
        val resolver = makeResolver(data)
        val engine = ValidationEngine(resolver)

        val condition = buildJsonObject {
            put("call", "required")
            putJsonObject("args") {
                put("value", "/name")
            }
        }

        val checks = listOf(
            A2UICheck(
                condition = condition,
                message = JsonPrimitive("Name is required")
            )
        )

        val result = engine.validate(checks)

        assertFalse(result.isValid)
        assertEquals(1, result.errors.size)
        assertEquals("Name is required", result.errors[0])
    }
}
