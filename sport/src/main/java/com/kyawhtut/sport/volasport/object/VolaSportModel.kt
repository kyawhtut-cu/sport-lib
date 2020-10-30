package com.kyawhtut.sport.volasport.`object`

/**
 * @author kyawhtut
 * @date 23/08/2020
 */
data class VolaSportModel(
    val title: String,
    val image: String,
    val isLive: Boolean,
    val url: String,
    val type: VolaSportType
)
