// MapActivity.kt
package com.example.gr2sw2024b_lnpr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)
        val movieTitle = intent.getStringExtra("movieTitle") ?: "Ubicación"

        setContent {
            MaterialTheme {
                MapView(latitude = latitude, longitude = longitude, movieTitle = movieTitle)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapView(latitude: Double, longitude: Double, movieTitle: String) {
    val location = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, 15f) // Zoom level 15
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mapa de $movieTitle") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                Marker(
                    state = MarkerState(position = location),
                    title = movieTitle,
                    snippet = "Lat: $latitude, Long: $longitude"
                )
            }
        }
    }
}