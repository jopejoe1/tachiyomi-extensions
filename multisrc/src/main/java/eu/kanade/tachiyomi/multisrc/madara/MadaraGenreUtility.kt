package eu.kanade.tachiyomi.multisrc.madara

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
/*
* Based on the one in MMRCMS extension
 */
@TargetApi(Build.VERSION_CODES.O)
fun generateGenres(baseUrl: String): List<Madara.Genre> {
    return try {
        val document = getDocument("$baseUrl/?s=&post_type=wp-manga")
        parseIds(document!!)
    }catch (e: Exception) {
        emptyList()
    }
}

private fun getDocument(url: String): Document? {
    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3")
    val serverCheck = arrayOf("cloudflare-nginx", "cloudflare")

    try {
        val request = Request.Builder().url(url)
        getOkHttpClient().newCall(request.build()).execute().let { response ->
            // Bypass Cloudflare ("Please wait 5 seconds" page)
            if (response.code == 503 && response.header("Server") in serverCheck) {
                var cookie = "${response.header("Set-Cookie")!!.substringBefore(";")}; "
                Jsoup.parse(response.body!!.string()).let { document ->
                    val path = document.select("[id=\"challenge-form\"]").attr("action")
                    val chk = document.select("[name=\"s\"]").attr("value")
                    getOkHttpClient().newCall(Request.Builder().url("$url/$path?s=$chk").build()).execute().let { solved ->
                        cookie += solved.header("Set-Cookie")!!.substringBefore(";")
                        request.addHeader("Cookie", cookie).build().let {
                            return Jsoup.parse(getOkHttpClient().newCall(it).execute().body?.string())
                        }
                    }
                }
            }
            if (response.code == 200) {
                return Jsoup.parse(response.body?.string())
            }
        }
    } catch (e: Exception) {
    }
    return null
}

private fun parseIds(Document: Document): List<Madara.Genre> {
    val elements = Document.select("div.checkbox-group > div.checkbox > label")
    val ids = mutableListOf<Madara.Genre>()
    if (elements.isEmpty()) {
        return ids
    }
    elements.forEach {
        ids.add(Madara.Genre(it.text(), it.attr("for")))
    }
    return ids
}

@Throws(Exception::class)
private fun getOkHttpClient(): OkHttpClient {
    System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3")
    val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
            }

            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                return arrayOf()
            }
        }
    )

    // Install the all-trusting trust manager

    val sc = SSLContext.getInstance("SSL")
    sc.init(null, trustAllCerts, java.security.SecureRandom())
    val sslSocketFactory = sc.socketFactory

    // Create all-trusting host name verifier
    // Install the all-trusting host verifier

    return OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true }
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .build()
}
