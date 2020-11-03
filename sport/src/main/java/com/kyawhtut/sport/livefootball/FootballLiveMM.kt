package com.kyawhtut.sport.livefootball

import com.kyawhtut.sport.Utils
import com.kyawhtut.sport.livefootball.`object`.FootballLiveModel
import com.kyawhtut.sport.livefootball.`object`.FootballLiveURLModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup

/**
 * @author kyawhtut
 * @date 02/11/2020
 */
object FootballLiveMM {

    private const val LIVE_URL = "http://mmsoccerlive.top/api/v1/livestreampost"
    private const val HIGHLIGHT_URL = "http://mmsoccerlive.top/api/v1/highlightpost"
    private const val PLAY_URL = "http://mmsoccerlive.top/api/v1/match/link/%s"

    suspend fun getLive(): List<FootballLiveModel> {
        return withContext(Dispatchers.IO) {
            val response = Jsoup.connect(LIVE_URL)
                .ignoreContentType(true)
                .sslSocketFactory(Utils.socketFactory)
                .get().body().text()
            val data = JSONObject(response).getJSONArray("data")
            mutableListOf<FootballLiveModel>().apply {
                for (index in 0 until data.length()) {
                    val obj = data.getJSONObject(index)
                    val id = obj.getInt("id")
                    val homeTeam = obj.getString("home_name")
                    val homeTeamImage = obj.getString("iconhome")
                    val awayTeam = obj.getString("away_name")
                    val awayTeamImage = obj.getString("iconaway")
                    val time = obj.getString("time")
                    val date = obj.getString("date")
                    val matchType = obj.getString("matchtype")
                    add(
                        FootballLiveModel(
                            id,
                            homeTeam,
                            homeTeamImage,
                            awayTeam,
                            awayTeamImage,
                            time,
                            date,
                            matchType
                        )
                    )
                }
            }
        }
    }

    suspend fun getHighlight(): List<FootballLiveModel> {
        throw Exception("Highlight list can't support in this version.")
        return withContext(Dispatchers.IO) {
            listOf()
        }
    }

    suspend fun parseLiveURL(data: FootballLiveModel): List<FootballLiveURLModel> {
        return withContext(Dispatchers.IO) {
            val response = Jsoup.connect(PLAY_URL.format(data.id))
                .ignoreContentType(true)
                .sslSocketFactory(Utils.socketFactory)
                .get().body().text()
            val data = JSONObject(response).getJSONArray("data")
            mutableListOf<FootballLiveURLModel>().apply {
                for (index in 0 until data.length()) {
                    val obj = data.getJSONObject(index)
                    val id = obj.getInt("id")
                    val title = obj.getString("streamlink_name")
                    val link = obj.getString("link")
                    add(
                        FootballLiveURLModel(
                            id,
                            title,
                            link
                        )
                    )
                }
            }
        }
    }
}
