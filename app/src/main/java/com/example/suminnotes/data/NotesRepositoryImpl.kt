package com.example.suminnotes.data

import com.example.suminnotes.domain.Note
import com.example.suminnotes.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(private val notesDao: NotesDao) : NotesRepository {

    override suspend fun addNote(
        title: String,
        content: String,
        isPinned: Boolean,
        updatedAt: Long
    ) {
        val noteDbModel = NoteDbModel(0, title, content, updatedAt, isPinned)
        notesDao.addNote(noteDbModel)
    }

    override suspend fun deleteNote(noteId: Int) {
        notesDao.deleteNote(noteId)
    }

    override suspend fun editNote(note: Note) {
        notesDao.addNote(note.toDbModel())
    }

    override fun getAllNotes(): Flow<List<Note>> {
        return notesDao.getAllNotes().map { it.toEntities() }
    }

    override suspend fun getNote(noteId: Int): Note {
        return notesDao.getNote(noteId).toEntity()
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return notesDao.searchNotes(query).map { it.toEntities() }
    }

    override suspend fun switchPinStatus(noteId: Int) {
        notesDao.switchPinnedStatus(noteId)
    }

//    companion object {
//        private val LOCK = Any()
//        private var instance: NotesRepositoryImpl? = null
//
//        fun getInstance(context: Context): NotesRepositoryImpl {
//            instance?.let { return it }
//
//            synchronized(LOCK) {
//                instance?.let { return it }
//
//                return NotesRepositoryImpl(context).also {
//                    instance = it
//                }
//            }
//        }
//    }
}