package jsonpackage.models

/**
 * JSON Boolean
 *
 * This class represents the Json Boolean that Extends Json Element
 * @param
 */

data class JsonBoolean(val value: Boolean) : JsonElement() {
    override fun toJsonString(): String = value.toString()
}