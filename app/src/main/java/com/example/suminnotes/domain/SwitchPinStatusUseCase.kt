package com.example.suminnotes.domain

import javax.inject.Inject

class SwitchPinStatusUseCase @Inject constructor(
    private val repository: NotesRepository
) {

    suspend operator fun invoke(noteId: Int) {
        repository.switchPinStatus(noteId)
    }
}