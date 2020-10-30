package com.kyawhtut.sport.volasport.`object`

import android.util.Base64
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author kyawhtut
 * @date 08/07/2020
 */
internal class VolaSportRequest {
    private val time: String
    private val salt: String
    private val sign: String?
    private val randomSalt: Int
        get() = Random().nextInt(900)

    private fun md5(string: String): String? {
        return try {
            val instance = MessageDigest.getInstance("MD5")
            instance.update(string.toByteArray())
            val digest = instance.digest()
            val sb = StringBuilder()
            for (i in digest.indices) {
                sb.append(String.format("%02x", digest[i]))
            }
            sb.toString()
        } catch (ex: NoSuchAlgorithmException) {
            ex.printStackTrace()
            null
        }
    }

    companion object {
        fun toBase64(s: String): String {
            return String(Base64.encode(s.toByteArray(), 0))
        }

        fun getTime(format: String): String {
            val default1 = TimeZone.getDefault()
            TimeZone.setDefault(TimeZone.getTimeZone(format))
            val timeFormat: String = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH)
                .format(Calendar.getInstance().time)
            TimeZone.setDefault(default1)
            return timeFormat
        }
    }

    init {
        val sb = StringBuilder()
        sb.append("")
        sb.append(randomSalt)
        salt = sb.toString()
        val sb2 = StringBuilder()
        sb2.append("#~rFo*%&I0#0i?JOM-0KTo,MgF.ufiL:z!y>lh,/Oo=hnO!6T|{S&<dj+Hl7M2§8#~rFo*%&I0#0i?JOM-0KTo,MgF.ufiL:z!y>lh,/Oo=hnO!6T|{S&<dj+Hl7M2§8#~rFo*%&I0#0i?JOM-0KTo,MgF.ufiL:z!y>lh,/Oo=hnO!6T|{S&<dj+Hl7M2§8")
        sb2.append(salt)
        sign = md5(sb2.toString())
        time = getTime("UTC")
    }

    fun toJson(): String {
        val jsonObject = JSONObject()
        jsonObject.put("timesamp", time)
        jsonObject.put("salt", salt)
        jsonObject.put("sign", sign)
        return jsonObject.toString()
    }
}
