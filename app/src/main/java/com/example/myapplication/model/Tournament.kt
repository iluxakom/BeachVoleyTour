package com.example.myapplication.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


class Tournament(private val players: List<String>, schema: String) {

    private val gamesToPlay: MutableList<Game> = emptyList<Game>().toMutableList()

    init {
        if (players.size < 4 || players.size > 7) error("The number of players must be between 4 and 7")

        Json.decodeFromString<List<TournamentData>>(schema)
            .firstOrNull { it.playersNumber == players.size }
            ?.let {
                val repeat = it.repeatRoundNumber ?: 1
                repeat(repeat) { _ ->
                    it.games.forEach { game ->
                        val team1 = game.jsonObject["team1"]?.jsonArray ?: error("no team1 element")
                        val team2 = game.jsonObject["team2"]?.jsonArray ?: error("no team2 element")
                        assert(team1.size == 2) { "the size of team must be 2" }
                        assert(team2.size == 2) { "the size of team must be 2" }

                        gamesToPlay.add(
                            Game(
                                Team(team1[0].jsonPrimitive.int, team1[1].jsonPrimitive.int),null,
                                Team(team2[0].jsonPrimitive.int, team2[1].jsonPrimitive.int),null,
                            )
                        )
                    }
                }
            } ?: error("can't find a tournament for ${players.size} players")

    }

    fun getResults(): List<PlayerResult> {
        val results = mutableListOf<PlayerResult>()
        players.forEachIndexed { index, _ ->
            val playerIndex = index + 1
            var wins = 0
            var fullScore = 0
            gamesToPlay.forEach { game ->
                game.run {
                    val score = getPlayerScore(playerIndex)
                    fullScore += score
                   if (score > 0) wins++
                }
            }
            results.add(PlayerResult(players[playerIndex - 1], wins, fullScore))
        }
        return results.sortedDescending()
    }

    fun getGames() = gamesToPlay.map { it.team1 to it.team2 }

    fun setGameResult(gameNumber: Int, team1Result: Int, team2Result: Int) {
        gamesToPlay[gameNumber - 1].run {
            this.team1Result = team1Result
            this.team2Result = team2Result
        }
    }

    data class PlayerResult(val name: String, val wins: Int, val score: Int): Comparable<PlayerResult> {
        override fun toString(): String {
            return "$name $wins $score"
        }
        override fun compareTo(other: PlayerResult): Int {
            return compareValuesBy(this, other, { it.wins }, { it.score })
        }
    }
    @Serializable
    data class TournamentData(val playersNumber: Int, val repeatRoundNumber: Int? = null, val games: JsonArray)

    data class Team(val player1: Int, val player2: Int) {
        fun getPlayers() = Pair(player1, player2)
    }

    data class Game(val team1: Team, var team1Result: Int? = null, val team2: Team, var team2Result: Int? = null) {
        private fun getPlayers() = listOf(team1.player1, team1.player2, team2.player1, team2.player2)
        fun getPlayerScore(player: Int): Int {
            if (!getPlayers().contains(player) || team1Result == null || team2Result == null) return 0
            val winnerTeam = if (team2Result!! >  team1Result!!) team2 else team1
            val scoreDiff = kotlin.math.abs(team1Result!! - team2Result!!)
            return if (winnerTeam.getPlayers().toList().contains(player)) scoreDiff else -scoreDiff
        }
    }
}
