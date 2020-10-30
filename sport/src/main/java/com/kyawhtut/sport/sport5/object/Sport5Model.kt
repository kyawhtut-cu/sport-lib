package com.kyawhtut.sport.sport5.`object`

/**
 * @author kyawhtut
 * @date 27/10/2020
 */
data class Sport5Model(
    val id: Long,
    val matchTitle: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: String,
    val awayScore: String,
    val isLive: Boolean,
    val sport5Type: Sport5Type,
    var playURL: String? = null
) {
    override fun toString(): String {
        return "%s\n%s%s\n%s - %s\n%s - %s\n%s".format(
            sport5Type,
            matchTitle, if (isLive) " (Live)" else "",
            homeTeam, awayTeam,
            homeScore, awayScore
        )
    }
}
