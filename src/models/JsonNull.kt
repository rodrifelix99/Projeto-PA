package models

object JsonNull : JsonElement() {
    override fun toJsonString(): String = "null"
}