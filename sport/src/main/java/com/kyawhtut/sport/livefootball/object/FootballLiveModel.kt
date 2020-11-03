package com.kyawhtut.sport.livefootball.`object`

/**
 * @author kyawhtut
 * @date 02/11/2020
 */
data class FootballLiveModel(
    val id: Int,
    val homeTeam: String,
    val homeTeamImage: String,
    val awayTeam: String,
    val awayTeamImage: String,
    val time: String,
    val date: String,
    val matchType: String
)
