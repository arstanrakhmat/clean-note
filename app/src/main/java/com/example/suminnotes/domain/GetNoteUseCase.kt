package com.example.suminnotes.domain

class GetNoteUseCase(
    private val repository: NotesRepository
) {

    operator fun invoke(noteId: Int): Note {
        return repository.getNote(noteId)
    }
}