package jsonpackage

import jsonpackage.models.JsonArray
import jsonpackage.models.JsonBoolean
import jsonpackage.models.JsonElement
import jsonpackage.models.JsonNull
import jsonpackage.models.JsonNumber
import jsonpackage.models.JsonObject
import jsonpackage.models.JsonString
import kotlin.reflect.full.declaredMemberProperties

/**
* Converts a Kotlin object to a JSON element.
*
* This function uses reflection to inspect the properties of the object and convert them to JSON.
* It supports primitive types, lists, maps, and data classes.
*
* @param value The Kotlin object to convert.
* @return A JsonElement representing the object in JSON format.
*/
fun toJsonElement(value: Any?): JsonElement {
    return when (value) {
        null -> JsonNull
        is String -> JsonString(value)
        is Int, is Double -> JsonNumber(value as Number)
        is Boolean -> JsonBoolean(value)
        is Enum<*> -> JsonString(value.name)
        is List<*> -> JsonArray(value.map { toJsonElement(it) })
        is Map<*, *> -> JsonObject(
            value.mapNotNull { (k, v) ->
                if (k is String) k to toJsonElement(v) else null
            }.toMap()
        )
        else -> {
            val kClass = value::class
            require(kClass.isData) { "Only data classes are allowed. Got: ${kClass.simpleName}" }

            val props = kClass.declaredMemberProperties.associate { prop ->
                prop.name to toJsonElement(prop.getter.call(value))
            }

            JsonObject(props)
        }
    }
}
