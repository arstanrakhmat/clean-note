package com.example.suminnotes.presentation.screens.editing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suminnotes.data.TestNotesRepositoryImpl
import com.example.suminnotes.domain.DeleteNoteUseCase
import com.example.suminnotes.domain.EditNoteUseCase
import com.example.suminnotes.domain.GetNoteUseCase
import com.example.suminnotes.domain.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditNoteViewModel(private val noteId: Int) : ViewModel() {

    private val repository = TestNotesRepositoryImpl

    private val editNoteUseCase = EditNoteUseCase(repository)
    private val getNoteUseCase = GetNoteUseCase(repository)
    private val deleteNoteUseCase = DeleteNoteUseCase(repository)

    private val _state = MutableStateFlow<EditNoteState>(EditNoteState.Initial)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update {
                val note = getNoteUseCase(noteId)
                EditNoteState.Editing(note)
            }
        }
    }

    fun processCommand(command: EditNoteCommand) {
        when (command) {
            EditNoteCommand.Back -> {
                _state.update { EditNoteState.Finished }
            }

            is EditNoteCommand.InputContent -> {
                _state.update { prevState ->
                    if (prevState is EditNoteState.Editing) {
                        val newNote = prevState.note.copy(content = command.content)
                        prevState.copy(note = newNote)
                    } else {
                        prevState
                    }
                }
            }

            is EditNoteCommand.InputTitle -> {
                _state.update { prevState ->
                    if (prevState is EditNoteState.Editing) {
                        val newNote = prevState.note.copy(title = command.title)
                        prevState.copy(note = newNote)
                    } else {
                        prevState
                    }
                }
            }

            EditNoteCommand.Save -> {
                viewModelScope.launch {
                    _state.update { prevState ->
                        if (prevState is EditNoteState.Editing) {
                            val note = prevState.note
                            editNoteUseCase(note)
                            EditNoteState.Finished
                        } else {
                            prevState
                        }
                    }
                }
            }

            EditNoteCommand.Delete -> {
                viewModelScope.launch {
                    _state.update { prevState ->
                        if (prevState is EditNoteState.Editing) {
                            val note = prevState.note
                            deleteNoteUseCase(note.id)
                            EditNoteState.Finished
                        } else {
                            prevState
                        }
                    }
                }
            }
        }
    }
}

sealed interface EditNoteCommand {
    data class InputTitle(val title: String) : EditNoteCommand

    data class InputContent(val content: String) : EditNoteCommand

    data object Save : EditNoteCommand

    data object Back : EditNoteCommand

    data object Delete : EditNoteCommand
}

sealed interface EditNoteState {
    data object Initial : EditNoteState

    data class Editing(
        val note: Note
    ) : EditNoteState {

        val isSavedEnabled: Boolean
            get() = note.title.isNotBlank() && note.content.isNotBlank()
    }

    data object Finished : EditNoteState
}