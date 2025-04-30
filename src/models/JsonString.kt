package models

class JsonString(val value: String) : JsonElement() {
    override fun toJsonString(): String = "\"${value.replace("\"", "\\\"")}\""
}