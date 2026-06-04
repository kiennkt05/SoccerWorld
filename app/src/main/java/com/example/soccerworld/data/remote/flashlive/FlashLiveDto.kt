package com.example.soccerworld.data.remote.flashlive

import com.google.gson.annotations.SerializedName

data class StandingsResponse(
    @SerializedName("DATA") val data: List<StandingBlock>? = null
)

data class TopScorersResponse(
    @SerializedName("DATA") val data: List<TopScorerBlock>? = null,
    @SerializedName("ROWS") val rows: List<TopScorerRow>? = null
)

data class StandingBlock(
    @SerializedName("GROUP") val group: String? = null,
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
    @SerializedName("TS_PLAYER_NAME_PA") val playerName: String? = null,
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
    @SerializedName("NAME") val name: String? = null,
    @SerializedName("TOURNAMENT_STAGE_ID") val tournamentStageId: String? = null,
    @SerializedName("TOURNAMENT_IMAGE") val tournamentImage: String? = null,
    @SerializedName("EVENTS") val events: List<FlashLiveEvent>? = null
)

data class FlashLiveEvent(
    @SerializedName("EVENT_ID") val eventId: String? = null,
    @SerializedName("START_TIME") val startTime: Long? = null,
    @SerializedName("HOME_ID") val homeId: String? = null,
    @SerializedName("HOME_NAME") val homeName: String? = null,
    @SerializedName("SHORTNAME_HOME") val shortNameHome: String? = null,
    @SerializedName("HOME_IMAGE_PATH") val homeImagePath: String? = null,
    @SerializedName("HOME_IMAGES") val homeImages: List<String>? = null,
    @SerializedName("HOME_SCORE_CURRENT") val homeScore: String? = null,
    @SerializedName("HOME_SCORE_FULL") val homeScoreFull: String? = null,
    @SerializedName("AWAY_ID") val awayId: String? = null,
    @SerializedName("AWAY_NAME") val awayName: String? = null,
    @SerializedName("SHORTNAME_AWAY") val shortNameAway: String? = null,
    @SerializedName("AWAY_IMAGE_PATH") val awayImagePath: String? = null,
    @SerializedName("AWAY_IMAGES") val awayImages: List<String>? = null,
    @SerializedName("AWAY_SCORE_CURRENT") val awayScore: String? = null,
    @SerializedName("AWAY_SCORE_FULL") val awayScoreFull: String? = null,
    @SerializedName("STAGE_TYPE") val stageType: String? = null,
    @SerializedName("ROUND") val round: String? = null
)

data class EventDataResponse(
    @SerializedName("DATA") val data: EventDataWrapper? = null
)

data class EventDataWrapper(
    @SerializedName("EVENT") val event: FlashLiveEvent? = null
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
    @SerializedName("STAGE_NAME") val stageName: String? = null,
    @SerializedName("RESULT_HOME") val resultHome: String? = null,
    @SerializedName("RESULT_AWAY") val resultAway: String? = null,
    @SerializedName("ITEMS") val items: List<SummaryIncident>? = null
)

data class SummaryIncident(
    @SerializedName("INCIDENT_TIME") val time: String? = null,
    @SerializedName("INCIDENT_TEAM") val team: Int? = null,
    @SerializedName("ADDED_TIME") val addedTime: String? = null,
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

data class EventHighlightResponse(
    @SerializedName("DATA") val data: List<HighlightItem>? = null,
)

data class HighlightItem(
    @SerializedName("PROPERTY_LINK") val link: String? = null,
    @SerializedName("PROPERTY_TITLE") val title: String? = null,
    @SerializedName("PROPERTY_SOURCE") val source: String? = null,
    @SerializedName("IMAGES") val images: List<HighlightImage>? = null
)

data class HighlightImage(
    @SerializedName("PROPERTY_IMAGE_URL") val url: String? = null
)

data class LineupsResponse(
    @SerializedName("DATA") val data: List<LineupGroup>? = null
)

data class LineupGroup(
    @SerializedName("FORMATION_NAME") val formationName: String? = null,
    @SerializedName("FORMATIONS") val formations: List<LineupFormation>? = null,
    @SerializedName("PLAYER_GROUP_TYPE") val playerGroupType: Int? = null
)

data class LineupFormation(
    @SerializedName("FORMATION_LINE") val formationLine: Int? = null,
    @SerializedName("FORMATION_DISPOSTION") val formation: String? = null,
    @SerializedName("MEMBERS") val members: List<LineupMember>? = null
)

data class LineupMember(
    @SerializedName("PLAYER_ID") val playerId: String? = null,
    @SerializedName("PLAYER_FULL_NAME") val fullName: String? = null,
    @SerializedName("SHORT_NAME") val shortName: String? = null,
    @SerializedName("PLAYER_NUMBER") val number: Int? = null,
    @SerializedName("PLAYER_POSITION_ID") val positionId: Int? = null,
    @SerializedName("PLAYER_POSITION") val position: Int? = null,
    @SerializedName("LPI") val imageId: String? = null,
    @SerializedName("LPR") val rating: String? = null,
    @SerializedName("INCIDENTS") val incidents: List<Int>? = null
)

data class H2HResponse(
    @SerializedName("DATA") val data: List<H2HData>? = null
)

data class H2HData(
    @SerializedName("GROUPS") val groups: List<H2HGroup>? = null
)

data class H2HGroup(
    @SerializedName("GROUP_LABEL") val groupLabel: String? = null,
    @SerializedName("ITEMS") val items: List<H2HItem>? = null
)

data class H2HItem(
    @SerializedName("EVENT_ID") val eventId: String? = null,
    @SerializedName("HOME_PARTICIPANT") val homeParticipant: String? = null,
    @SerializedName("AWAY_PARTICIPANT") val awayParticipant: String? = null,
    @SerializedName("CURRENT_RESULT") val currentResult: String? = null,
    @SerializedName("START_TIME") val startTime: Long? = null,
    @SerializedName("EVENT_NAME") val eventName: String? = null
)

data class TeamDataResponse(
    @SerializedName("DATA") val data: TeamData? = null
)

data class TeamData(
    @SerializedName("ID") val id: String? = null,
    @SerializedName("NAME") val name: String? = null,
    @SerializedName("IMAGE_PATH") val imagePath: String? = null,
    @SerializedName("TVN") val stadium: String? = null,
    @SerializedName("COUNTRY_NAME") val countryName: String? = null,
    @SerializedName("ACTUAL_TOURNAMENT_STAGE_ID") val actualTournamentStageId: String? = null,
    @SerializedName("ACTUAL_TOURNAMENT_SEASON_ID") val actualTournamentSeasonId: String? = null
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
    @SerializedName("PLAYER_IMAGE_PATH") val playerImagePath: String? = null,
    @SerializedName("PLAYER_JERSEY_NUMBER") val jerseyNumber: Int? = null,
    @SerializedName("PLAYER_FLAG_ID") val flagId: Int? = null
)

data class PlayerDataResponse(
    @SerializedName("DATA") val data: PlayerData? = null
)

data class PlayerData(
    @SerializedName("ID") val id: String? = null,
    @SerializedName("LAYOUT") val layout: String? = null,
    @SerializedName("SHORT_NAME") val shortName: String? = null,
    @SerializedName("GENDER_ID") val genderId: Int? = null,
    @SerializedName("COUNTRY_ID") val countryId: Int? = null,
    @SerializedName("COUNTRY_NAME") val countryName: String? = null,
    @SerializedName("IMAGE_PATH") val imagePath: String? = null,
    @SerializedName("NAME") val name: String? = null,
    @SerializedName("BIRTHDAY_TIME") val birthdayTime: String? = null,
    @SerializedName("TYPE_ID") val typeId: Int? = null,
    @SerializedName("TYPE_NAME") val typeName: String? = null,
    @SerializedName("PARENT_NAME") val parentName: String? = null,
    @SerializedName("PMV") val pmv: String? = null,
    @SerializedName("PCE") val pce: String? = null,
    @SerializedName("TEAM_ID") val teamId: String? = null,
    @SerializedName("TEAM_IMAGE") val teamImage: String? = null,
    @SerializedName("TEAM_NAME") val teamName: String? = null
)

data class PlayerCareerResponse(
    @SerializedName("DATA") val data: List<CareerTabBlock>? = null
)

data class CareerTabBlock(
    @SerializedName("TAB_ID") val tabId: Int? = null,
    @SerializedName("TAB_LABEL") val tabLabel: String? = null,
    @SerializedName("TAB_TYPE") val tabType: Int? = null,
    @SerializedName("ROWS") val rows: List<CareerRow>? = null
)

data class CareerRow(
    @SerializedName("SEASON_LABEL") val seasonLabel: String? = null,
    @SerializedName("TEAM_ID") val teamId: String? = null,
    @SerializedName("TEAM_NAME") val teamName: String? = null,
    @SerializedName("TEAM_IMAGE_URL") val teamImageUrl: String? = null,
    @SerializedName("TOURNAMENT_NAME") val tournamentName: String? = null,
    @SerializedName("TOURNAMENT_FLAG_ID") val tournamentFlagId: Int? = null,
    @SerializedName("TOURNAMENT_STAGE_ID") val tournamentStageId: String? = null,
    @SerializedName("PAZ") val paz: String? = null,
    @SerializedName("STATS") val stats: Map<String, String>? = null
)

sealed interface SearchItemDto {
    val id: String
    val type: String
    val sportId: Int?
}

data class TeamSearchItemDto(
    @SerializedName("ID") override val id: String,
    @SerializedName("TYPE") override val type: String = "team",
    @SerializedName("SPORT_ID") override val sportId: Int? = null,
    @SerializedName("NAME") val name: String,
    @SerializedName("COUNTRY_NAME") val countryName: String? = null,
    @SerializedName("IMAGE") val image: String? = null
) : SearchItemDto

data class PlayerSearchItemDto(
    @SerializedName("ID") override val id: String,
    @SerializedName("TYPE") override val type: String = "playersInTeam",
    @SerializedName("SPORT_ID") override val sportId: Int? = null,
    @SerializedName("NAME") val name: String,
    @SerializedName("IMAGE") val image: String? = null,
    @SerializedName("COUNTRY_NAME") val countryName: String? = null
) : SearchItemDto

data class TournamentSearchItemDto(
    @SerializedName("ID") override val id: String,
    @SerializedName("TYPE") override val type: String = "tournament",
    @SerializedName("SPORT_ID") override val sportId: Int? = null,
    @SerializedName("NAME") val name: String,
    @SerializedName("IMAGE") val image: String? = null,
    @SerializedName("COUNTRY_NAME") val countryName: String? = null,
    @SerializedName("TOURNAMENT_ID") val tournamentId: String? = null,
    @SerializedName("TOURNAMENT_STAGE_IDS_WITH_STATS_DATA") val tournamentStageIds: List<String>? = null
) : SearchItemDto

data class MultiSearchResponse(
    @SerializedName("DATA") val data: List<SearchItemDto>? = null
)

data class UnknownSearchItemDto(
    @SerializedName("ID") override val id: String = "unknown",
    @SerializedName("TYPE") override val type: String = "unknown",
    @SerializedName("SPORT_ID") override val sportId: Int? = null
) : SearchItemDto

data class TeamTransfersResponse(
    @SerializedName("DATA") val data: List<TransferData>?
)

data class TransferData(
    @SerializedName("DATE") val transferDate: Long?,
    @SerializedName("TRANSFER_TYPE") val transferTypeStr: String?,
    @SerializedName("TRANSFER_DIRECTION") val transferDirection: String?,
    @SerializedName("FROM") val fromTeam: TransferTeam?,
    @SerializedName("TO") val toTeam: TransferTeam?,
    @SerializedName("PLAYER") val player: TransferPlayer?
) {
    val transferId: String? get() = player?.participantId
    val playerId: String? get() = player?.participantId
    val playerName: String? get() = player?.value
    val transferType: Int? get() = null
    val fromTeamId: String? get() = null
    val fromTeamName: String? get() = fromTeam?.value
    val toTeamId: String? get() = null
    val toTeamName: String? get() = toTeam?.value
    val transferReason: String? get() = transferTypeStr
}

data class TransferTeam(
    @SerializedName("VALUE") val value: String?,
    @SerializedName("PARTICIPANT_IMAGE") val image: String?
)

data class TransferPlayer(
    @SerializedName("PARTICIPANT_ID") val participantId: String?,
    @SerializedName("VALUE") val value: String?,
    @SerializedName("PARTICIPANT_IMAGE") val image: String?,
    @SerializedName("CRNA") val countryName: String?
)

data class EventNewsResponse(
    @SerializedName("DATA") val data: List<NewsItem>? = null
)

data class NewsItem(
    @SerializedName("ID") val id: String? = null,
    @SerializedName("TITLE") val title: String? = null,
    @SerializedName("LINK") val link: String? = null,
    @SerializedName("PUBLISHED") val published: Long? = null,
    @SerializedName("PROVIDER_NAME") val providerName: String? = null,
    @SerializedName("LINKS") val links: List<NewsImageLink>? = null
)

data class NewsImageLink(
    @SerializedName("IMAGE_VARIANT_ID") val variantId: Int? = null,
    @SerializedName("IMAGE_VARIANT_URL") val url: String? = null
)
