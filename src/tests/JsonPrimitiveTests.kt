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
        assertEquals("\"Olá\"", JsonString("Olá").toJsonString())
        assertEquals("\"com \\\"aspas\\\"\"", JsonString("com \"aspas\"").toJsonString())
    }

    @Test
    fun testJsonNumber() {
        assertEquals("123", JsonNumber(123).toJsonString())
        assertEquals("3.14", JsonNumber(3.14).toJsonString())
    }

    @Test
    fun testJsonBoolean() {
        assertEquals("true", JsonBoolean(true).toJsonString())
        assertEquals("false", JsonBoolean(false).toJsonString())
    }

    @Test
    fun testJsonNull() {
        assertEquals("null", JsonNull.toJsonString())
    }
}