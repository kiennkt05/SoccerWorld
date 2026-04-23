package com.example.soccerworld.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.soccerworld.data.local.entity.FavoriteMatchEntity
import com.example.soccerworld.model.topscorer.TopScorerEntity

@Database(
    entities = [
        TopScorerEntity::class,
        FavoriteMatchEntity::class
    ],
    version = 7,
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
        ).addMigrations(MIGRATION_6_7)
            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)
            .build()
    }
}