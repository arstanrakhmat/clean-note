@file:Suppress("OPT_IN_USAGE")

package com.example.suminnotes.presentation.screens.notes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suminnotes.data.NotesRepositoryImpl
import com.example.suminnotes.data.TestNotesRepositoryImpl
import com.example.suminnotes.domain.GetAllNotesUseCase
import com.example.suminnotes.domain.Note
import com.example.suminnotes.domain.SearchNotesUseCase
import com.example.suminnotes.domain.SwitchPinStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesViewModel(context: Context) : ViewModel() {

    private val repository = NotesRepositoryImpl.getInstance(context)

    private val getAllNotesUseCase = GetAllNotesUseCase(repository)
    private val searchNotesUseCase = SearchNotesUseCase(repository)
    private val switchPinStatusUseCase = SwitchPinStatusUseCase(repository)

    private val query = MutableStateFlow(" ")

    private val _state = MutableStateFlow(NotesScreenState())
    val state = _state.asStateFlow()

    init {
        query
            .onEach { input ->
                _state.update { it.copy(query = input) }
            }
            .flatMapLatest { input ->
                if (input.isBlank()) {
                    getAllNotesUseCase()
                } else {
                    searchNotesUseCase(input)
                }
            }
            .onEach { notes ->
                val pinnedNotes = notes.filter { it.isPinned }
                val otherNotes = notes.filter { !it.isPinned }
                _state.update {
                    it.copy(pinnedNotes = pinnedNotes, otherNotes = otherNotes)
                }
            }
            .launchIn(viewModelScope)
    }

    fun processCommand(command: NotesCommands) {
        viewModelScope.launch {
            when (command) {
                is NotesCommands.InputSearchQuery -> {
                    query.update {
                        command.query.trim()
                    }
                }

                is NotesCommands.SwitchPinnedStatus -> {
                    switchPinStatusUseCase(command.noteId)
                }
            }
        }
    }
}

sealed interface NotesCommands {
    data class InputSearchQuery(val query: String) : NotesCommands

    data class SwitchPinnedStatus(val noteId: Int) : NotesCommands
}

data class NotesScreenState(
    val query: String = "",
    val pinnedNotes: List<Note> = listOf(),
    val otherNotes: List<Note> = listOf()
)