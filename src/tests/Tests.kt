package tests

import org.junit.jupiter.api.Assertions.assertEquals
import models.JsonString
import org.junit.jupiter.api.Test

class Tests {
    @Test
    fun testJsonStringSerialization() {
        val json = JsonString("Olá")
        assertEquals("\"Olá\"", json.toJsonString())
    }
}