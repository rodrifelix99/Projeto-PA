package jsonpackage.models

/**
 * JSON Number
 *
 * This class represents the Json Number that Extends Json Element
 *
 * @property value The number value of the JSON element.
 */

data class JsonNumber(val value: Number) : JsonElement() {
    override fun toJsonString(): String = value.toString()
}