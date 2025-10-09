package com.example.suminnotes.domain

import javax.inject.Inject

class AddNoteUseCase @Inject constructor(
    private val repository: NotesRepository
) {

    suspend operator fun invoke(
        title: String,
        content: List<ContentItem>
    ) {
        repository.addNote(
            title = title,
            content = content,
            isPinned = false,
            updatedAt = System.currentTimeMillis()
        )
    }
}