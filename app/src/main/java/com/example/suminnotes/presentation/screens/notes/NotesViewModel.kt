@file:Suppress("OPT_IN_USAGE")

package com.example.suminnotes.presentation.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suminnotes.domain.GetAllNotesUseCase
import com.example.suminnotes.domain.Note
import com.example.suminnotes.domain.SearchNotesUseCase
import com.example.suminnotes.domain.SwitchPinStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val searchNotesUseCase: SearchNotesUseCase,
    private val switchPinStatusUseCase: SwitchPinStatusUseCase
) : ViewModel() {

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