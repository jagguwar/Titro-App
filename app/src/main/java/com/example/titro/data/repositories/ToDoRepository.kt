package com.example.titro.data.repositories

import com.example.titro.data.ToDoDao
import com.example.titro.data.models.ToDoNote
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class ToDoRepository @Inject constructor(private val toDoDao: ToDoDao) {

    val getAllNotes: Flow<List<ToDoNote>> = toDoDao.getAllNotes()
    val sortByLowPriority: Flow<List<ToDoNote>> = toDoDao.sortByLowPriority()
    val sortByHighPriority: Flow<List<ToDoNote>> = toDoDao.sortByHighPriority()

    fun getSelectedNote(noteId: Int): Flow<ToDoNote> {
        return toDoDao.getSelectedNote(noteId = noteId)
    }

    suspend fun addNote(toDoNote: ToDoNote) {
        toDoDao.addNote(toDoNote = toDoNote)
    }

    suspend fun updateNote(toDoNote: ToDoNote) {
        toDoDao.updateNote(toDoNote = toDoNote)
    }

    suspend fun deleteNote(toDoNote: ToDoNote) {
        toDoDao.deleteNote(toDoNote = toDoNote)
    }

    suspend fun deleteAllNotes() {
        toDoDao.deleteAllNotes()
    }

    fun searchDatabase(searchQuery: String): Flow<List<ToDoNote>> {
        return toDoDao.searchDatabase(searchQuery = searchQuery)
    }

}