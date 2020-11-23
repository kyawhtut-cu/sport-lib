package com.kyawhtut.sport.fefatv

import com.kyawhtut.sport.Utils
import com.kyawhtut.sport.fefatv.`object`.FeFaCategory
import com.kyawhtut.sport.fefatv.`object`.FeFaModel
import com.kyawhtut.sport.fefatv.`object`.FeFaRequest.Companion.getHighlightChannels
import com.kyawhtut.sport.fefatv.`object`.FeFaRequest.Companion.getLatestChannels
import com.kyawhtut.sport.fefatv.`object`.FeFaRequest.Companion.homeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup

/**
 * @author kyawhtut
 * @date 27/10/2020
 */
object FeFaSport {

    private const val BASE_URL = "https://dashboard-v3.burmatv.net/api.php"

    private suspend fun getCategoryID(isLive: Boolean): String {
        return withContext(Dispatchers.IO) {
            try {
                val home = Jsoup.connect(BASE_URL)
                    .userAgent("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")
                    .data("data", homeData)
                    .ignoreContentType(true)
                    .sslSocketFactory(Utils.socketFactory)
                    .post()
                val category = parseHome(home.body().text(), isLive)
                category?.cID ?: throw Exception("Category Not Found.")
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun getLiveSport(page: Int = 1): List<FeFaModel> {
        return withContext(Dispatchers.IO) {
            try {
                val data = Jsoup.connect(BASE_URL)
                    .userAgent("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")
                    .data("data", getLatestChannels(page))
                    .ignoreContentType(true)
                    .sslSocketFactory(Utils.socketFactory)
                    .post()
                parseChannel(data.body().text())
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun getHighlightSport(page: Int = 1): List<FeFaModel> {
        return withContext(Dispatchers.IO) {
            try {
                val data = Jsoup.connect(BASE_URL)
                    .userAgent("Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36")
                    .data("data", getHighlightChannels(page))
                    .ignoreContentType(true)
                    .sslSocketFactory(Utils.socketFactory)
                    .post()
                parseChannel(data.body().text())
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun parseLiveURL(url: String): String {
        return withContext(Dispatchers.IO) {
            try {
                if (url.contains("m3u8", true)) {
                    return@withContext url
                }
                if (url.contains("youtube.com", true)) {
                    throw Exception("Youtube link can't parse right now.")
                }
                val response = Jsoup.connect(url)
                    .userAgent("Burma-TV-Sport-Movies-Series-Streaming-Solution-Agent")
                    .ignoreContentType(true)
                    .followRedirects(false)
                    .sslSocketFactory(Utils.socketFactory)
                    .execute()
                response.header("location")
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun parseHome(value: String, isLive: Boolean): FeFaCategory? {
        val json = JSONObject(value).getJSONObject("LIVETV").getJSONArray("cat_list")
        val result = mutableListOf<FeFaCategory>()
        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            val categoryID = obj.getString("cid")
            val categoryName = obj.getString("category_name")
            val categoryImage = obj.getString("category_image")
            val categoryImageThumb = obj.getString("category_image_thumb")
            result.add(
                FeFaCategory(categoryID, categoryName, categoryImage, categoryImageThumb)
            )
        }
        return if (isLive) result.firstOrNull {
            it.categoryName.contains(
                "live",
                true
            )
        } else result.firstOrNull { it.categoryName.contains("highlights", true) }
    }

    private fun parseChannel(value: String): List<FeFaModel> {
        val json = JSONObject(value).getJSONArray("LIVETV")
        val result = mutableListOf<FeFaModel>()
        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            val id = obj.getString("id")
            val categoryID = obj.getString("cat_id")
            val channelTitle = obj.getString("channel_title")
            val channelPlayURL = obj.getString("channel_url")
            val channelThumbnail = obj.getString("channel_thumbnail")
            result.add(
                FeFaModel(id, categoryID, channelTitle, channelPlayURL, channelThumbnail)
            )
        }
        return result
    }
}
