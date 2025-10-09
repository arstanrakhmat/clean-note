package com.example.suminnotes.presentation.screens.creation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.suminnotes.domain.AddNoteUseCase
import com.example.suminnotes.domain.ContentItem
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
                        val newContent = prevState.content.mapIndexed { index, contentItem ->
                            if (index == command.index && contentItem is ContentItem.Text) {
                                contentItem.copy(content = command.content)
                            } else {
                                contentItem
                            }
                        }
                        prevState.copy(
                            content = newContent
                        )
                    } else {
                        prevState
                    }
                }
            }

            is CreateNoteCommand.InputTitle -> {
                _state.update { prevState ->
                    if (prevState is CreateNoteState.Creation) {
                        prevState.copy(
                            title = command.title,
                        )
                    } else {
                        prevState
                    }
                }
            }

            CreateNoteCommand.Save -> {
                viewModelScope.launch {
                    _state.update { prevState ->
                        if (prevState is CreateNoteState.Creation) {
                            val title = prevState.title
                            val content = prevState.content.filter {
                                it !is ContentItem.Text || it.content.isNotBlank()
                            }
                            addNoteUseCase(title, content)
                            CreateNoteState.Finished
                        } else {
                            prevState
                        }
                    }
                }

            }

            is CreateNoteCommand.AddImage -> {
                _state.update { prevState ->
                    if (prevState is CreateNoteState.Creation) {
                        val newItems = prevState.content.toMutableList()
                        val lastItem = newItems.last()
                        if (lastItem is ContentItem.Text && lastItem.content.isBlank()) {
                            newItems.removeAt(newItems.lastIndex)
                        }
                        newItems.add(ContentItem.Image(command.uri.toString()))
                        newItems.add(ContentItem.Text(""))
                        prevState.copy(content = newItems)
                    } else {
                        prevState
                    }
                }
            }

            is CreateNoteCommand.DeleteImage -> {
                _state.update { prevState ->
                    if (prevState is CreateNoteState.Creation) {
                        val newItems = prevState.content.toMutableList()
                        newItems.removeAt(command.index)
                        prevState.copy(content = newItems)
                    } else {
                        prevState
                    }
                }
            }
        }
    }
}

sealed interface CreateNoteCommand {
    data class InputTitle(val title: String) : CreateNoteCommand

    data class InputContent(val content: String, val index: Int) : CreateNoteCommand

    data class AddImage(val uri: Uri) : CreateNoteCommand

    data class DeleteImage(val index: Int) : CreateNoteCommand

    data object Save : CreateNoteCommand

    data object Back : CreateNoteCommand
}

sealed interface CreateNoteState {
    data class Creation(
        val title: String = "",
        val content: List<ContentItem> = listOf(ContentItem.Text("")),
    ) : CreateNoteState {
        val isSavedEnabled: Boolean
            get() {
                return when {
                    title.isBlank() -> false
                    content.isEmpty() -> false
                    else -> {
                        content.any {
                            it !is ContentItem.Text || it.content.isNotBlank()
                        }
                    }
                }
            }
    }

    data object Finished : CreateNoteState
}