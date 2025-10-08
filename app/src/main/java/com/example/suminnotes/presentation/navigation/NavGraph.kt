package com.example.suminnotes.presentation.navigation

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
                    navController.navigate(Screen.EditNote.route)
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
            EditNoteScreen(
                noteId = 5,
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

    data object EditNote : Screen(route = "edit_note")
}