package com.example.suminnotes.presentation.navigation

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.suminnotes.presentation.screens.creation.CreateNoteScreen
import com.example.suminnotes.presentation.screens.editing.EditNoteScreen
import com.example.suminnotes.presentation.screens.notes.NotesScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Notes.route
    ) {
        composable(Screen.Notes.route) {
            NotesScreen(
                onNoteClick = {
                    navController.navigate(Screen.EditNote.createRoute(it.id))
                },
                onAddNoteClick = {
                    navController.navigate(Screen.CreateNote.route)
                }
            )
        }

        composable(Screen.CreateNote.route) {
            CreateNoteScreen(
                onFinished = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EditNote.route) {
            val noteId = Screen.EditNote.getNoteId(arguments = it.arguments)

            EditNoteScreen(
                noteId = noteId,
                onFinished = {
                    navController.popBackStack()
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object Notes : Screen(route = "notes")

    data object CreateNote : Screen(route = "create_note")

    data object EditNote : Screen(route = "edit_note/{note_id}") { //Bundle("note_id" - "5")

        fun createRoute(noteId: Int): String { //edit_note/5
            return "edit_note/$noteId"
        }

        fun getNoteId(arguments: Bundle?): Int {
            return arguments?.getString("note_id")?.toInt() ?: 0
        }
    }
}