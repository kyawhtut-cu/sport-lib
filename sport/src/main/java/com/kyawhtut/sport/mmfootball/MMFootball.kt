package com.kyawhtut.sport.mmfootball

import com.kyawhtut.sport.mmfootball.`object`.MMFootballModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

/**
 * @author kyawhtut
 * @date 02/11/2020
 */
object MMFootball {

    private const val SPLASH_URL = "https://json.mmlivescore.com/mm5football/home/splash.json?ts=%s"
    private const val LIVE_URL = "https://json.mmlivescore.com/mm5football/reload/today.json?ts=%s"
    private const val POPULAR_URL =
        "https://json.mmlivescore.com/mm5football/reload/popular.json?ts=%s"
    private const val HIGHLIGHT_URL =
        "https://json.mmlivescore.com/mm5football/reload/highlights.json?ts=%s"

    private suspend fun getSplash(key: String): String {
        return withContext(Dispatchers.IO) {
            val response = URL(SPLASH_URL.format(System.currentTimeMillis() / 1000)).readText()
            val jsonObj = JSONObject(response).getJSONObject("meta")
            jsonObj.getString(key)
        }
    }

    suspend fun getLive(): List<MMFootballModel> {
        return withContext(Dispatchers.IO) {
            val response = URL(LIVE_URL.format(System.currentTimeMillis() / 1000)).readText()
            parse(response, true).filter { !it.title.contains("apk update", true) }
        }
    }

    suspend fun getPopular(): List<MMFootballModel> {
        return withContext(Dispatchers.IO) {
            val response = URL(POPULAR_URL.format(System.currentTimeMillis() / 1000)).readText()
            parse(response, true).filter { !it.title.contains("apk update", true) }
        }
    }

    suspend fun getHighlight(): List<MMFootballModel> {
        return withContext(Dispatchers.IO) {
            val response = URL(HIGHLIGHT_URL.format(System.currentTimeMillis() / 1000)).readText()
            parse(response, false).filter { !it.title.contains("apk update", true) }
        }
    }

    suspend fun parseLiveURL(data: MMFootballModel): String {
        return withContext(Dispatchers.IO) {
            if (data.url.isEmpty()) throw Exception("URL is empty.")
            if (!data.url.any {
                    it.contains("m3u8", true) || it.contains(
                        "txt",
                        true
                    )
                }
            ) throw Exception("URL is not supported.")
            if (data.url.any { it.contains("m3u8", true) }) {
                return@withContext data.url.first { it.contains("m3u8", true) }
            }
            val cdn = getSplash("cdn")
            val streamKey = URL(data.url.first { it.contains("txt", true) }).readText().trim()
            "%s%s%s.m3u8".format(cdn, "stream", streamKey)
        }
    }

    private fun parse(value: String, isLive: Boolean): List<MMFootballModel> {
        val result = mutableListOf<MMFootballModel>()
        val jsonArray = JSONObject(value).getJSONObject(if (isLive) "today" else "highlights")
            .getJSONArray("posts")
        for (index in 0 until jsonArray.length()) {
            val post = jsonArray.getJSONObject(index)
            val image = post.getString("image")
            val title = post.getString("title")
            val categories = post.getJSONArray("categories")
            val link = post.getJSONArray("links")
            val links = mutableListOf<String>()
            for (row in 0 until link.length()) {
                links.add(link.getString(row))
            }
            result.add(
                MMFootballModel(
                    title,
                    image,
                    if (!isLive) {
                        if (categories.length() == 0) "" else categories.getString(0)
                    } else {
                        if (categories.length() == 0) ""
                        else categories.getString(1)
                    },
                    if (isLive) {
                        if (categories.length() == 0) null else categories.getString(0)
                    } else null,
                    links
                )
            )
        }
        return result
    }
}
