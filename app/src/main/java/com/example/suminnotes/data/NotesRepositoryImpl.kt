package com.example.suminnotes.data

import com.example.suminnotes.ImageFileManager
import com.example.suminnotes.domain.ContentItem
import com.example.suminnotes.domain.Note
import com.example.suminnotes.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepositoryImpl @Inject constructor(
    private val notesDao: NotesDao,
    private val imageFileManager: ImageFileManager
) : NotesRepository {

    override suspend fun addNote(
        title: String,
        content: List<ContentItem>,
        isPinned: Boolean,
        updatedAt: Long
    ) {
        val note = Note(0, title, content.processForStorage(), updatedAt, isPinned)
        val noteDbModel = note.toDbModel()
        notesDao.addNote(noteDbModel)
    }

    override suspend fun deleteNote(noteId: Int) {
        val note = notesDao.getNote(noteId).toEntity()
        notesDao.deleteNote(noteId)

        note.content
            .filterIsInstance<ContentItem.Image>()
            .map { it.url }
            .forEach {
                imageFileManager.deleteImage(it)
            }
    }

    override suspend fun editNote(note: Note) {
        val oldNote = notesDao.getNote(note.id).toEntity()

        val oldUrls = oldNote.content.filterIsInstance<ContentItem.Image>().map { it.url }
        val newUrls = note.content.filterIsInstance<ContentItem.Image>().map { it.url }
        val removedUrls = oldUrls - newUrls

        removedUrls.forEach { url ->
            imageFileManager.deleteImage(url)
        }

        val processedContent = note.content.processForStorage()
        val processedNote = note.copy(content = processedContent)

        notesDao.addNote(processedNote.toDbModel())
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

    private suspend fun List<ContentItem>.processForStorage(): List<ContentItem> {
        return map { contentItem ->
            when (contentItem) {
                is ContentItem.Image -> {
                    if (imageFileManager.isInternal(contentItem.url)) {
                        contentItem
                    } else {
                        val internalPath =
                            imageFileManager.copyImageToInternalStorage(contentItem.url)
                        ContentItem.Image(url = internalPath)
                    }
                }

                is ContentItem.Text -> contentItem
            }
        }
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