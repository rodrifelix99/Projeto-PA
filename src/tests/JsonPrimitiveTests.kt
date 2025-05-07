package tests

import models.JsonBoolean
import models.JsonNull
import models.JsonNumber
import org.junit.jupiter.api.Assertions.assertEquals
import models.JsonString
import org.junit.jupiter.api.Test

class JsonPrimitiveTests {
    @Test
    fun testJsonString() {
        val testString = JsonString("Olá").toJsonString()
        println(testString)
        assertEquals("\"Olá\"", testString)
        assertEquals("\"com \\\"aspas\\\"\"", JsonString("com \"aspas\"").toJsonString())
    }

    @Test
    fun testJsonNumber() {
        val testNumber = JsonNumber(123).toJsonString()
        println(testNumber)
        assertEquals("123", testNumber)
        assertEquals("3.14", JsonNumber(3.14).toJsonString())
    }

    @Test
    fun testJsonBoolean() {
        val testBoolean =JsonBoolean(true).toJsonString()
        println(testBoolean)
        assertEquals("true", testBoolean)
        assertEquals("false", JsonBoolean(false).toJsonString())
    }

    @Test
    fun testJsonNull() {
        assertEquals("null", JsonNull.toJsonString())
    }
}