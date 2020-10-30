package com.kyawhtut.sport.sport5

import com.kyawhtut.sport.sport5.`object`.Sport5Model
import com.kyawhtut.sport.sport5.`object`.Sport5Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * @author kyawhtut
 * @date 27/10/2020
 */
object Sport5 {

    suspend fun getAll(isExcludeUnLive: Boolean = false): List<Sport5Model> {
        return withContext(Dispatchers.IO) {
            try {
                val football = getFootball(isExcludeUnLive)
                val basketball = getBasketball(isExcludeUnLive)
                val tennis = getTennis(isExcludeUnLive)
                val esport = getESport(isExcludeUnLive)
                football + basketball + tennis + esport
            } catch (e: Exception) {
                throw  e
            }
        }
    }

    suspend fun getFootball(isExcludeUnLive: Boolean = false): List<Sport5Model> {
        return withContext(Dispatchers.IO) {
            try {
                val football = Jsoup.connect("https://www.555sports.com/football").get()
                parseData(football, Sport5Type.FOOTBALL, isExcludeUnLive)
            } catch (e: Exception) {
                throw  e
            }
        }
    }

    suspend fun getBasketball(isExcludeUnLive: Boolean = false): List<Sport5Model> {
        return withContext(Dispatchers.IO) {
            try {
                val basketball = Jsoup.connect("https://www.555sports.com/basketball").get()
                parseData(basketball, Sport5Type.BASKETBALL, isExcludeUnLive)
            } catch (e: Exception) {
                throw  e
            }
        }
    }

    suspend fun getTennis(isExcludeUnLive: Boolean = false): List<Sport5Model> {
        return withContext(Dispatchers.IO) {
            try {
                val tennis = Jsoup.connect("https://www.555sports.com/tennis").get()
                parseData(tennis, Sport5Type.TENNIS, isExcludeUnLive)
            } catch (e: Exception) {
                throw  e
            }
        }
    }

    suspend fun getESport(isExcludeUnLive: Boolean = false): List<Sport5Model> {
        return withContext(Dispatchers.IO) {
            try {
                val esport = Jsoup.connect("https://www.555sports.com/esports").get()
                parseData(esport, Sport5Type.ESPORTS, isExcludeUnLive)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun parseURL(sport5: Sport5Model): Sport5Model {
        return withContext(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect("https://www.555sports.com${sport5.playURL}").get()
                with(doc.body().data()) {
                    this.substring(this.indexOf("m3u8Url:"), this.indexOf(",available"))
                        .split("\"")
                }.run {
                    if (this.size == 3) {
                        val url = Jsoup.connect(this@run[1].replace("\\u002F", "/")).header(
                            "Referer", "https://www.555sports.com/"
                        ).ignoreContentType(true).get()
                        var play: String
                        with(url.body().text()) {
                            play = this.substring(this.indexOf("https"))
                        }
                        sport5.copy().apply {
                            playURL = play
                        }
                    } else throw Exception("URL is not live now.")
                }
            } catch (e: Exception) {
                throw  Exception("URL is not live now.")
            }
        }
    }

    private fun parseData(
        html: Document,
        type: Sport5Type,
        isExcludeUnLive: Boolean
    ): List<Sport5Model> {
        val list = mutableListOf<Sport5Model>()
        var index = 0L
        html.select("a.match-item").iterator().forEach {
            val matchTitle = it.select("span.name").text()
            val nameBox = it.select("span.name-box")
            val homeTeam = nameBox.select("span.home").text()
            val awayTeam = nameBox.select("span.away").text()
            val score = it.select("span.score-box")
            val homeScore = score.select("span.home").text()
            val awayScore = score.select("span.away").text()
            val isLive = it.select("span.icon-box").hasClass("icon-box")
            val link = it.attr("href")

            val sport = Sport5Model(
                index++,
                matchTitle,
                homeTeam,
                awayTeam,
                homeScore,
                awayScore,
                isLive,
                type,
                link
            )
            list.add(sport)
        }
        return list.filter { it.playURL != null }.filter {
            it.isLive || isExcludeUnLive
        }
    }
}
