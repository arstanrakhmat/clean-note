@file:Suppress("OPT_IN_USAGE")

package com.example.suminnotes.presentation.screens.notes

import androidx.lifecycle.ViewModel
import com.example.suminnotes.data.TestNotesRepositoryImpl
import com.example.suminnotes.domain.AddNoteUseCase
import com.example.suminnotes.domain.DeleteNoteUseCase
import com.example.suminnotes.domain.EditNoteUseCase
import com.example.suminnotes.domain.GetAllNotesUseCase
import com.example.suminnotes.domain.GetNoteUseCase
import com.example.suminnotes.domain.Note
import com.example.suminnotes.domain.SearchNotesUseCase
import com.example.suminnotes.domain.SwitchPinStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class NotesViewModel : ViewModel() {

    private val repository: TestNotesRepositoryImpl = TestNotesRepositoryImpl

    private val addNoteUseCase = AddNoteUseCase(repository)
    private val editNoteUseCase = EditNoteUseCase(repository)
    private val deleteNoteUseCase = DeleteNoteUseCase(repository)
    private val getAllNotesUseCase = GetAllNotesUseCase(repository)
    private val getNoteUseCase = GetNoteUseCase(repository)
    private val searchNotesUseCase = SearchNotesUseCase(repository)
    private val switchPinStatusUseCase = SwitchPinStatusUseCase(repository)

    private val query = MutableStateFlow(" ")

    private val _state = MutableStateFlow(NotesScreenState())
    val state = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        addSomeNotes()

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
            .launchIn(scope)
    }

    //TODO: do not forget to remove it
    private fun addSomeNotes() {
        repeat(50_000) {
            addNoteUseCase(title = "Title #$it", content = "Content $it")
        }
    }

    fun processCommand(command: NotesCommands) {
        when (command) {
            is NotesCommands.DeleteNote -> {
                deleteNoteUseCase(command.noteId)
            }

            is NotesCommands.EditNote -> {
                val note = getNoteUseCase(command.note.id)
                val title = note.title
                editNoteUseCase(note.copy(title = "$title edited"))
            }

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

sealed interface NotesCommands {
    data class InputSearchQuery(val query: String) : NotesCommands

    data class SwitchPinnedStatus(val noteId: Int) : NotesCommands

    //Temp
    data class DeleteNote(val noteId: Int) : NotesCommands
    data class EditNote(val note: Note) : NotesCommands
}

data class NotesScreenState(
    val query: String = "",
    val pinnedNotes: List<Note> = listOf(),
    val otherNotes: List<Note> = listOf()
)