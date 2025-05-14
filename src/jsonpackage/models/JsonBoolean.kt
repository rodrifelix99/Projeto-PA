package jsonpackage.models

data class JsonBoolean(val value: Boolean) : JsonElement() {
    override fun toJsonString(): String = value.toString()
}