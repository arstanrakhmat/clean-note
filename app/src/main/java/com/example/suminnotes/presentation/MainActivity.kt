package com.example.suminnotes.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.suminnotes.presentation.screens.editing.EditNoteScreen
import com.example.suminnotes.presentation.ui.theme.SuminNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuminNotesTheme {
                EditNoteScreen(noteId = 5, onFinished = {
                    Log.d("CreateNoteScreen", "Finished")
                })
//                CreateNoteScreen(
//                    onFinished = {
//                        Log.d("CreateNoteScreen", "Finished")
//                    }
//                )
//                NotesScreen(onNoteClick = {
//                    Log.d("MainActivity", "onNoteClick: $it")
//                },
//                    onAddNoteClick = {
//                        Log.d("MainActivity", "onAddNoteClicked")
//                    }
//                )
            }
        }
    }
}
