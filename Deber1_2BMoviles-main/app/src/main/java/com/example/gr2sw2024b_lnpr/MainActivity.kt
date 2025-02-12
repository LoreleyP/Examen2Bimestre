package com.example.gr2sw2024b_lnpr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoviesScreen()
        }
    }
}

@Composable
fun MoviesScreen() {
    var movies by remember { mutableStateOf(sampleMovies) }
    var showCreateMovieDialog by remember { mutableStateOf(false) }
    var selectedMovieForEdit by remember { mutableStateOf<Movie?>(null) }
    var selectedMovieForActors by remember { mutableStateOf<Movie?>(null) }

    if (showCreateMovieDialog || selectedMovieForEdit != null) {
        MovieDialog(
            movies = movies,
            movie = selectedMovieForEdit,
            onDismiss = {
                showCreateMovieDialog = false
                selectedMovieForEdit = null
            },
            onSave = { movie: Movie ->
                if (selectedMovieForEdit == null) {
                    movies = movies + movie
                } else {
                    movies = movies.map { if (it.id == movie.id) movie else it }
                }
                showCreateMovieDialog = false
                selectedMovieForEdit = null
            }
        )
    }

    if (selectedMovieForActors != null) {
        ActorsScreen(
            movie = selectedMovieForActors!!,
            onUpdateActors = { updatedActors ->
                movies = movies.map {
                    if (it.id == selectedMovieForActors!!.id) it.copy(actors = updatedActors) else it
                }
            },
            onBack = { selectedMovieForActors = null }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showCreateMovieDialog = true }) {
                    Text("+")
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text(
                    text = "PELÍCULAS",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyColumn {
                    items(movies.size) { index: Int ->
                        val movie = movies[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { selectedMovieForEdit = movie },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = movie.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "Actores: ${movie.actors.joinToString(", ")}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(onClick = { selectedMovieForActors = movie }) {
                                        Text("Ver Actores")
                                    }
                                    Button(onClick = {
                                        movies = movies.filter { it.id != movie.id }
                                    }) {
                                        Text("Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActorsScreen(movie: Movie, onUpdateActors: (List<String>) -> Unit, onBack: () -> Unit) {
    var actors by remember { mutableStateOf(movie.actors) }
    var showEditActorDialog by remember { mutableStateOf<String?>(null) }

    if (showEditActorDialog != null) {
        ActorDialog(
            initialName = showEditActorDialog,
            onDismiss = { showEditActorDialog = null },
            onSave = { name ->
                if (showEditActorDialog.isNullOrEmpty()) {
                    actors = actors + name
                } else {
                    actors = actors.map { if (it == showEditActorDialog) name else it }
                }
                onUpdateActors(actors)
                showEditActorDialog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ACTORES") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("Atrás")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showEditActorDialog = "" }) {
                Text("+")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            LazyColumn {
                items(actors.size) { index ->
                    val actor = actors[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = actor,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(onClick = { showEditActorDialog = actor }) {
                                    Text("Editar")
                                }
                                Button(onClick = {
                                    actors = actors.filter { it != actor }
                                    onUpdateActors(actors)
                                }) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MovieDialog(
    movies: List<Movie>,
    movie: Movie?,
    onDismiss: () -> Unit,
    onSave: (Movie) -> Unit
) {
    var title by remember { mutableStateOf(TextFieldValue(movie?.title ?: "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (movie == null) "Crear Película" else "Editar Película") },
        text = {
            Column {
                Text("Título de la Película")
                BasicTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val newMovie = Movie(
                    id = movie?.id ?: ((movies.maxOfOrNull { it.id } ?: 0) + 1),
                    title = title.text,
                    actors = movie?.actors ?: emptyList()
                )
                onSave(newMovie)
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ActorDialog(initialName: String?, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue(initialName ?: "")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = if (initialName.isNullOrEmpty()) "Crear Actor" else "Editar Actor") },
        text = {
            Column {
                Text("Nombre del Actor")
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name.text) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// Datos iniciales y modelos de ejemplo
data class Movie(val id: Int, val title: String, val actors: List<String>)

val sampleMovies = listOf(
    Movie(1, "Inception", listOf("Leonardo DiCaprio", "Joseph Gordon-Levitt")),
    Movie(2, "The Dark Knight", listOf("Christian Bale", "Heath Ledger"))
)