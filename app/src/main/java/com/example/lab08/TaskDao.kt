package com.example.lab08

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface TaskDao {


    // Obtener todas las tareas
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>


    // Insertar una nueva tarea
    @Insert
    suspend fun insertTask(task: Task)


    // Marcar una tarea como completada o no completada
    @Update
    suspend fun updateTask(task: Task)
    //Eliminar una tarea
    @Delete
    suspend fun deleteTask(task: Task)

    // Eliminar todas las tareas
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    // Filtrar Tareas
    @Query("SELECT * FROM tasks WHERE description LIKE '%' || :searchQuery || '%'")
    suspend fun searchTasks(searchQuery: String): List<Task>


    @Query("SELECT * FROM tasks WHERE is_completed = :isCompleted")
    suspend fun getTasksByStatus(isCompleted: Boolean): List<Task>

    // Ordenar Tareas por nombre,fecha o estado
    @Query("SELECT * FROM tasks ORDER BY " +
            "CASE WHEN :orderBy = 'description' THEN description END ASC, " +
            "CASE WHEN :orderBy = 'created_at' THEN created_at END ASC, " +
            "CASE WHEN :orderBy = 'status' THEN is_completed END ASC")
    suspend fun getTasksOrdered(orderBy: String): List<Task>
}
