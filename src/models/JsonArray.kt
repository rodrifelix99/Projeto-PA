package models

class JsonArray(
    private val list: List<JsonElement> = emptyList()
) : JsonElement() {

    fun map(transform: (JsonElement) -> JsonElement): JsonArray =
        JsonArray(list.map(transform))

    fun filter(predicate: (JsonElement) -> Boolean): JsonArray =
        JsonArray(list.filter(predicate))

    override fun toJsonString(): String =
        list.joinToString(prefix = "[", postfix = "]") { it.toJsonString() }
}