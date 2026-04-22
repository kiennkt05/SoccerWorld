package com.example.soccerworld.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.soccerworld.data.local.entity.EspnMatchEnrichmentEntity
import com.example.soccerworld.data.local.entity.EspnPlayerProfileEntity
import com.example.soccerworld.data.local.entity.EspnTeamProfileEntity
import com.example.soccerworld.data.local.entity.FavoriteMatchEntity
import com.example.soccerworld.data.local.entity.IdBridgeEntity
import com.example.soccerworld.data.local.entity.PlayerMediaEntity
import com.example.soccerworld.data.local.entity.TeamMediaEntity
import com.example.soccerworld.model.topscorer.TopScorerEntity

// 1. Đổi entities thành class mới, tăng version lên 3
@Database(
    entities = [
        TopScorerEntity::class,
        IdBridgeEntity::class,
        TeamMediaEntity::class,
        PlayerMediaEntity::class,
        EspnMatchEnrichmentEntity::class,
        EspnTeamProfileEntity::class,
        EspnPlayerProfileEntity::class,
        FavoriteMatchEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class FootballDatabase : RoomDatabase() {

    abstract fun footballDao(): FootballDao

    companion object {
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS id_bridge (
                        footballDataId INTEGER NOT NULL,
                        entityType TEXT NOT NULL,
                        espnId TEXT,
                        sportsDbId TEXT,
                        canonicalName TEXT NOT NULL,
                        resolvedAt INTEGER NOT NULL,
                        PRIMARY KEY(footballDataId)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS team_media (
                        footballDataTeamId INTEGER NOT NULL,
                        teamName TEXT NOT NULL,
                        badgeUrl TEXT,
                        bannerUrl TEXT,
                        lastUpdated INTEGER NOT NULL,
                        PRIMARY KEY(footballDataTeamId)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS player_media (
                        footballDataPlayerId INTEGER NOT NULL,
                        playerName TEXT NOT NULL,
                        thumbUrl TEXT,
                        cutoutUrl TEXT,
                        lastUpdated INTEGER NOT NULL,
                        PRIMARY KEY(footballDataPlayerId)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS espn_match_enrichment (
                        footballDataMatchId INTEGER NOT NULL,
                        espnEventId TEXT NOT NULL,
                        payloadJson TEXT NOT NULL,
                        status TEXT,
                        lastUpdated INTEGER NOT NULL,
                        PRIMARY KEY(footballDataMatchId)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS espn_team_profile (
                        espnTeamId TEXT NOT NULL,
                        footballDataTeamId INTEGER,
                        teamName TEXT,
                        logoUrl TEXT,
                        payloadJson TEXT,
                        lastUpdated INTEGER NOT NULL,
                        PRIMARY KEY(espnTeamId)
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS espn_player_profile (
                        espnPlayerId TEXT NOT NULL,
                        footballDataPlayerId INTEGER,
                        playerName TEXT,
                        imageUrl TEXT,
                        payloadJson TEXT,
                        lastUpdated INTEGER NOT NULL,
                        PRIMARY KEY(espnPlayerId)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS favorite_matches (
                        matchId INTEGER NOT NULL,
                        leagueCode TEXT,
                        utcDate TEXT,
                        homeTeamId INTEGER,
                        homeTeamName TEXT,
                        homeTeamCrest TEXT,
                        awayTeamId INTEGER,
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
        ).addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build()
    }
}