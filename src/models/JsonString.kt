package models

data class JsonString(val value: String) : JsonElement() {
    override fun toJsonString(): String = "\"${value.replace("\"", "\\\"")}\""
}