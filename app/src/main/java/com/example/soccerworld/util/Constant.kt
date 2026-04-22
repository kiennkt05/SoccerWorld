package com.example.soccerworld.util

object Constant {
    const val BASE_URL = "https://api.football-data.org"
    const val GET_LEAGUE_TABLE = "v4/competitions/{league_id}/standings"
    const val GET_TOP_SCORERS = "v4/competitions/{league_id}/scorers"
    const val GET_ALL_TEAMS_OF_LEAGUE = "v4/competitions/{league_id}/teams"
    const val GET_ALL_PLAYERS_OF_TEAM = "v4/teams/{id}"

    // GET ALL TRANSFER OF TEAM INAVAILABLE IN THIS API
    // const val GET_ALL_TRANSFERS_OF_TEAM = "transfers/team/{team_id}"
    const val GET_ALL_FIXTURE_OF_LEAGUE = "v4/competitions/{league_id}/matches"
    const val GET_ALL_H2H_ITEMS = "v4/matches/{id}/head2head" //"fixtures/h2h/{home_team_id}/{away_team_id}"
    const val GET_FIXTURE_STATISTICS = "v4/matches/{fixture_id}"//"statistics/fixture/{fixture_id}"

    const val TEAM_ID = "team_id"
    const val FIXTURE_TEAM_IDS = "h2h_team_ids"
}