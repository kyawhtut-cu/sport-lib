package com.kyawhtut.sport

import android.annotation.SuppressLint
import java.security.KeyManagementException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.security.cert.CertificateException

/**
 * @author kyawhtut
 * @date 31/10/2020
 */
@SuppressLint("TrustAllX509TrustManager")
internal object Utils {

    val socketFactory: SSLSocketFactory
        get() {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<out X509Certificate>? {
                    return arrayOf()
                }
            })

            try {
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                return sslContext.socketFactory
            } catch (e: Exception) {
                when (e) {
                    is RuntimeException, is KeyManagementException -> {
                        throw RuntimeException("Failed to create a SSL socket factory", e)
                    }
                    else -> throw e
                }
            }
        }

}
