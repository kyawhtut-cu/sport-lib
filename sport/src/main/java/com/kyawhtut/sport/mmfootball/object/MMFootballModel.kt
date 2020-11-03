package com.kyawhtut.sport.mmfootball.`object`

/**
 * @author kyawhtut
 * @date 02/11/2020
 */
data class MMFootballModel(
    val title: String,
    val image: String,
    val quality: String,
    val time: String?,
    val url: List<String>
)
