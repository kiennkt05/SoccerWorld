package com.example.soccerworld.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.soccerworld.data.local.entity.FixturesCacheEntity
import com.example.soccerworld.data.local.entity.FavoriteMatchEntity
import com.example.soccerworld.data.local.entity.MatchDetailCacheEntity
import com.example.soccerworld.data.local.entity.StandingsCacheEntity
import com.example.soccerworld.data.local.entity.TeamPlayersCacheEntity
import com.example.soccerworld.data.local.entity.TeamsCacheEntity
import com.example.soccerworld.model.topscorer.TopScorerEntity

@Database(
    entities = [
        TopScorerEntity::class,
        FavoriteMatchEntity::class,
        StandingsCacheEntity::class,
        FixturesCacheEntity::class,
        TeamsCacheEntity::class,
        TeamPlayersCacheEntity::class,
        MatchDetailCacheEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class FootballDatabase : RoomDatabase() {

    abstract fun footballDao(): FootballDao

    companion object {
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS topscorer_table")
                db.execSQL("DROP TABLE IF EXISTS id_bridge")
                db.execSQL("DROP TABLE IF EXISTS team_media")
                db.execSQL("DROP TABLE IF EXISTS player_media")
                db.execSQL("DROP TABLE IF EXISTS espn_match_enrichment")
                db.execSQL("DROP TABLE IF EXISTS espn_team_profile")
                db.execSQL("DROP TABLE IF EXISTS espn_player_profile")
                db.execSQL("DROP TABLE IF EXISTS favorite_matches")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS topscorer_table (
                        playerId TEXT NOT NULL,
                        playerName TEXT NOT NULL,
                        teamName TEXT NOT NULL,
                        goals INTEGER NOT NULL,
                        imageUrl TEXT,
                        PRIMARY KEY(playerId)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS favorite_matches (
                        matchId TEXT NOT NULL,
                        leagueCode TEXT,
                        utcDate TEXT,
                        homeTeamId TEXT,
                        homeTeamName TEXT,
                        homeTeamCrest TEXT,
                        awayTeamId TEXT,
                        awayTeamName TEXT,
                        awayTeamCrest TEXT,
                        status TEXT,
                        savedAt INTEGER NOT NULL,
                        PRIMARY KEY(matchId)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS standings_cache (
                        leagueId TEXT NOT NULL,
                        payloadJson TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(leagueId)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS fixtures_cache (
                        queryKey TEXT NOT NULL,
                        payloadJson TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(queryKey)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS teams_cache (
                        leagueId TEXT NOT NULL,
                        payloadJson TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(leagueId)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS team_players_cache (
                        teamId TEXT NOT NULL,
                        payloadJson TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(teamId)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS match_detail_cache (
                        fixtureId TEXT NOT NULL,
                        payloadJson TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(fixtureId)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS topscorer_table_new (
                        leagueId TEXT NOT NULL,
                        playerId TEXT NOT NULL,
                        playerName TEXT NOT NULL,
                        teamName TEXT NOT NULL,
                        goals INTEGER NOT NULL,
                        imageUrl TEXT,
                        PRIMARY KEY(leagueId, playerId)
                    )
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE IF EXISTS topscorer_table")
                db.execSQL("ALTER TABLE topscorer_table_new RENAME TO topscorer_table")
            }
        }

        @Volatile
        private var instance: FootballDatabase? = null
        private val lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            instance ?: makeDatabase(context).also {
                instance = it
            }
        }

        private fun makeDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            FootballDatabase::class.java,
            "footballdatabase"
        ).addMigrations(MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)
            .build()
    }
}