package com.example.soccerworld.data.remote.flashlive

import com.google.gson.annotations.SerializedName

data class StandingsResponse(
    @SerializedName("DATA") val data: List<StandingBlock>? = null
)

data class TopScorersResponse(
    @SerializedName("DATA") val data: List<TopScorerBlock>? = null
)

data class StandingBlock(
    @SerializedName("ROWS") val rows: List<StandingRow>? = null
)

data class TopScorerBlock(
    @SerializedName("ROWS") val rows: List<TopScorerRow>? = null
)

data class StandingRow(
    @SerializedName("RANKING") val ranking: Int? = null,
    @SerializedName("TEAM_ID") val teamId: String? = null,
    @SerializedName("TEAM_NAME") val teamName: String? = null,
    @SerializedName("TEAM_IMAGE_PATH") val teamImagePath: String? = null,
    @SerializedName("MATCHES_PLAYED") val matchesPlayed: Int? = null,
    @SerializedName("WINS") val wins: Int? = null,
    @SerializedName("DRAWS") val draws: Int? = null,
    @SerializedName("LOSSES") val losses: Int? = null,
    @SerializedName("POINTS") val points: Int? = null,
    @SerializedName("GOALS") val goals: String? = null
)

data class TopScorerRow(
    @SerializedName("TS_RANK") val rank: Int? = null,
    @SerializedName("TS_PLAYER_ID") val playerId: String? = null,
    @SerializedName("TS_PLAYER_NAME") val playerName: String? = null,
    @SerializedName("TS_PLAYER_GOALS") val goals: Int? = null,
    @SerializedName("TS_PLAYER_ASISTS") val assists: Int? = null,
    @SerializedName("TS_PLAYER_TEAM") val teamId: String? = null,
    @SerializedName("TEAM_NAME") val teamName: String? = null,
    @SerializedName("TS_IMAGE_PATH") val imagePath: String? = null
)

data class FixturesResponse(
    @SerializedName("DATA") val data: List<EventsByTournament>? = null
)

data class EventsListResponse(
    @SerializedName("DATA") val data: List<EventsByTournament>? = null
)

data class EventsByTournament(
    @SerializedName("TOURNAMENT_STAGE_ID") val tournamentStageId: String? = null,
    @SerializedName("EVENTS") val events: List<FlashLiveEvent>? = null
)

data class FlashLiveEvent(
    @SerializedName("EVENT_ID") val eventId: String? = null,
    @SerializedName("START_TIME") val startTime: Long? = null,
    @SerializedName("HOME_ID") val homeId: String? = null,
    @SerializedName("HOME_NAME") val homeName: String? = null,
    @SerializedName("HOME_IMAGE_PATH") val homeImagePath: String? = null,
    @SerializedName("HOME_IMAGES") val homeImages: List<String>? = null,
    @SerializedName("HOME_SCORE_CURRENT") val homeScore: String? = null,
    @SerializedName("AWAY_ID") val awayId: String? = null,
    @SerializedName("AWAY_NAME") val awayName: String? = null,
    @SerializedName("AWAY_IMAGE_PATH") val awayImagePath: String? = null,
    @SerializedName("AWAY_IMAGES") val awayImages: List<String>? = null,
    @SerializedName("AWAY_SCORE_CURRENT") val awayScore: String? = null,
    @SerializedName("STAGE_TYPE") val stageType: String? = null,
    @SerializedName("ROUND") val round: String? = null
)

data class EventDataResponse(
    @SerializedName("DATA") val data: FlashLiveEvent? = null
)

data class EventSummaryResponse(
    @SerializedName("DATA") val data: List<SummaryStage>? = null,
    @SerializedName("INFO") val info: SummaryInfo? = null
)

data class SummaryInfo(
    @SerializedName("REFEREE") val referee: String? = null,
    @SerializedName("VENUE") val venue: String? = null,
    @SerializedName("ATTENDANCE") val attendance: String? = null
)

data class SummaryStage(
    @SerializedName("ITEMS") val items: List<SummaryIncident>? = null
)

data class SummaryIncident(
    @SerializedName("INCIDENT_TIME") val time: String? = null,
    @SerializedName("INCIDENT_PARTICIPANTS") val participants: List<SummaryParticipant>? = null
)

data class SummaryParticipant(
    @SerializedName("INCIDENT_TYPE") val type: String? = null,
    @SerializedName("PARTICIPANT_NAME") val participantName: String? = null,
    @SerializedName("INCIDENT_NAME") val incidentName: String? = null,
    @SerializedName("HOME_SCORE") val homeScore: String? = null,
    @SerializedName("AWAY_SCORE") val awayScore: String? = null
)

data class EventStatsResponse(
    @SerializedName("DATA") val data: List<EventStatsStage>? = null
)

data class EventStatsStage(
    @SerializedName("STAGE_NAME") val stageName: String? = null,
    @SerializedName("GROUPS") val groups: List<EventStatsGroup>? = null
)

data class EventStatsGroup(
    @SerializedName("GROUP_LABEL") val groupLabel: String? = null,
    @SerializedName("ITEMS") val items: List<EventStatsItem>? = null
)

data class EventStatsItem(
    @SerializedName("INCIDENT_NAME") val incidentName: String? = null,
    @SerializedName("VALUE_HOME") val valueHome: String? = null,
    @SerializedName("VALUE_AWAY") val valueAway: String? = null
)

data class LineupsResponse(
    @SerializedName("DATA") val data: List<LineupGroup>? = null
)

data class LineupGroup(
    @SerializedName("FORMATIONS") val formations: List<LineupFormation>? = null
)

data class LineupFormation(
    @SerializedName("FORMATION_DISPOSTION") val formation: String? = null,
    @SerializedName("PLAYER_GROUP_TYPE") val playerGroupType: Int? = null,
    @SerializedName("MEMBERS") val members: List<LineupMember>? = null
)

data class LineupMember(
    @SerializedName("PLAYER_ID") val playerId: String? = null,
    @SerializedName("PLAYER_FULL_NAME") val fullName: String? = null,
    @SerializedName("PLAYER_NUMBER") val number: Int? = null,
    @SerializedName("PLAYER_POSITION_ID") val positionId: Int? = null
)

data class H2HResponse(
    @SerializedName("DATA") val data: List<H2HData>? = null
)

data class H2HData(
    @SerializedName("GROUPS") val groups: List<H2HGroup>? = null
)

data class H2HGroup(
    @SerializedName("ITEMS") val items: List<H2HItem>? = null
)

data class H2HItem(
    @SerializedName("EVENT_ID") val eventId: String? = null,
    @SerializedName("HOME_PARTICIPANT") val homeParticipant: String? = null,
    @SerializedName("AWAY_PARTICIPANT") val awayParticipant: String? = null,
    @SerializedName("CURRENT_RESULT") val currentResult: String? = null,
    @SerializedName("START_TIME") val startTime: Long? = null
)

data class TeamDataResponse(
    @SerializedName("DATA") val data: TeamData? = null
)

data class TeamData(
    @SerializedName("ID") val id: String? = null,
    @SerializedName("NAME") val name: String? = null,
    @SerializedName("IMAGE_PATH") val imagePath: String? = null
)

data class SquadResponse(
    @SerializedName("DATA") val data: List<SquadGroup>? = null
)

data class SquadGroup(
    @SerializedName("ITEMS") val items: List<SquadPlayer>? = null
)

data class SquadPlayer(
    @SerializedName("PLAYER_ID") val playerId: String? = null,
    @SerializedName("PLAYER_NAME") val playerName: String? = null,
    @SerializedName("PLAYER_TYPE_ID") val playerTypeId: String? = null,
    @SerializedName("PLAYER_IMAGE_PATH") val playerImagePath: String? = null
)

data class PlayerDataResponse(
    @SerializedName("DATA") val data: PlayerData? = null
)

data class PlayerData(
    @SerializedName("ID") val id: String? = null,
    @SerializedName("NAME") val name: String? = null,
    @SerializedName("IMAGE_PATH") val imagePath: String? = null
)

data class SearchResult(
    @SerializedName("ID") val id: String? = null,
    @SerializedName("NAME") val name: String? = null,
    @SerializedName("TYPE") val type: String? = null,
    @SerializedName("IMAGE") val image: String? = null
)
