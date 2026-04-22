package com.example.soccerworld.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class CustomSharedPreferences {

    companion object{
        private val COUNTRY_ID = "country_id"
        private val PREFERENCES_TIME = "preferences_time"
        private const val TOP_SCORERS_TIME = "top_scorers_time"
        private const val STANDINGS_TIME = "standings_time"
        private const val FIXTURES_TIME = "fixtures_time"
        private const val TEAMS_TIME = "teams_time"
        private val RB_ID = "rb_id"
        private val LEAGUE_ID = "league_id"
        private var sharedPreferences: SharedPreferences? = null

        @Volatile
        private var instance: CustomSharedPreferences? = null
        private val lock = Any()

        operator fun invoke(context: Context) : CustomSharedPreferences = instance ?: synchronized(lock){
            instance ?: makeCustomSharedPreferences(context).also {
                instance = it
            }
        }

        private fun makeCustomSharedPreferences(context: Context): CustomSharedPreferences {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return CustomSharedPreferences()
        }

    }
    fun saveCountryId(countryId: Int){
        sharedPreferences?.edit(commit = true){
            putInt(COUNTRY_ID,countryId)
        }
    }

    fun getCountryId()= sharedPreferences?.getInt(COUNTRY_ID,524)

    fun saveRbCountry(rb : Int){
        sharedPreferences?.edit(commit = true){
            putInt(RB_ID,rb)
        }
    }

    fun getRbCountry() = sharedPreferences?.getInt(RB_ID, 0)

    fun saveTime(time: Long) {
        sharedPreferences?.edit(commit = true){
            putLong(PREFERENCES_TIME,time)
        }
    }

    fun getTime() = sharedPreferences?.getLong(PREFERENCES_TIME,0)

    fun saveTopScorersTime(time: Long) {
        sharedPreferences?.edit(commit = true) {
            putLong(TOP_SCORERS_TIME, time)
        }
    }

    fun getTopScorersTime() = sharedPreferences?.getLong(TOP_SCORERS_TIME, 0)

    fun saveStandingsTime(time: Long) {
        sharedPreferences?.edit(commit = true) {
            putLong(STANDINGS_TIME, time)
        }
    }

    fun getStandingsTime() = sharedPreferences?.getLong(STANDINGS_TIME, 0)

    fun saveFixturesTime(time: Long) {
        sharedPreferences?.edit(commit = true) {
            putLong(FIXTURES_TIME, time)
        }
    }

    fun getFixturesTime() = sharedPreferences?.getLong(FIXTURES_TIME, 0)

    fun saveTeamsTime(time: Long) {
        sharedPreferences?.edit(commit = true) {
            putLong(TEAMS_TIME, time)
        }
    }

    fun getTeamsTime() = sharedPreferences?.getLong(TEAMS_TIME, 0)

    fun saveLeagueId(leagueId: String) {
        sharedPreferences?.edit(commit = true) {
            putString(LEAGUE_ID, leagueId)
        }
    }

    fun getLeagueId(): String? = sharedPreferences?.getString(LEAGUE_ID, null)

    fun hasSelectedLeague(): Boolean = sharedPreferences?.contains(LEAGUE_ID) ?: false
}