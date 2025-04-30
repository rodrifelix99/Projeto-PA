package models

class JsonBoolean(val value: Boolean) : JsonElement() {
    override fun toJsonString(): String = value.toString()
}