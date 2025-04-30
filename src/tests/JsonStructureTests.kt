package tests

import models.JsonArray
import models.JsonBoolean
import models.JsonNull
import models.JsonNull.validateArrayTypes
import models.JsonNull.validateObjectKeys
import models.JsonNumber
import models.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import models.JsonString
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JsonStructureTests {
    @Test
    fun testJsonArraySerialization() {
        val array = JsonArray(
            listOf(JsonNumber(1), JsonBoolean(false), JsonString("texto"))
        )
        assertEquals("[1, false, \"texto\"]", array.toJsonString())
    }

    @Test
    fun testJsonArrayFilter() {
        val array = JsonArray(
            listOf(JsonNumber(1), JsonNumber(2), JsonNumber(3))
        )
        val filtered = array.filter { (it as JsonNumber).toJsonString().toInt() > 1 }
        assertEquals("[2, 3]", filtered.toJsonString())
    }

    @Test
    fun testJsonObjectSerialization() {
        val obj = JsonObject(
            mapOf(
                "nome" to JsonString("André"),
                "ativo" to JsonBoolean(true),
                "idade" to JsonNumber(30)
            )
        )
        assertEquals("{\"nome\": \"André\", \"ativo\": true, \"idade\": 30}", obj.toJsonString())
    }

    @Test
    fun testJsonObjectFilter() {
        val obj = JsonObject(
            mapOf(
                "a" to JsonNumber(1),
                "b" to JsonNumber(2),
                "c" to JsonNumber(3)
            )
        )
        val filtered = obj.filter { (_, v) -> (v as JsonNumber).toJsonString().toInt() >= 2 }
        assertEquals("{\"b\": 2, \"c\": 3}", filtered.toJsonString())
    }

    @Test
    fun testValidateObjectKeys() {
        val validObj = JsonObject(mapOf("nome" to JsonString("Ana")))
        val invalidObj = JsonObject(mapOf("" to JsonString("vazio")))
        assertTrue(validObj.validateObjectKeys())
        assertFalse(invalidObj.validateObjectKeys())
    }

    @Test
    fun testValidateArrayTypes() {
        val validArray = JsonArray(listOf(JsonNumber(1), JsonNumber(2), JsonNull))
        val invalidArray = JsonArray(listOf(JsonNumber(1), JsonString("texto")))
        assertTrue(validArray.validateArrayTypes())
        assertFalse(invalidArray.validateArrayTypes())
    }

}