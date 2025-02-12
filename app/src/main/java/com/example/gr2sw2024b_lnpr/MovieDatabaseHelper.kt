package com.example.gr2sw2024b_lnpr

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp


    class MovieDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE $TABLE_MOVIES (" +
                        "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "$COLUMN_TITLE TEXT," +
                        "$COLUMN_ACTORS TEXT)"
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_MOVIES")
            onCreate(db)
        }

        fun insertMovie(movie: Movie) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_TITLE, movie.title)
                put(COLUMN_ACTORS, movie.actors.joinToString(", "))
            }
            db.insert(TABLE_MOVIES, null, values)
            db.close()
        }

        fun updateMovie(movie: Movie) {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COLUMN_TITLE, movie.title)
                put(COLUMN_ACTORS, movie.actors.joinToString(", "))
            }
            db.update(TABLE_MOVIES, values, "$COLUMN_ID = ?", arrayOf(movie.id.toString()))
            db.close()
        }

        fun deleteMovie(id: Int) {
            val db = writableDatabase
            db.delete(TABLE_MOVIES, "$COLUMN_ID = ?", arrayOf(id.toString()))
            db.close()
        }

        fun getAllMovies(): List<Movie> {
            val db = readableDatabase
            val cursor = db.query(TABLE_MOVIES, null, null, null, null, null, null)
            val movies = mutableListOf<Movie>()

            with(cursor) {
                while (moveToNext()) {
                    val id = getInt(getColumnIndexOrThrow(COLUMN_ID))
                    val title = getString(getColumnIndexOrThrow(COLUMN_TITLE))
                    val actors = getString(getColumnIndexOrThrow(COLUMN_ACTORS)).split(", ")
                    movies.add(Movie(id, title, actors))
                }
            }
            cursor.close()
            db.close()
            return movies
        }

        companion object {
            const val DATABASE_VERSION = 1
            const val DATABASE_NAME = "MoviesDB.db"
            const val TABLE_MOVIES = "movies"
            const val COLUMN_ID = "id"
            const val COLUMN_TITLE = "title"
            const val COLUMN_ACTORS = "actors"
        }
    }


    @Composable
    fun MoviesScreen(dbHelper: MovieDatabaseHelper) {
        var movies by remember { mutableStateOf(dbHelper.getAllMovies()) }
        var showCreateMovieDialog by remember { mutableStateOf(false) }
        var selectedMovieForEdit by remember { mutableStateOf<Movie?>(null) }
        var selectedMovieForActors by remember { mutableStateOf<Movie?>(null) }

        if (showCreateMovieDialog || selectedMovieForEdit != null) {
            MovieDialog(
                movie = selectedMovieForEdit,
                onDismiss = {
                    showCreateMovieDialog = false
                    selectedMovieForEdit = null
                },
                onSave = { movie: Movie ->
                    if (selectedMovieForEdit == null) {
                        dbHelper.insertMovie(movie)
                    } else {
                        dbHelper.updateMovie(movie)
                    }
                    movies = dbHelper.getAllMovies()
                    showCreateMovieDialog = false
                    selectedMovieForEdit = null
                }
            )
        }

        if (selectedMovieForActors != null) {
            ActorsScreen(
                movie = selectedMovieForActors!!,
                onUpdateActors = { updatedActors ->
                    val updatedMovie = selectedMovieForActors!!.copy(actors = updatedActors)
                    dbHelper.updateMovie(updatedMovie)
                    movies = dbHelper.getAllMovies()
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
                                            dbHelper.deleteMovie(movie.id)
                                            movies = dbHelper.getAllMovies()
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
                        id = movie?.id ?: 0,
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

    // Data model
    data class Movie(val id: Int, val title: String, val actors: List<String>)

