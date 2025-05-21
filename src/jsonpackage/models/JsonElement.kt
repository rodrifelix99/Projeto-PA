package jsonpackage.models

/**
 * JSON Element
 *
 * This class represents the Json Element
 *
 */

sealed class JsonElement {
    /**
     * Converts the JSON element to a JSON string representation.
     *
     * @return The JSON string representation of the element.
     */
    abstract fun toJsonString(): String

    fun JsonElement.accept(visitor: (JsonElement) -> Unit) {
        visitor(this)
        when (this) {
            is JsonArray -> getElements().forEach { it.accept(visitor) }
            is JsonObject -> getMap().values.forEach { it.accept(visitor) }
            else -> {}
        }
    }

    fun JsonElement.validateObjectKeys(): Boolean {
        var valid = true
        this.accept {
            if (it is JsonObject) {
                if (it.getMap().keys.any { key -> key.isBlank() })
                    valid = false
            }
        }
        return valid
    }

    fun JsonElement.validateArrayTypes(): Boolean {
        var valid = true
        this.accept {
            if (it is JsonArray) {
                val firstType = it.getElements().firstOrNull()?.javaClass
                if (it.getElements().any { element -> element.javaClass != firstType || element is JsonNull }) {
                    valid = false
                }
            }
        }
        return valid
    }

}
