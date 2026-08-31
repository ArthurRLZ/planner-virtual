package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ClassroomCourseDto(
    val id: String,
    val name: String
)

@Serializable
data class ClassroomCoursesResponse(
    val courses: List<ClassroomCourseDto> = emptyList()
)

@Serializable
data class ClassroomDateDto(val year: Int, val month: Int, val day: Int)

@Serializable
data class ClassroomTimeDto(val hours: Int = 23, val minutes: Int = 59)

@Serializable
data class ClassroomCourseWorkDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val alternateLink: String,
    val dueDate: ClassroomDateDto? = null,
    val dueTime: ClassroomTimeDto? = null
)

@Serializable
data class ClassroomCourseWorkResponse(
    val courseWork: List<ClassroomCourseWorkDto> = emptyList()
)