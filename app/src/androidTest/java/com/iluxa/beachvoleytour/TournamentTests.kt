package com.iluxa.beachvoleytour

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iluxa.beachvoleytour.model.Tournament
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TournamentTests {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val schema =
        context.assets.open("tournament.schemes.json").readBytes().decodeToString()
    private val players = listOf("aaa", "bbb", "ccc", "ddd", "eee", "fff")

    private fun getResultsWithPlayerNames(res: List<Tournament.PlayerResult>) =
        "[" + res.joinToString { "${players[it.playerIndex - 1]} ${it.wins} ${it.score}" } + "]"


    @Test
    fun check_first_round_game_data_for_4_players_tour() {
        Tournament(players.subList(0, 4).size, schema).run {
            assertEquals(
                listOf(
                    Pair(Tournament.Team(1, 2), Tournament.Team(3, 4)),
                    Pair(Tournament.Team(1, 3), Tournament.Team(2, 4)),
                    Pair(Tournament.Team(1, 4), Tournament.Team(2, 3)),
                ),
                getGames().subList(0, 3)
            )
        }
    }

    @Test
    fun check_complete_4_players_tour() {
        Tournament(players.subList(0, 4).size, schema).run {
            setGameResult(1, 11, 2)
            assertEquals(
                "[aaa 1 9, bbb 1 9, ccc 0 -9, ddd 0 -9]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(2, 5, 11)
            assertEquals(
                "[bbb 2 15, aaa 1 3, ddd 1 -3, ccc 0 -15]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(3, 4, 11)
            assertEquals(
                "[bbb 3 22, aaa 1 -4, ccc 1 -8, ddd 1 -10]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(4, 11, 6)
            assertEquals(
                "[bbb 4 27, aaa 2 1, ccc 1 -13, ddd 1 -15]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(5, 11, 8)
            assertEquals(
                "[bbb 4 24, aaa 3 4, ccc 2 -10, ddd 1 -18]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(6, 11, 10)
            assertEquals(
                "[bbb 4 23, aaa 4 5, ccc 2 -11, ddd 2 -17]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(7, 9, 11)
            assertEquals(
                "[bbb 4 21, aaa 4 3, ccc 3 -9, ddd 3 -15]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(8, 10, 11)
            assertEquals(
                "[bbb 5 22, aaa 4 2, ddd 4 -14, ccc 3 -10]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(9, 11, 9)
            assertEquals(
                "[bbb 5 20, aaa 5 4, ddd 5 -12, ccc 3 -12]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(10, 11, 6)
            assertEquals(
                "[bbb 6 25, aaa 6 9, ddd 5 -17, ccc 3 -17]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(11, 8, 11)
            assertEquals(
                "[bbb 7 28, aaa 6 6, ddd 6 -14, ccc 3 -20]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(12, 11, 10)

            assertEquals(
                "[bbb 7 27, aaa 7 7, ddd 7 -13, ccc 3 -21]",
                getResultsWithPlayerNames(getResults())
            )

        }
    }

    @Test
    fun check_complete_5_players_tour() {
        Tournament(players.subList(0, 5).size, schema).run {
            setGameResult(1, 11, 7)
            assertEquals(
                "[aaa 1 4, bbb 1 4, eee 0 0, ccc 0 -4, ddd 0 -4]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(2, 5, 11)
            assertEquals(
                "[bbb 2 10, ccc 1 2, aaa 1 -2, ddd 0 -4, eee 0 -6]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(3, 11, 9)
            assertEquals(
                "[bbb 3 12, aaa 2 0, ccc 1 2, ddd 0 -6, eee 0 -8]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(4, 3, 11)
            assertEquals(
                "[bbb 3 12, ccc 2 10, aaa 2 -8, eee 1 0, ddd 0 -14]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(5, 11, 5)
            assertEquals(
                "[bbb 4 18, eee 2 6, ccc 2 4, aaa 2 -8, ddd 0 -20]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(6, 11, 10)
            assertEquals(
                "[bbb 4 17, aaa 3 -7, eee 2 6, ccc 2 3, ddd 1 -19]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(7, 3, 11)
            setGameResult(8, 6, 11)
            assertEquals(
                "[bbb 6 30, eee 3 9, aaa 3 -20, ccc 2 -5, ddd 2 -14]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(9, 10, 11)
            setGameResult(10, 3, 11)
            setGameResult(11, 7, 11)
            setGameResult(12, 9, 11)
            setGameResult(13, 4, 11)
            setGameResult(14, 4, 11)
            setGameResult(15, 9, 11)
            assertEquals(
                "[bbb 8 29, eee 8 22, ddd 6 -3, ccc 5 -7, aaa 3 -41]",
                getResultsWithPlayerNames(getResults())
            )
        }
    }

    @Test
    fun check_complete_6_players_tour() {
        Tournament(players.size, schema).run {
            setGameResult(1, 11, 6)
            setGameResult(2, 8, 11)
            setGameResult(3, 11, 8)
            setGameResult(4, 7, 11)
            setGameResult(5, 11, 7)
            setGameResult(6, 6, 11)
            setGameResult(7, 11, 4)
            setGameResult(8, 11, 8)
            setGameResult(9, 7, 11)
            assertEquals(
                "[fff 6 22, bbb 4 13, aaa 4 10, eee 2 -8, ddd 1 -18, ccc 1 -19]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(10, 11, 10)
            setGameResult(11, 9, 11)
            setGameResult(12, 11, 5)
            setGameResult(13, 8, 11)
            setGameResult(14, 5, 11)
            assertEquals(
                "[fff 8 29, aaa 6 11, ddd 5 -6, bbb 4 3, eee 4 -6, ccc 1 -31]",
                getResultsWithPlayerNames(getResults())
            )
            setGameResult(15, 4, 11)
            setGameResult(16, 11, 8)
            setGameResult(17, 6, 11)
            setGameResult(18, 11, 6)
            assertEquals(
                "[fff 11 46, aaa 8 12, eee 6 -3, ddd 6 -4, bbb 4 -12, ccc 1 -39]",
                getResultsWithPlayerNames(getResults())
            )
        }
    }
}
