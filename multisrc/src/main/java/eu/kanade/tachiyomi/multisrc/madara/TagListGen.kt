package eu.kanade.tachiyomi.multisrc.madara

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import java.io.PrintWriter
import java.security.cert.CertificateException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class TagListGen {
    init {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3")
    }
    fun sources(): List<Any> {
        // Uncomment when
        return emptyList()
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun generate() {
        val buffer = StringBuffer()
        val dateTime = ZonedDateTime.now()
        val formattedDate = dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)
        buffer.append("package eu.kanade.tachiyomi.multisrc.madara")
        buffer.append("\n\n// GENERATED FILE, DO NOT MODIFY!\n// Generated $formattedDate\n\n")
        buffer.append("fun GenreList(url:String) = when (url){\n")
        var number = 1
        sources().forEach {
            try {
                val baseUrl = it//.baseUrl
                val document = getDocument("${baseUrl}/?s=&post_type=wp-manga")
                val ids = parseIds(document!!, "genre")


                buffer.append("    \"${baseUrl}\" -> listOf(\n")
                for (id in ids) {
                    buffer.append("        Madara.Genre(\"${id.first}\",\"${id.second}\"),\n")
                }
                buffer.append("    )\n")
                number++
            } catch (e: Exception) {
                println("error generating source $it ${e.printStackTrace()}")
            }
        }
        buffer.append("    else -> emptyList()\n")
        buffer.append("}\n")
        println("Post-run types: ${number - 1}")
        val writer = PrintWriter(relativePath)
        writer.write(buffer.toString())
        writer.close()
    }

    private fun getDocument(url: String): Document? {
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
            e.printStackTrace()
        }
        return null
    }

    private fun parseIds(Document: Document, type: String): List<Pair<String, String>> {
        val elements = Document.select("form.search-advanced-form > div.checkbox-group > div.checkbox")
        val ids = mutableListOf<Pair<String, String>>()
        if (elements.isEmpty()) {
            return ids
        }
        elements.forEach {
            val value = it.select("input").attr("value")
            val name = it.select("label").text()
            ids.add(Pair(name, value))
        }
        return ids
    }

    @Throws(Exception::class)
    private fun getOkHttpClient(): OkHttpClient {
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

    companion object {

        val relativePath = System.getProperty("user.dir") + "/multisrc/src/main/java/eu/kanade/tachiyomi/multisrc/madara/MadaraTags.kt"

        @JvmStatic
        fun main(args: Array<String>) {
            TagListGen().generate()
        }
    }
}
