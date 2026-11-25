package com.example.a48955baism

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.a48955baism.ui.theme._48955baismTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val storage = TaskStorage(getPreferences(MODE_PRIVATE))
        setContent {
            _48955baismTheme {
                var tasks by remember { mutableStateOf(storage.read()) }
                TaskScreen(tasks) { updatedTasks ->
                    storage.write(updatedTasks)
                    tasks = updatedTasks
                }
            }
        }
    }
}

@Composable
private fun TaskScreen(tasks: TaskList, onChange: (TaskList) -> Unit) {
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            Button(
                onClick = {
                    editId = null
                    editorOpen = true
                },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text(stringResource(R.string.add_task))
            }

            if (tasks.entries.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_tasks))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks.entries, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onCompletedChange = { completed ->
                                onChange(tasks.setCompleted(task.id, completed))
                            },
                            onEdit = {
                                editId = task.id
                                editorOpen = true
                            },
                            onDelete = { deleteId = task.id }
                        )
                    }
                }
            }
        }
    }

    if (editorOpen) {
        val selected = tasks.entries.find { it.id == editId }
        TaskEditor(
            task = selected,
            onDismiss = { editorOpen = false },
            onSave = { title, description, priority ->
                val updated = if (selected == null) {
                    tasks.add(title, description, priority)
                } else {
                    tasks.update(selected.id, title, description, priority)
                }
                onChange(updated)
                editorOpen = false
            }
        )
    }

    tasks.entries.find { it.id == deleteId }?.let { task ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text(stringResource(R.string.delete_task_title)) },
            text = { Text(task.title) },
            confirmButton = {
                TextButton(onClick = {
                    onChange(tasks.remove(task.id))
                    deleteId = null
                }) { Text(stringResource(R.string.delete_task)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteId = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onCompletedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (task.completed) TextDecoration.LineThrough else null
            )
            if (task.description.isNotEmpty()) {
                Text(task.description, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = stringResource(R.string.task_priority_value, priorityLabel(task.priority)),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth().toggleable(
                    value = task.completed,
                    role = Role.Checkbox,
                    onValueChange = onCompletedChange
                ).padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Checkbox(checked = task.completed, onCheckedChange = null)
                Text(stringResource(R.string.task_completed))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text(stringResource(R.string.edit_task)) }
                TextButton(onClick = onDelete) { Text(stringResource(R.string.delete_task)) }
            }
        }
    }
}

@Composable
private fun TaskEditor(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (String, String, TaskPriority) -> Unit
) {
    var title by rememberSaveable(task?.id) { mutableStateOf(task?.title.orEmpty()) }
    var description by rememberSaveable(task?.id) { mutableStateOf(task?.description.orEmpty()) }
    var priority by rememberSaveable(task?.id) { mutableStateOf(task?.priority ?: TaskPriority.NORMAL) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (task == null) R.string.add_task else R.string.edit_task))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.task_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.task_description)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.task_priority), style = MaterialTheme.typography.labelLarge)
                Column(Modifier.selectableGroup()) {
                    TaskPriority.entries.forEach { option ->
                        Row(
                            modifier = Modifier.fillMaxWidth().selectable(
                                selected = option == priority,
                                onClick = { priority = option },
                                role = Role.RadioButton
                            ).padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(selected = option == priority, onClick = null)
                            Text(priorityLabel(option))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, description, priority) }, enabled = title.isNotBlank()) {
                Text(stringResource(R.string.save_task))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun priorityLabel(priority: TaskPriority): String = stringResource(
    when (priority) {
        TaskPriority.LOW -> R.string.priority_low
        TaskPriority.NORMAL -> R.string.priority_normal
        TaskPriority.HIGH -> R.string.priority_high
    }
)
