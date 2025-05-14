package jsonpackage.models

/**
 * JSON Null
 *
 * This class represents the Json Null that Extends Json Element
 *
 */

object JsonNull : JsonElement() {
    override fun toJsonString(): String = "null"
}