package jsonpackage.models

data class JsonObject(
    private val map: Map<String, JsonElement> = emptyMap()
) : JsonElement() {
    fun getMap(): Map<String, JsonElement> = map

    fun filter(predicate: (Map.Entry<String, JsonElement>) -> Boolean): JsonObject =
        JsonObject(map.filter(predicate))

    override fun toJsonString(): String =
        map.entries.joinToString(prefix = "{", postfix = "}") {
            "\"${it.key}\": ${it.value.toJsonString()}"
        }
}