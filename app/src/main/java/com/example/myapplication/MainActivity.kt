package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
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

    val gameViewModel: GameViewModel = viewModel()


    NavHost(navController = navController, startDestination = "players") {
        composable("players") {
            PlayerScreen(gameViewModel, onNext = {
                navController.navigate("games")
            })
        }
        composable("games") {
            GameScreen(gameViewModel, onNext = {
                navController.navigate("results")
            })
        }
        composable("results") {
            ResultsScreen(gameViewModel)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(viewModel: GameViewModel, onNext: () -> Unit) {
    val players = viewModel.players

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
                    .padding(horizontal = 16.dp)
                    .padding(top = 60.dp),
                verticalArrangement = Arrangement.Top
            ) {

                players.forEachIndexed { index, player ->
                    TextField(
                        value = player,
                        onValueChange = { players[index] = it },
                        label = { Text("Enter player ${index + 1}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Button to add the player name to the list
                Button(
                    onClick = {
                        players.add("")
                    },
                    enabled = players.size < 7,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        if (players.size < 7) "Add Player"
                        else "Max 7 players is here"
                    )
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
fun GameScreen(gameViewModel: GameViewModel, onNext: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Games") }
            )
        },
        content = { padding ->
            gameViewModel.run {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(top = 60.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    gameResults.forEachIndexed { index, result ->
                        val team1Score = remember { mutableStateOf(result.team1Score) }
                        val team2Score = remember { mutableStateOf(result.team2Score) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val team1Players = gameTeams[index].first
                            val team2Players = gameTeams[index].second
                            Text(
                                text = "${index + 1}.",
                            )
                            TextField(
                                value = team1Score.value.run { this?.toString() ?: "" },
                                onValueChange = {
                                    val newValue = it.toIntOrNull() ?: 0
                                    team1Score.value = newValue
                                    gameViewModel.setTeam1GameResult(
                                        index,
                                        newValue
                                    )
                                },
                                label = { Text(text = "${players[team1Players.first]}/${players[team1Players.second]}") },

                                modifier = Modifier
                                    .weight(1f),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                )
                            )
                            Text(
                                text = "vs",
                            )
                            TextField(
                                value = team2Score.value.run { this?.toString() ?: "" },
                                onValueChange = {
                                    val newValue = it.toIntOrNull() ?: 0
                                    team2Score.value = newValue
                                    gameViewModel.setTeam2GameResult(
                                        index,
                                        newValue
                                    )
                                },
                                label = { Text(text = "${players[team2Players.first]}/${players[team2Players.second]}") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                )
                            )

                        }
                    }
                }
            }
        },
        bottomBar = {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!gameViewModel.isTourFinished.value) Text(text = "Set down all the games to go to results")
                Button(
                    onClick = {
                        gameViewModel.printGameResults()
                        onNext()
                    },
                    enabled = gameViewModel.isTourFinished.value,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("To results")
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(gameViewModel: GameViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Results") }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .padding(top = 60.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Player")
                    Text(text = "-")
                    Text(text = "Scores")
                }
                gameViewModel.players.forEach {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(it)
                        Text(text = "-")
                        Text(text = "100")
                    }
                }
            }
        }
    )
}

class GameViewModel : ViewModel() {
    val players = mutableStateListOf("1", "2", "3", "4", "5")
    val gameTeams = listOf(
        Pair(0, 1) to Pair(2, 3),
        Pair(0, 2) to Pair(3, 4),
        Pair(0, 3) to Pair(2, 4),
        Pair(0, 4) to Pair(1, 4),
    )
    val gameResults = mutableListOf(
        GameResult(null, null),
        GameResult(null, null),
        GameResult(null, null),
        GameResult(null, null),
    )

    val isTourFinished = mutableStateOf(false)

    private fun observeGameResults() {
        isTourFinished.value = gameResults.all { it.team1Score != null && it.team2Score != null }
    }

    fun setTeam1GameResult(gameIndex: Int, score: Int) {
        gameResults[gameIndex].team1Score = score
        observeGameResults()
    }

    fun setTeam2GameResult(gameIndex: Int, score: Int) {
        gameResults[gameIndex].team2Score = score
        observeGameResults()
    }

    fun printGameResults() = println(gameResults.map { it.toString() })

}

data class GameResult(var team1Score: Int?, var team2Score: Int?) {
    override fun toString(): String {
        return "$team1Score - $team2Score"
    }
}