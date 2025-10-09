package com.example.suminnotes.data

import com.example.suminnotes.domain.Note

fun Note.toDbModel(): NoteDbModel {
    return NoteDbModel(
        id, title, content, updatedAt, isPinned
    )
}

fun NoteDbModel.toEntity(): Note {
    return Note(
        id, title, content, updatedAt, isPinned
    )
}