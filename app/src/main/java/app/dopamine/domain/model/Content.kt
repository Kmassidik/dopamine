package app.dopamine.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Manifest(
    val schemaVersion: Int,
    val contentVersion: String,
    val minAppVersion: Int,
    val categories: List<CategoryDto>,
    val courses: List<CourseDto>,
    val images: List<ImageDto> = emptyList(),
)

@Serializable
data class CategoryDto(val id: String, val name: String, val icon: String, val color: String, val order: Int)

@Serializable
data class CourseDto(
    val id: String,
    val categoryId: String,
    val title: String,
    val description: String = "",
    val order: Int,
    val units: List<UnitDto>,
)

@Serializable
data class UnitDto(val id: String, val title: String, val order: Int, val lessons: List<LessonRef>)

@Serializable
data class LessonRef(
    val id: String,
    val title: String,
    val path: String,
    val version: Int,
    val sha256: String,
    val sizeBytes: Long,
    val order: Int,
)

@Serializable
data class ImageDto(val path: String, val sha256: String, val sizeBytes: Long)

@Serializable
data class Lesson(
    val id: String,
    val version: Int,
    val title: String,
    val xp: Int,
    val tags: List<String> = emptyList(),
    val questions: List<Question>,
)

/** Flat on purpose: unknown question types still parse and are skipped by the player. */
@Serializable
data class Question(
    val id: String,
    val type: String,
    val prompt: String? = null,
    val choices: List<String>? = null,
    val answerIndex: Int? = null,
    val accepted: List<String>? = null,
    val keypad: String? = null,
    val pairs: List<List<String>>? = null,
    val steps: List<String>? = null,
    val explanation: String = "",
    val image: String? = null,
)

val KNOWN_QUESTION_TYPES = setOf("multiple_choice", "type_answer", "fill_blank", "match_pairs", "order_steps")
