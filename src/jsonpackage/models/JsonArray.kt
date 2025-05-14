package jsonpackage.models

data class JsonArray(
    private val elements: List<JsonElement> = emptyList()
) : JsonElement() {
    fun getElements(): List<JsonElement> = elements

    fun map(transform: (JsonElement) -> JsonElement): JsonArray =
        JsonArray(elements.map(transform))

    fun filter(predicate: (JsonElement) -> Boolean): JsonArray =
        JsonArray(elements.filter(predicate))

    override fun toJsonString(): String =
        elements.joinToString(prefix = "[", postfix = "]") { it.toJsonString() }
}