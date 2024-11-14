package com.example.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.lab08.ui.theme.Lab08Theme
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                val db = Room.databaseBuilder(
                    applicationContext,
                    TaskDatabase::class.java,
                    "task_db"
                )
                    .fallbackToDestructiveMigration() // Esto eliminará y recreará la base de datos
                    .build()

                val taskDao = db.taskDao()
                val viewModel = TaskViewModel(taskDao)

                TaskScreen(viewModel)

            }
        }
    }
}
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var newTaskDescription by remember { mutableStateOf("") }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF3E0))
            .padding(16.dp)
    ) {
        // Campo para agregar nueva tarea
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newTaskDescription,
                onValueChange = { newTaskDescription = it },
                label = { Text("Nueva tarea") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (newTaskDescription.isNotEmpty()) {
                        viewModel.addTask(newTaskDescription)
                        newTaskDescription = ""
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            label = { Text("Buscar tareas") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, "Buscar") }
        )

        // Filtros y ordenamiento
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Filtros
            Row {
                FilterChip(
                    selected = filterStatus == null,
                    onClick = { viewModel.setFilterStatus(null) },
                    label = { Text("Todas") }
                )
                Spacer(Modifier.width(4.dp))
                FilterChip(
                    selected = filterStatus == true,
                    onClick = { viewModel.setFilterStatus(true) },
                    label = { Text("Completadas") }
                )
                Spacer(Modifier.width(4.dp))
                FilterChip(
                    selected = filterStatus == false,
                    onClick = { viewModel.setFilterStatus(false) },
                    label = { Text("Pendientes") }
                )
            }

            // Menú de ordenamiento
            var expanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowUpward, "Ordenar")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Por nombre") },
                        onClick = {
                            viewModel.setSortOrder("description")
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Por fecha") },
                        onClick = {
                            viewModel.setSortOrder("created_at")
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Por estado") },
                        onClick = {
                            viewModel.setSortOrder("status")
                            expanded = false
                        }
                    )
                }
            }
        }

        // Lista de tareas
        LazyColumn {
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onToggleComplete = { viewModel.toggleTaskCompletion(task) },
                    onEdit = {
                        editingTask = task
                        showEditDialog = true
                    },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
        }

        // Botón para eliminar todas las tareas
        Button(
            onClick = { coroutineScope.launch { viewModel.deleteAllTasks() } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Eliminar todas las tareas")
        }

        // Diálogo de edición
        if (showEditDialog && editingTask != null) {
            var editedDescription by remember { mutableStateOf(editingTask!!.description) }
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Editar tarea") },
                text = {
                    TextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Descripción") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateTaskDescription(editingTask!!, editedDescription)
                        showEditDialog = false
                    }) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    Button(onClick = { showEditDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
@Composable
fun TaskItem(
    task: Task,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.isCompleted)
                        TextDecoration.LineThrough else null
                )
                Text(
                    text = "Creada: ${formatDate(task.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar")
                }
                IconButton(onClick = onToggleComplete) {
                    Icon(
                        if (task.isCompleted) Icons.Default.CheckCircle
                        else Icons.Default.RadioButtonUnchecked,
                        "Completar"
                    )
                }
            }
        }
    }
}
fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}