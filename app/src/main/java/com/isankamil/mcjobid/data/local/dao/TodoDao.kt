package com.isankamil.mcjobid.data.local.dao

import androidx.room.*
import com.isankamil.mcjobid.data.local.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos WHERE ownerId = :ownerId ORDER BY isCompleted ASC, priority = 'TINGGI' DESC, priority = 'SEDANG' DESC, dueDate ASC, createdAt DESC")
    fun observeTodos(ownerId: String): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    suspend fun getTodos(ownerId: String): List<TodoEntity>

    @Query("SELECT * FROM todos WHERE id = :id LIMIT 1")
    suspend fun getTodoById(id: String): TodoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodos(todos: List<TodoEntity>)

    @Update
    suspend fun updateTodo(todo: TodoEntity)

    @Query("UPDATE todos SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun setCompletion(id: String, isCompleted: Boolean, completedAt: Long?)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodoById(id: String)

    @Query("DELETE FROM todos WHERE ownerId = :ownerId AND isCompleted = 1")
    suspend fun deleteCompletedTodos(ownerId: String)

    @Query("DELETE FROM todos WHERE ownerId = :ownerId")
    suspend fun deleteAllByOwner(ownerId: String)

    @Query("DELETE FROM todos WHERE ownerId = :ownerId AND id NOT IN (:ids)")
    suspend fun deleteTodosNotInIds(ownerId: String, ids: List<String>)
}
