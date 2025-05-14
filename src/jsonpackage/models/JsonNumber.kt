package jsonpackage.models

data class JsonNumber(val value: Number) : JsonElement() {
    override fun toJsonString(): String = value.toString()
}