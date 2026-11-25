package com.example.a48955baism

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

internal class TaskStorage(private val preferences: SharedPreferences) {
    fun read(): TaskList {
        val saved = JSONArray(preferences.getString(TASKS_KEY, "[]") ?: "[]")
        val tasks = List(saved.length()) { index ->
            val entry = saved.getJSONObject(index)
            val priorityName = entry.optString("priority")
            Task(
                id = entry.getString("id"),
                title = entry.getString("title"),
                description = entry.optString("description"),
                priority = TaskPriority.entries.find { it.name == priorityName } ?: TaskPriority.NORMAL,
                completed = entry.optBoolean("completed")
            )
        }
        return TaskList(tasks)
    }

    fun write(tasks: TaskList) {
        val saved = JSONArray()
        tasks.entries.forEach { task ->
            saved.put(
                JSONObject()
                    .put("id", task.id)
                    .put("title", task.title)
                    .put("description", task.description)
                    .put("priority", task.priority.name)
                    .put("completed", task.completed)
            )
        }
        preferences.edit().putString(TASKS_KEY, saved.toString()).apply()
    }

    private companion object {
        const val TASKS_KEY = "tasks"
    }
}
