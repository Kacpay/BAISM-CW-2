package com.example.a48955baism

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskListTest {
    @Test
    fun newListIsEmpty() {
        assertTrue(TaskList().entries.isEmpty())
    }

    @Test
    fun addReturnsTasksInOrderWithDistinctIds() {
        val tasks = TaskList().add("  MobSF  ").add("Bitrise").entries

        assertEquals(listOf("Bitrise", "MobSF"), tasks.map { it.title })
        assertNotEquals(tasks[0].id, tasks[1].id)
        assertEquals(TaskPriority.NORMAL, tasks[0].priority)
        assertEquals("", tasks[0].description)
        assertFalse(tasks[0].completed)
    }

    @Test
    fun addStoresTheDescriptionAndPriority() {
        val added = TaskList().add("MobSF", "  Zeskanować APK  ", TaskPriority.HIGH).entries.single()

        assertEquals("Zeskanować APK", added.description)
        assertEquals(TaskPriority.HIGH, added.priority)
        assertFalse(added.completed)
    }

    @Test
    fun updateChangesOnlyTheSelectedTaskAndKeepsItsIdAndStatus() {
        val original = TaskList().add("Skan").add("Skan")
        val id = original.entries[0].id
        val completed = original.setCompleted(id, true)

        val updated = completed.update(id, "  Skan APK  ", "  Sprawdzić wyniki  ", TaskPriority.LOW)

        assertEquals(Task(id, "Skan APK", "Sprawdzić wyniki", TaskPriority.LOW, completed = true), updated.entries[0])
        assertEquals(original.entries[1], updated.entries[1])
        assertEquals("Skan", original.entries[0].title)
    }

    @Test
    fun setCompletedCanCompleteAndReopenOnlyTheSelectedTask() {
        val original = TaskList().add("MobSF").add("Bitrise")
        val id = original.entries[0].id
        val completed = original.setCompleted(id, true)

        assertTrue(completed.entries[0].completed)
        assertFalse(completed.entries[1].completed)
        assertEquals(original.entries, completed.setCompleted(id, false).entries)
    }

    @Test
    fun removeDeletesOnlyTheSelectedTask() {
        val original = TaskList().add("Skan").add("Skan")
        val updated = original.remove(original.entries[0].id).entries

        assertEquals(listOf(original.entries[1]), updated)
    }

    @Test(expected = IllegalArgumentException::class)
    fun addRejectsBlankTitle() {
        TaskList().add("   ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateRejectsBlankTitle() {
        val tasks = TaskList().add("MobSF")

        tasks.update(tasks.entries.single().id, "   ", "", TaskPriority.NORMAL)
    }
}
