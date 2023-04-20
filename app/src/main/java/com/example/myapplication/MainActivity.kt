package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "players") {
        composable("players") {
            PlayerScreen(onNext = {
                navController.navigate("games")
            })
        }
        composable("games") {
            GameScreen(onNext = {
                navController.navigate("results")
            })
        }
        composable("results") {
            ResultsScreen()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(onNext: () -> Unit) {
    // State to hold the list of player names
    val players = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Players") }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Text field to enter player names
                var playerName by remember { mutableStateOf("") }
                TextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Player Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                // Button to add the player name to the list
                Button(
                    onClick = {
                        // Add the player name to the list of players
                        players.add(playerName)
                        // Clear the text field
                        playerName = ""
                    },
                    enabled = playerName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Add Player")
                }

                // Button to go to the next screen
                Button(
                    onClick = onNext,
                    enabled = players.size in 4..7,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Next")
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(onNext: () -> Unit) {
    // TODO: Implement game screen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Games") }
            )
        },
        content = { padding ->
            TextField( //TODO
                value = "playerName",
                onValueChange = { },
                label = { Text("Player Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        },
        bottomBar = {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("To Results")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen() {
    // TODO: Implement results screen
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Results") }
            )
        },
        content = { padding ->
            TextField( //TODO
                value = "playerName",
                onValueChange = { },
                label = { Text("Player Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
        }
    )
}
