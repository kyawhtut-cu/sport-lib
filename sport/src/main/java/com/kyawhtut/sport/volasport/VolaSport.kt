package com.kyawhtut.sport.volasport

import com.kyawhtut.sport.volasport.`object`.VolaSportModel
import com.kyawhtut.sport.volasport.`object`.VolaSportRequest
import com.kyawhtut.sport.volasport.`object`.VolaSportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder

/**
 * @author kyawhtut
 * @date 28/10/2020
 */
object VolaSport {

    private const val BASE_URL = "https://apknet.xyz/v41/%s"

    private fun getToken(): String {
        return "token=%s".format(
            URLEncoder.encode(
                VolaSportRequest.toBase64(VolaSportRequest().toJson()),
                "UTF-8"
            )
        )
    }

    private suspend fun getUserAgent(): String {
        return withContext(Dispatchers.IO) {
            val agent = JSONObject(
                Jsoup.connect(BASE_URL.format("userAgents.php"))
                    .ignoreContentType(true).get().body().text()
            ).getJSONArray("userAgents")
            if (agent.length() == 0) throw Exception("User agent not found.") else agent.getString(0)
        }
    }

    suspend fun getVolaSport(): List<VolaSportModel> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    Jsoup.connect(BASE_URL.format("s1.php?${getToken()}")).get()
                val newsResponse = Jsoup.connect(BASE_URL.format("news.php?${getToken()}")).get()
                parseVolaSport(response) + parseVolaNew(newsResponse)
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun parsePlayURL(url: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = Jsoup.connect(url).followRedirects(false).execute()
                response.header("location")
            } catch (e: Exception) {
                throw e
            }
        }
    }

    suspend fun getLivePlayLink(url: String): List<Pair<String, String>> {
        return withContext(Dispatchers.IO) {
            try {
                val response =
                    Jsoup.connect(url + "&" + getToken()).get()
                response.select("a").map { element ->
                    element.text() to BASE_URL.format(
                        element.attr(
                            "href"
                        ),
                        getToken()
                    )
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }

    private fun parseVolaSport(value: Document): List<VolaSportModel> {
        val result = mutableListOf<VolaSportModel>()
        value.select(".table tr[onclick]").iterator().forEach { element ->
            val image = element.select(".img-fluid").first().absUrl("src")
            val col = element.select(".col").toList()
            if (col.isNotEmpty()) {
                val row = col.first().select(".row").toList()
                val url = element.attr("onclick").run {
                    this.substring(
                        this.indexOf("location='") + "location='".length,
                        this.length - 2
                    )
                }
                if (row.size == 3) {
                    result.add(
                        VolaSportModel(
                            row[1].text(),
                            image,
                            row[2].text().isNotEmpty(),
                            BASE_URL.format(url),
                            if (url.contains(
                                    "detail.php",
                                    true
                                )
                            ) VolaSportType.LIVE else VolaSportType.HIGHLIGHT
                        )
                    )
                    if (url.contains("detail.php")) {
                    }
                }
            }
        }
        return result
    }

    private fun parseVolaNew(value: Document): List<VolaSportModel> {
        val result = mutableListOf<VolaSportModel>()
        value.select(".table tr[onclick]").iterator().forEach { element ->
            val url = element.attr("onclick").run {
                this.substring(
                    this.indexOf("location='") + "location='".length,
                    this.length - 2
                )
            }
            val image = element.select(".img-fluid").first().absUrl("src")
            val col = element.select(".col").toList()
            if (col.isNotEmpty()) {
                val row = col.first().select(".row").toList()
                if (row.size == 3) {
                    result.add(
                        VolaSportModel(
                            row[1].text(),
                            image,
                            row[2].text().isNotEmpty(),
                            BASE_URL.format(url),
                            VolaSportType.NEWS
                        )
                    )
                }
            }
        }
        return result
    }
}
