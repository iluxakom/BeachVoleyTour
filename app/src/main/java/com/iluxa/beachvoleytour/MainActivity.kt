package com.iluxa.beachvoleytour

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iluxa.beachvoleytour.model.Tournament
import com.iluxa.beachvoleytour.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences("MyApp", Context.MODE_PRIVATE)
    }

    lateinit var gameViewModel: GameViewModel


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val schema =
            applicationContext.assets.open("tournament.schemes.json").readBytes().decodeToString()

        gameViewModel = GameViewModel(sharedPreferences, schema)
        gameViewModel.loadData()

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(gameViewModel)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        gameViewModel.saveData()
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun MyApp(gameViewModel: GameViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (gameViewModel.isTourStarted()) "games" else "players"
    ) {
        composable("players") {
            PlayerScreen(gameViewModel, onNext = {
                navController.navigate("games")
            })
        }
        composable("games") {
            GameScreen(gameViewModel, onNext = {
                gameViewModel.saveResults()
                navController.navigate("results")
            }, onBack = {
                gameViewModel.saveResults()
                navController.navigate("players")
            })
        }
        composable("results") {
            val result = gameViewModel.getResults().map {
                Triple(
                    gameViewModel.players[it.playerIndex - 1],
                    it.wins,
                    it.score
                )
            }
            ResultsScreen(
                gameViewModel,
                result
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

                val showAddDialog = remember { mutableStateOf(false) }
                val showRestDialog = remember { mutableStateOf(false) }

                @Composable
                fun ResetDialog(state: MutableState<Boolean>, confirmAction: () -> Unit) {
                    AlertDialog(
                        onDismissRequest = { state.value = false },
                        title = { Text(text = "Game results will be lost") },
                        text = { Text(text = "some games already played. Reset the results?") },
                        confirmButton = {
                            Button(
                                onClick = confirmAction,
                            ) {
                                Text(text = "Proceed")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = { state.value = false },
                            ) {
                                Text(text = "Cancel")
                            }
                        }
                    )
                }

                if (showAddDialog.value) {
                    ResetDialog(showAddDialog, confirmAction = {
                        players.add("")
                        viewModel.resetResults()
                        showAddDialog.value = false
                    })
                }
                if (showRestDialog.value) {
                    ResetDialog(showRestDialog, confirmAction = {
                        players.clear()
                        players.addAll(listOf("", "", "", ""))
                        viewModel.resetResults()
                        showRestDialog.value = false
                    })
                }

                Button(
                    onClick = {
                        if (viewModel.isTourStarted())
                            showAddDialog.value = true
                        else {
                            players.add("")
                            viewModel.resetResults()
                        }
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
                    Text("Go games")
                }

                Button(
                    onClick = {
                        showRestDialog.value = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Reset")
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun GameScreen(gameViewModel: GameViewModel, onNext: () -> Unit, onBack: () -> Unit) {
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
                                val (team1Players, team2Players) =
                                    gameViewModel.getGames()[index].run { first to second }
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
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    onClick = onBack,
                    modifier = Modifier.padding(horizontal = 15.dp)
                ) {
                    Text(text = "< To players")
                }

                Button(
                    onClick = {
                        gameViewModel.printGameResults()
                        onNext()
                    },
                    modifier = Modifier.padding(horizontal = 15.dp)
                ) {
                    Text(text = "To results >")
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(vm: GameViewModel, result: List<Triple<String, Int, Int>>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Results" + if (vm.isTourFinished.value) "" else " (not all games finished)",
                        color = if (!vm.isTourFinished.value) Color.Red else Color.Green
                    )
                }
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
                result.forEach { playerResult ->
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
                                playerResult.first,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playerResult.second.toString(),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playerResult.third.toString(),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            @Composable
            fun ShareButton() {
                val shareIntent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT,
                            result.joinToString(separator = "\n") { "${it.first}:  wins: ${it.second} score: ${it.third}" })
                    }
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { }

                Button(onClick = {
                    val chooser = Intent.createChooser(shareIntent, "Share with...")
                    launcher.launch(chooser)
                }) {
                    Column {
                        Text("Share")
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                ShareButton()
            }
        }
    )
}

class GameViewModel(private val sharedPreferences: SharedPreferences, private val schema: String) :
    ViewModel() {
    var tournament: Tournament? = null
    val players = mutableStateListOf("", "", "", "")
    val gameResults = mutableStateListOf<GameResult>()


    val isTourFinished = mutableStateOf(false)

    fun resetResults() {
        gameResults.clear()
        isTourFinished.value = false
        tournament = Tournament(players.size, schema)
        gameResults.addAll(getEmptyGameList(tournament!!.getGames().size))
    }

    fun saveResults() = gameResults.forEachIndexed { index, gameResult ->
        tournament?.setGameResult(
            index + 1,
            gameResult.team1Score ?: 0,
            gameResult.team2Score ?: 0
        ) ?: error("no tournament data")
        observeGameResults()
    }

    fun getResults() = tournament?.getResults() ?: error("no tournament data")

    fun getGames() = tournament?.getGames() ?: error("no tournament data")

    private fun observeGameResults() {
        isTourFinished.value =
            gameResults.all { it.team1Score != null && it.team2Score != null }
    }

    fun isTourStarted() = gameResults.any { it.team1Score != null || it.team2Score != null }

    fun setGameResult(gameIndex: Int, result: GameResult) {
        gameResults[gameIndex] = result
        observeGameResults()
    }

    fun printGameResults() = println(gameResults.map { it.toString() })

    companion object {
        private const val PLAYERS_KEY = "PLAYERS_KEY"
        private const val GAME_RESULTS_KEY = "GAME_RESULTS_KEY"
    }

    fun loadData() {
        players.clear()
        players.addAll(
            Gson().fromJson(
                sharedPreferences.getString(PLAYERS_KEY, null),
                object : TypeToken<List<String>>() {}.type
            ) ?: listOf("", "", "", "")
        )
        tournament = Tournament(players.size, schema)

        gameResults.clear()
        gameResults.addAll(
            Gson().fromJson(
                sharedPreferences.getString(GAME_RESULTS_KEY, null),
                object : TypeToken<List<GameResult>>() {}.type
            ) ?: getEmptyGameList(tournament!!.getGames().size)
        )
    }

    private fun getEmptyGameList(size: Int): SnapshotStateList<GameResult> {
        val emptyGameList = mutableStateListOf<GameResult>()
        repeat(size) {
            emptyGameList.add(GameResult(null, null))
        }
        return emptyGameList
    }

    fun saveData() {
        sharedPreferences.edit().apply {
            putString(PLAYERS_KEY, Gson().toJson(players))
            putString(GAME_RESULTS_KEY, Gson().toJson(gameResults))
            apply()
        }
    }

}

data class GameResult(val team1Score: Int?, val team2Score: Int?) {
    override fun toString(): String {
        return "$team1Score - $team2Score"
    }
}
