package com.kyawhtut.sport.fefatv.`object`

import android.util.Base64
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

internal class FeFaRequest {

    companion object {
        private fun String.toBase64(): String = String(
            Base64.encode(
                toByteArray(),
                0
            )
        )

        private fun getData(vararg data: Pair<String, String>): String {
            return FeFaRequest().toJson().apply {
                data.forEach {
                    try {
                        put(it.first, it.second.toInt() as Number)
                    } catch (e: NumberFormatException) {
                        put(it.first, it.second)
                    }
                }
            }.run {
                toString().toBase64()
            }
        }

        val homeData: String
            get() = getData(
                "method_name" to "get_home"
            )

        fun getHighlightChannels(page: Int): String = getData(
            "method_name" to "get_highlight_channels",
            "page" to "$page"
        )

        fun getLatestChannels(page: Int): String = getData(
            "method_name" to "get_latest_channels",
            "page" to "$page"
        )

        fun getChannelByCID(cID: String, page: String): String = getData(
            "method_name" to "get_channels_by_cat_id",
            "cat_id" to cID,
            "page" to page
        )
    }

    private val salt: String
    private val sign: String?
    private val randomSalt: Int
        get() = Random().nextInt(900)

    private fun md5(value: String): String? {
        return try {
            val arrby: MessageDigest = MessageDigest.getInstance("MD5")
            arrby.update(value.toByteArray())
            val arrbyData = arrby.digest()
            val charSequence = StringBuilder()
            var n = 0
            do {
                if (n >= arrbyData.size) break
                charSequence.append(
                    String.format(
                        "%02x",
                        arrbyData[n]
                    )
                )
                ++n
                continue
                break
            } while (true)
            charSequence.toString()
        } catch (noSuchAlgorithmException: NoSuchAlgorithmException) {
            noSuchAlgorithmException.printStackTrace()
            null
        }
    }

    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("salt", salt)
        jsonObject.put("sign", sign)
        return jsonObject
    }

    init {
        var stringBuilder = StringBuilder()
        stringBuilder.append("")
        stringBuilder.append(randomSalt)
        salt = stringBuilder.toString()
        stringBuilder = StringBuilder()
        stringBuilder.append("Burma-TV-Movies-Series-Streaming-Solution")
        stringBuilder.append(salt)
        sign = md5(stringBuilder.toString())
    }
}
