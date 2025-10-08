package com.example.suminnotes.domain

class SwitchPinStatusUseCase(
    private val repository: NotesRepository
) {

    suspend operator fun invoke(noteId: Int) {
        repository.switchPinStatus(noteId)
    }
}