package com.example.suminnotes.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.suminnotes.presentation.screens.notes.NotesScreen
import com.example.suminnotes.presentation.ui.theme.SuminNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuminNotesTheme {
                NotesScreen(onNoteClick = {
                    Log.d("MainActivity", "onNoteClick: $it")
                },
                    onAddNoteClick = {
                        Log.d("MainActivity", "onAddNoteClicked")
                    })
            }
        }
    }
}
