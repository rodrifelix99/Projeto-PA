package models

class JsonNumber(val value: Number) : JsonElement() {
    override fun toJsonString(): String = value.toString()
}