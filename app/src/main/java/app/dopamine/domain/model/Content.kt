package app.dopamine.domain.model

import app.dopamine.domain.content.ContentContract
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
    val overview: Overview? = null,
)

/** The "course landing page": what it is, what you will learn, what you need, how long. */
@Serializable
data class Overview(
    val summary: String = "",
    val outcomes: List<String> = emptyList(),
    val requirements: List<String> = emptyList(),
    val minutes: Int = 0,
)

@Serializable
data class UnitDto(
    val id: String,
    val title: String,
    val order: Int,
    val lessons: List<LessonRef>,
    val notes: NotesRef? = null,
)

/** Where a unit's cheat sheet lives. Verified by sha256 like a lesson. */
@Serializable
data class NotesRef(
    val id: String,
    val title: String,
    val path: String,
    val version: Int,
    val sha256: String,
    val sizeBytes: Long,
    val minApp: Int = 1,
)

/** The unit cheat sheet: things to look up. block kind: text | formula | list | tip. */
@Serializable
data class Notes(val id: String, val version: Int, val title: String, val blocks: List<NoteBlock>, val minApp: Int = 1)

@Serializable
data class NoteBlock(
    val id: String,
    val kind: String,
    val title: String? = null,
    val body: String? = null,
    val items: List<String>? = null,
    val code: String? = null,
    val language: String? = null,
)

@Serializable
data class LessonRef(
    val id: String,
    val title: String,
    val path: String,
    val version: Int,
    val sha256: String,
    val sizeBytes: Long,
    val order: Int,
    /** Lowest app versionCode that can show this lesson. Older apps show it as locked. */
    val minApp: Int = 1,
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
    /** Reading pages shown before the practice. Optional so older lessons still work. */
    val learn: List<LearnPage> = emptyList(),
    val minApp: Int = 1,
)

/** One page of reading. kind: text | example (title + steps revealed one by one) | tip. */
@Serializable
data class LearnPage(
    val id: String,
    val kind: String,
    val title: String? = null,
    val body: String? = null,
    val steps: List<String>? = null,
    val image: String? = null,
    val code: String? = null,
    val language: String? = null,
    val alt: String? = null,
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
    /** What the picture shows, for screen readers. Required whenever [image] is set. */
    val alt: String? = null,
)

val KNOWN_NOTE_KINDS = ContentContract.NOTE_KINDS.filterValues { it <= ContentContract.APP_VERSION }.keys
val KNOWN_PAGE_KINDS = ContentContract.PAGE_KINDS.filterValues { it <= ContentContract.APP_VERSION }.keys
val KNOWN_QUESTION_TYPES = ContentContract.QUESTION_TYPES.filterValues { it <= ContentContract.APP_VERSION }.keys
