package com.iluxa.beachvoleytour

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iluxa.beachvoleytour.model.Tournament
import com.iluxa.beachvoleytour.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
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

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MyApp() {
    val navController = rememberNavController()

    val gameViewModel: GameViewModel = viewModel()


    val schema =
        LocalContext.current.assets.open("tournament.schemes.json").readBytes().decodeToString()

    NavHost(navController = navController, startDestination = "players") {
        composable("players") {
            PlayerScreen(gameViewModel, onNext = {
                val tour =
                    if (gameViewModel.tournament.value == null || gameViewModel.tournament.value!!.players.size != gameViewModel.players.size) {
                        val t = Tournament(gameViewModel.players.toList(), schema)
                        gameViewModel.tournament.value = t
                        t
                    } else gameViewModel.tournament.value!!
                if (tour.getGames().size != gameViewModel.gameResults.size) {
                    gameViewModel.resetResults()
                    repeat(tour.getGames().size) {
                        gameViewModel.gameResults.add(GameResult(null, null))
                    }
                }
                navController.navigate("games")
            })
        }
        composable("games") {
            GameScreen(gameViewModel, onNext = {
                gameViewModel.gameResults.forEachIndexed { index, gameResult ->
                    gameViewModel.tournament.value?.setGameResult(
                        index + 1,
                        gameResult.team1Score!!,
                        gameResult.team2Score!!
                    ) ?: error("no tournament data")
                }
                navController.navigate("results")
            })
        }
        composable("results") {
            ResultsScreen(
                gameViewModel.tournament.value?.getResults() ?: error("no tournament data")
            )
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

                val showDialog = remember { mutableStateOf(false) }

                if (showDialog.value) {
                    AlertDialog(
                        onDismissRequest = { showDialog.value = false },
                        title = { Text(text = "Game results will be lost") },
                        text = { Text(text = "some games already played. Reset the results?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    players.add("")
                                    showDialog.value = false
                                },
                            ) {
                                Text(text = "Add player anyway")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { showDialog.value = false },
                            ) {
                                Text(text = "Cancel")
                            }
                        }
                    )
                }

                Button(
                    onClick = {
                        if (viewModel.isTourStarted())
                            showDialog.value = true
                        else
                            players.add("")
                    },
                    enabled = players.size < 7,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        if (players.size < 7) "Add Player"
                        else "Max7 players is here"
                    )
                }

                Button(
                    onClick = onNext,
                    enabled = players.all { it.isNotBlank() },
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
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun GameScreen(gameViewModel: GameViewModel, onNext: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Games") }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                gameViewModel.run {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 60.dp, bottom = 60.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        itemsIndexed(gameResults) { index, result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val team1Players =
                                    gameViewModel.tournament.value!!.getGames()[index].first
                                val team2Players =
                                    gameViewModel.tournament.value!!.getGames()[index].second
                                Box(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${index + 1}.",
                                    )
                                }
                                Box(modifier = Modifier.weight(4f)) {
                                    TextField(
                                        value = result.team1Score.run { this?.toString() ?: "" },
                                        onValueChange = {
                                            val newValue = it.toIntOrNull() ?: 0
                                            val anotherTeamRes = gameResults[index].team2Score
                                            setGameResult(
                                                index,
                                                GameResult(newValue, anotherTeamRes)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "${players[team1Players.player1 - 1]}/${players[team1Players.player2 - 1]}",
                                                maxLines = 1, overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            keyboardType = KeyboardType.Number
                                        ),
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    )
                                }
                                Text(
                                    text = "vs",
                                )
                                Box(modifier = Modifier.weight(4f)) {
                                    TextField(
                                        value = result.team2Score.run { this?.toString() ?: "" },
                                        onValueChange = {
                                            val newValue = it.toIntOrNull() ?: 0
                                            val anotherTeamRes = gameResults[index].team1Score
                                            setGameResult(
                                                 index,
                                                GameResult(anotherTeamRes, newValue)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "${players[team2Players.player1 - 1]}/${players[team2Players.player2 - 1]}",
                                                maxLines = 1, overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            keyboardType = KeyboardType.Number
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Button(
                    onClick = {
                        gameViewModel.printGameResults()
                        onNext()
                    },
                    enabled = gameViewModel.isTourFinished.value,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (!gameViewModel.isTourFinished.value) "Set down all the games to go to results" else "To results")
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(results: List<Tournament.PlayerResult>) {
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
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            "Player",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wins",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Scores",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                results.forEach { playerResult ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                playerResult.name,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playerResult.wins.toString(),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playerResult.score.toString(),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    )
}

class GameViewModel : ViewModel() {
    val tournament = mutableStateOf<Tournament?>(null)
    val players = mutableStateListOf("", "", "", "", "")
    val gameResults = mutableStateListOf<GameResult>()


    val isTourFinished = mutableStateOf(false)

    fun resetResults() {
        gameResults.removeAll { true }
        isTourFinished.value = false
    }

    private fun observeGameResults() {
        isTourFinished.value = gameResults.all { it.team1Score != null && it.team2Score != null }
    }

    fun isTourStarted() = gameResults.any { it.team1Score != null || it.team2Score != null }

    fun setGameResult(gameIndex: Int, result: GameResult) {
        gameResults[gameIndex] = result
        observeGameResults()
    }

    fun printGameResults() = println(gameResults.map { it.toString() })

}

data class GameResult(val team1Score: Int?, val team2Score: Int?) {
    override fun toString(): String {
        return "$team1Score - $team2Score"
    }
}
