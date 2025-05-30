package tests

import jsonpackage.models.JsonArray
import jsonpackage.models.JsonBoolean
import jsonpackage.models.JsonNull
import jsonpackage.models.JsonNull.validateArrayTypes
import jsonpackage.models.JsonNull.validateObjectKeys
import jsonpackage.models.JsonNumber
import jsonpackage.models.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import jsonpackage.models.JsonString
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JsonStructureTests {
    @Test
    fun testJsonArraySerialization() {
        val array = JsonArray(
            listOf(JsonNumber(1), JsonBoolean(false), JsonString("texto"))
        )
        println(array.toJsonString())
        assertEquals("[1, false, \"texto\"]", array.toJsonString())
    }

    @Test
    fun testJsonArrayFilter() {
        val array = JsonArray(
            listOf(JsonNumber(1), JsonNumber(2), JsonNumber(3))
        )
        val filtered = array.filter { (it as JsonNumber).toJsonString().toInt() > 1 }

        val expectedArray = JsonArray(
            listOf( JsonNumber(2 ), JsonNumber(3))
        )
        assertEquals(expectedArray, filtered)
    }

    @Test
    fun testJsonObjectSerialization() {
        val obj = JsonObject(
            mapOf(
                "nome" to JsonString("Francisco"),
                "registado" to JsonBoolean(true),
                "idade" to JsonNumber(30)
            )
        )
        println(obj.toJsonString())
        assertEquals("{\"nome\": \"Francisco\", \"registado\": true, \"idade\": 30}", obj.toJsonString())
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
        println(obj.toJsonString())
        val filtered = obj.filter { (_, v) -> (v as JsonNumber).toJsonString().toInt() >= 2 }
        println(filtered.toJsonString())

        val expectedObject = JsonObject(
            mapOf(
                "b" to JsonNumber(2),
                "c" to JsonNumber(3)
            )
        )
        assertEquals(expectedObject, filtered)
    }

    @Test
    fun testValidateObjectKeys() {
        val validObj = JsonObject(mapOf("nome" to JsonString("Maria")))
        val invalidObj = JsonObject(mapOf("" to JsonString("empty")))
        println(validObj.toJsonString())
        println(invalidObj.toJsonString())
        assertTrue(validObj.validateObjectKeys())
        assertFalse(invalidObj.validateObjectKeys())
    }

    @Test
    fun testValidateArrayTypes() {
        val nullArray = JsonArray(listOf(JsonNumber(1), JsonNumber(2), JsonNull))
        val invalidArray = JsonArray(listOf(JsonNumber(1), JsonString("texto")))
        val validArray = JsonArray(listOf(JsonNumber(1), JsonNumber(2), JsonNumber(3)))
        println(nullArray.toJsonString())
        println(invalidArray.toJsonString())
        println(validArray.toJsonString())
        assertFalse(nullArray.validateArrayTypes())
        assertFalse(invalidArray.validateArrayTypes())
        assertTrue(validArray.validateArrayTypes())
    }

    @Test
    fun testArrayWithMap() {
        val array = JsonArray(
            listOf(
                JsonObject(
                    mapOf(
                        "a" to JsonNumber(1),
                        "b" to JsonNumber(2)
                    )
                ),
                JsonObject(
                    mapOf(
                        "c" to JsonNumber(3),
                        "d" to JsonNumber(4)
                    )
                )
            )
        )
        println(array.toJsonString())
        assertEquals("[{\"a\": 1, \"b\": 2}, {\"c\": 3, \"d\": 4}]", array.toJsonString())
    }

}