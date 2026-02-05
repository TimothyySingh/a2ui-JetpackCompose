package com.a2ui.core.parser

import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class A2UIParserTest {

    @Test
    fun testParseMinimalSurface() {
        val jsonString = """
            {
                "root": "r",
                "components": {
                    "r": {
                        "id": "r",
                        "component": "Text"
                    }
                }
            }
        """.trimIndent()

        val result = A2UIParser.parseDocument(jsonString)

        assertTrue(result.isSuccess)
        val surface = result.getOrThrow()
        assertEquals("r", surface.root)
        assertEquals(1, surface.components.size)
        assertNotNull(surface.components["r"])
        assertEquals("Text", surface.components["r"]!!.component)
    }

    @Test
    fun testParseWithData() {
        val jsonString = """
            {
                "root": "r",
                "components": {
                    "r": {
                        "id": "r",
                        "component": "Text"
                    }
                },
                "data": {
                    "username": "Alice",
                    "count": 42
                }
            }
        """.trimIndent()

        val result = A2UIParser.parseDocument(jsonString)

        assertTrue(result.isSuccess)
        val surface = result.getOrThrow()
        assertEquals("Alice", surface.data["username"]?.jsonPrimitive?.content)
        assertEquals(42, surface.data["count"]?.jsonPrimitive?.int)
    }

    @Test
    fun testParseWithTheme() {
        val jsonString = """
            {
                "root": "r",
                "components": {
                    "r": {
                        "id": "r",
                        "component": "Text"
                    }
                },
                "theme": {
                    "primaryColor": "#FF5733",
                    "agentDisplayName": "TestAgent"
                }
            }
        """.trimIndent()

        val result = A2UIParser.parseDocument(jsonString)

        assertTrue(result.isSuccess)
        val surface = result.getOrThrow()
        assertNotNull(surface.theme)
        assertEquals("#FF5733", surface.theme!!.primaryColor)
        assertEquals("TestAgent", surface.theme!!.agentDisplayName)
    }

    @Test
    fun testParseInvalid() {
        val garbage = "this is not json at all {{{}"

        val result = A2UIParser.parseDocument(garbage)

        assertTrue(result.isFailure)
    }

    @Test
    fun testParseJsonlCommands() {
        val jsonl = """
            {"op":"add","target":"main","component":{"id":"btn1","component":"Button"}}
            {"op":"remove","target":"btn1"}
        """.trimIndent()

        val commands = A2UIParser.parseJsonl(jsonl)

        assertEquals(2, commands.size)
        assertEquals(A2UIOperation.ADD, commands[0].op)
        assertEquals("main", commands[0].target)
        assertNotNull(commands[0].component)
        assertEquals("btn1", commands[0].component!!.id)
        assertEquals("Button", commands[0].component!!.component)
        assertEquals(A2UIOperation.REMOVE, commands[1].op)
        assertEquals("btn1", commands[1].target)
    }

    @Test
    fun testParseIgnoresUnknownKeys() {
        val jsonString = """
            {
                "root": "r",
                "components": {
                    "r": {
                        "id": "r",
                        "component": "Text",
                        "unknownField": "should be ignored",
                        "anotherUnknown": 123
                    }
                },
                "extraTopLevel": true
            }
        """.trimIndent()

        val result = A2UIParser.parseDocument(jsonString)

        assertTrue(result.isSuccess)
        val surface = result.getOrThrow()
        assertEquals("r", surface.root)
        assertEquals("Text", surface.components["r"]!!.component)
    }
}
