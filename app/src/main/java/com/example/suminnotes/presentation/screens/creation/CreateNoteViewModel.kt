package com.example.suminnotes.presentation.screens.creation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suminnotes.domain.AddNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNoteViewModel @Inject constructor(
    private val addNoteUseCase: AddNoteUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CreateNoteState>(CreateNoteState.Creation())
    val state = _state.asStateFlow()

    fun processCommand(command: CreateNoteCommand) {
        when (command) {
            CreateNoteCommand.Back -> {
                _state.update { CreateNoteState.Finished }
            }

            is CreateNoteCommand.InputContent -> {
                _state.update { prevState ->
                    if (prevState is CreateNoteState.Creation) {
                        prevState.copy(
                            content = command.content,
                            isSavedEnabled = prevState.title.isNotBlank() && prevState.content.isNotBlank()
                        )
                    } else {
                        CreateNoteState.Creation(content = command.content)
                    }
                }
            }

            is CreateNoteCommand.InputTitle -> {
                _state.update { prevState ->
                    if (prevState is CreateNoteState.Creation) {
                        prevState.copy(
                            title = command.title,
                            isSavedEnabled = prevState.title.isNotBlank() && prevState.content.isNotBlank()
                        )
                    } else {
                        CreateNoteState.Creation(title = command.title)
                    }
                }
            }

            CreateNoteCommand.Save -> {
                viewModelScope.launch {
                    _state.update { prevState ->
                        if (prevState is CreateNoteState.Creation) {
                            val title = prevState.title
                            val content = prevState.content
                            addNoteUseCase(title, content)
                            CreateNoteState.Finished
                        } else {
                            prevState
                        }
                    }
                }

            }
        }
    }
}

sealed interface CreateNoteCommand {
    data class InputTitle(val title: String) : CreateNoteCommand

    data class InputContent(val content: String) : CreateNoteCommand

    data object Save : CreateNoteCommand

    data object Back : CreateNoteCommand
}

sealed interface CreateNoteState {
    data class Creation(
        val title: String = "",
        val content: String = "",
        val isSavedEnabled: Boolean = false
    ) : CreateNoteState

    data object Finished : CreateNoteState
}