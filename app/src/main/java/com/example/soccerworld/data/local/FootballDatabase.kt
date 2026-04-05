package com.example.soccerworld.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.soccerworld.model.topscorer.TopScorerEntity

// 1. Đổi entities thành class mới, tăng version lên 3
@Database(entities = [TopScorerEntity::class], version = 3, exportSchema = false)
abstract class FootballDatabase : RoomDatabase() {

    abstract fun footballDao(): FootballDao

    companion object {
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
        ).fallbackToDestructiveMigration().build()
    }
}