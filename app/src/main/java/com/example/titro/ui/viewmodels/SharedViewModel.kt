package com.example.titro.ui.viewmodels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.titro.data.models.Priority
import com.example.titro.data.models.ToDoNote
import com.example.titro.data.repositories.DataStoreRepository
import com.example.titro.data.repositories.ToDoRepository
import com.example.titro.util.Action
import com.example.titro.util.Constants.MAX_TITLE_LENGTH
import com.example.titro.util.RequestState
import com.example.titro.util.SearchAppBarState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val repository: ToDoRepository,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {

    val action: MutableState<Action> = mutableStateOf(Action.NO_ACTION)

    val id: MutableState<Int> = mutableStateOf(0)
    val title: MutableState<String> = mutableStateOf("")
    val description: MutableState<String> = mutableStateOf("")
    val priority: MutableState<Priority> = mutableStateOf(Priority.LOW)

    val searchAppBarState: MutableState<SearchAppBarState> =
        mutableStateOf(SearchAppBarState.CLOSED)
    val searchTextState: MutableState<String> = mutableStateOf("")

    private val _allNotes =
        MutableStateFlow<RequestState<List<ToDoNote>>>(RequestState.Idle)
    val allNotes: StateFlow<RequestState<List<ToDoNote>>> = _allNotes

    private val _searchedNotes =
        MutableStateFlow<RequestState<List<ToDoNote>>>(RequestState.Idle)
    val searchedNotes: StateFlow<RequestState<List<ToDoNote>>> = _searchedNotes

    private val _sortState =
        MutableStateFlow<RequestState<Priority>>(RequestState.Idle)
    val sortState: StateFlow<RequestState<Priority>> = _sortState

    init {
        getAllNotes()
        readSortState()
    }

    fun searchDatabase(searchQuery: String) {
        _searchedNotes.value = RequestState.Loading
        try {
            viewModelScope.launch {
                repository.searchDatabase(searchQuery = "%$searchQuery%")
                    .collect { searchedNotes ->
                        _searchedNotes.value = RequestState.Success(searchedNotes)
                    }
            }
        } catch (e: Exception) {
            _searchedNotes.value = RequestState.Error(e)
        }
        searchAppBarState.value = SearchAppBarState.TRIGGERED
    }

    val lowPriorityNotes: StateFlow<List<ToDoNote>> =
        repository.sortByLowPriority.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    val highPriorityNotes: StateFlow<List<ToDoNote>> =
        repository.sortByHighPriority.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    private fun readSortState() {
        _sortState.value = RequestState.Loading
        try {
            viewModelScope.launch {
                dataStoreRepository.readSortState
                    .map { Priority.valueOf(it) }
                    .collect {
                        _sortState.value = RequestState.Success(it)
                    }
            }
        } catch (e: Exception) {
            _sortState.value = RequestState.Error(e)
        }
    }

    fun persistSortState(priority: Priority) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.persistSortState(priority = priority)
        }
    }

    private fun getAllNotes() {
        _allNotes.value = RequestState.Loading
        try {
            viewModelScope.launch {
                repository.getAllNotes.collect {
                    _allNotes.value = RequestState.Success(it)
                }
            }
        } catch (e: Exception) {
            _allNotes.value = RequestState.Error(e)
        }
    }

    private val _selectedNote: MutableStateFlow<ToDoNote?> = MutableStateFlow(null)
    val selectedNote: StateFlow<ToDoNote?> = _selectedNote

    fun getSelectedNote(noteId: Int) {
        viewModelScope.launch {
            repository.getSelectedNote(noteId = noteId).collect { note ->
                _selectedNote.value = note
            }
        }
    }

    private fun addNote() {
        viewModelScope.launch(Dispatchers.IO) {
            val toDoNote = ToDoNote(
                title = title.value,
                description = description.value,
                priority = priority.value
            )
            repository.addNote(toDoNote = toDoNote)
        }
        searchAppBarState.value = SearchAppBarState.CLOSED
    }

    private fun updateNote() {
        viewModelScope.launch(Dispatchers.IO) {
            val toDoNote = ToDoNote(
                id = id.value,
                title = title.value,
                description = description.value,
                priority = priority.value
            )
            repository.updateNote(toDoNote = toDoNote)
        }
    }

    private fun deleteNote() {
        viewModelScope.launch(Dispatchers.IO) {
            val toDoNote = ToDoNote(
                id = id.value,
                title = title.value,
                description = description.value,
                priority = priority.value
            )
            repository.deleteNote(toDoNote = toDoNote)
        }
    }

    private fun deleteAllNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllNotes()
        }
    }

    fun handleDatabaseActions(action: Action) {
        when (action) {
            Action.ADDED -> {
                addNote()
            }
            Action.UPDATED -> {
                updateNote()
            }
            Action.DELETED -> {
                deleteNote()
            }
            Action.DELETE_ALL -> {
                deleteAllNotes()
            }
            Action.UNDONE -> {
                addNote()
            }
            else -> {

            }
        }
    }

    fun updateNoteFields(selectedNote: ToDoNote?) {
        if (selectedNote != null) {
            id.value = selectedNote.id
            title.value = selectedNote.title
            description.value = selectedNote.description
            priority.value = selectedNote.priority
        } else {
            id.value = 0
            title.value = ""
            description.value = ""
            priority.value = Priority.LOW
        }
    }

    fun updateTitle(newTitle: String) {
        if (newTitle.length < MAX_TITLE_LENGTH) {
            title.value = newTitle
        }
    }

    fun validateFields(): Boolean {
        return title.value.isNotEmpty() && description.value.isNotEmpty()
    }

}