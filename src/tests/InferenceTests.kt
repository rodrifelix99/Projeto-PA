package tests

import jsonpackage.models.JsonArray
import jsonpackage.models.JsonBoolean
import jsonpackage.models.JsonElement
import jsonpackage.models.JsonNull
import jsonpackage.models.JsonNumber
import jsonpackage.models.JsonObject
import jsonpackage.models.JsonString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import jsonpackage.toJsonElement

enum class EvalType {
    TEST, PROJECT, EXAM
}

data class EvalItem(
    val name: String,
    val percentage: Double,
    val mandatory: Boolean,
    val type: EvalType?
)

data class Course(
    val name: String,
    val credits: Int,
    val evaluation: List<EvalItem>
)

class InferenceTests() {
    @Test
    fun testInferenceCourse() {
        val course = Course(
            "PA", 6, listOf(
                EvalItem("quizzes", 0.2, false, null),
                EvalItem("project", 0.8, true, EvalType.PROJECT)
            )
        )
        val json: JsonElement = toJsonElement(course)
        val expected: JsonElement = JsonObject(
            mapOf(
                "name" to JsonString("PA"),
                "credits" to JsonNumber(6),
                "evaluation" to JsonArray(
                    listOf(
                        JsonObject(
                            mapOf(
                                "name" to JsonString("quizzes"),
                                "percentage" to JsonNumber(0.2),
                                "mandatory" to JsonBoolean(false),
                                "type" to JsonNull
                            )
                        ),
                        JsonObject(
                            mapOf(
                                "name" to JsonString("project"),
                                "percentage" to JsonNumber(0.8),
                                "mandatory" to JsonBoolean(true),
                                "type" to JsonString("PROJECT")
                            )
                        )
                    )
                )
            )
        )
        assertEquals(expected, json)

        println("json: ${json.toJsonString()}")
        println("expected: ${expected.toJsonString()}")
    }
}
