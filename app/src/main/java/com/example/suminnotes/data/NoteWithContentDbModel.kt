package com.example.suminnotes.data

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithContentDbModel(
    @Embedded
    val noteDbModel: NoteDbModel,
    @Relation(parentColumn = "id", entityColumn = "noteId")
    val contentDbModel: List<ContentItemDbModel>
)
