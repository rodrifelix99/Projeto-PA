package models

sealed class JsonElement {
    abstract fun toJsonString(): String
}
