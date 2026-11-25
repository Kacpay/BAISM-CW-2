package com.example.a48955baism

import java.util.UUID

internal enum class TaskPriority {
    LOW, NORMAL, HIGH
}

internal data class Task(
    val id: String,
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.NORMAL,
    val completed: Boolean = false
)

internal class TaskList(val entries: List<Task> = emptyList()) {
    fun add(
        title: String,
        description: String = "",
        priority: TaskPriority = TaskPriority.NORMAL
    ): TaskList {
        val task = Task(UUID.randomUUID().toString(), cleanTitle(title), description.trim(), priority)
        return TaskList(entries + task)
    }

    fun update(id: String, title: String, description: String, priority: TaskPriority): TaskList {
        val updatedTitle = cleanTitle(title)
        return change(id) { it.copy(title = updatedTitle, description = description.trim(), priority = priority) }
    }

    fun setCompleted(id: String, completed: Boolean): TaskList =
        change(id) { it.copy(completed = completed) }

    fun remove(id: String): TaskList = TaskList(entries.filterNot { it.id == id })

    private fun change(id: String, transform: (Task) -> Task): TaskList =
        TaskList(entries.map { task -> if (task.id == id) transform(task) else task })

    private fun cleanTitle(title: String): String {
        val trimmed = title.trim()
        require(trimmed.isNotEmpty()) { "A task title must not be blank." }
        return trimmed
    }
}
