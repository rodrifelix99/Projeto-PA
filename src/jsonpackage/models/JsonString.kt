package jsonpackage.models

/**
 * JSON String
 *
 * This class represents the Json String that Extends Json Element
 *
 */


data class JsonString(val value: String) : JsonElement() {
    override fun toJsonString(): String = "\"${value.replace("\"", "\\\"")}\""
}