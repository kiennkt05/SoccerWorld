package com.example.soccerworld.model.fixture


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Fixture(
    @SerializedName("fixture_id")
    var fixtureId: Int,
    @SerializedName("league_id")
    var leagueId: Int,
    @SerializedName("league")
    var league: League,
    @SerializedName("event_date")
    var eventDate: String,
    @SerializedName("event_timestamp")
    var eventTimestamp: Int,
    @SerializedName("firstHalfStart")
    var firstHalfStart: Int,
    @SerializedName("secondHalfStart")
    var secondHalfStart: Int,
    @SerializedName("round")
    var round: String,
    @SerializedName("status")
    var status: String,
    @SerializedName("statusShort")
    var statusShort: String,
    @SerializedName("elapsed")
    var elapsed: Int,
    @SerializedName("venue")
    var venue: String,
    @SerializedName("referee")
    var referee: String,
    @SerializedName("homeTeam")
    var homeTeam: HomeTeam,
    @SerializedName("awayTeam")
    var awayTeam: AwayTeam,
    @SerializedName("goalsHomeTeam")
    var goalsHomeTeam: Int,
    @SerializedName("goalsAwayTeam")
    var goalsAwayTeam: Int,
    @SerializedName("score")
    var score: Score
) : Parcelable
