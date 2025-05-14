package jsonpackage.models

/**
 * JSON Number
 *
 * This class represents the Json Number that Extends Json Element
 *
 */

data class JsonNumber(val value: Number) : JsonElement() {
    override fun toJsonString(): String = value.toString()
}