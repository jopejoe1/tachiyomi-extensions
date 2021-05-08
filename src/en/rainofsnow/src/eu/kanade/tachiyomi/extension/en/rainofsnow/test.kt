package eu.kanade.tachiyomi.extension.en.rainofsnow

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Headers
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.userAgent
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.PrintWriter
import java.security.cert.CertificateException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class test {
    init {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3")
    }
    val client: OkHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val types = listOf("artists")

    @TargetApi(Build.VERSION_CODES.O)
    fun generate() {
        val buffer = StringBuffer()
        val dateTime = ZonedDateTime.now()
        val formattedDate = dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)
        buffer.append("package eu.kanade.tachiyomi.extension.en.rainofsnow")
        buffer.append("\n\n// GENERATED FILE, DO NOT MODIFY!\n// Generated $formattedDate\n\n")
        var number = 1
        types.forEach {
            try {
                val document = getDocument("https://rainofsnow.com/comic_chapters/yoko-taro-1/?novelid=14186")!!
                val baseUrl = "https://rainofsnow.com"
                // val ids = parseIds(document!!, it)
                val pages = mutableListOf<Page>()
                val images = mutableListOf<String>()
                fun headersBuilder(): Headers.Builder = Headers.Builder()
                    .add("Referer", baseUrl)
                    .add("User-Agent", userAgent)
                    .add("Content-Type", "application/x-www-form-urlencoded")

                document.select("[style=display: block;] img").forEach { element ->
                    images.add(element.attr("abs:src"))
                }

                val js = document.select(".zoomdesc-cont .chap-img-smlink + script").html()
                val postId = js.substringAfter("var my_repeater_field_post_id = ").substringBefore(";").trim()
                var postOffset = js.substringAfter("var my_repeater_field_offset = ").substringBefore(";").trim()
                val postNonce = js.substringAfter("var my_repeater_field_nonce = ").substringBefore(";").trim()
                var morePages = js.substringAfter("var my_repeater_more = ").substringBefore(";").trim().toBoolean()
                buffer.append("js = $js\n\n")
                buffer.append("postId = $postId\n")
                buffer.append("postOffset = $postOffset\n")
                buffer.append("postNonce = $postNonce\n")
                buffer.append("morePages = $morePages\n")

                while (morePages) {
                    val url = "$baseUrl/wp-admin/admin-ajax.php".toHttpUrlOrNull()!!.newBuilder()
                    val requestBody = "action=my_repeater_show_more&post_id=$postId&offset=$postOffset&nonce=$postNonce".toRequestBody(null)
                    val request = POST(url.toString(), headersBuilder().build(), requestBody)
                    val ajax = client.newCall(request).execute().asJsoup()
                    buffer.append("ajax = $morePages\n\n")
                    ajax.select("img").forEach { img ->
                        images.add(img.attr("abs:src"))
                        buffer.append("Image = ${img.attr("abs:src")}\n")
                    }
                    morePages = ajax.html().contains("\"more\":true")
                    postOffset = ajax.html().substringAfterLast(":").substringBefore("}")
                    buffer.append("postOffset = $postOffset\n")
                    buffer.append("morePages = $morePages\n")
                }

                for ((pageNum, image) in images.withIndex()) {
                    pages.add(Page(pageNum, image, image))
                }
                number++
            } catch (e: Exception) {
                println("error generating source $it ${e.printStackTrace()}")
            }
        }
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
        val elements = Document.select("[name=\"$type${"[]"}\"] > option")
        val ids = mutableListOf<Pair<String, String>>()
        if (elements.isEmpty()) {
            return ids
        }
        elements.forEach {
            ids.add(Pair(it.text(), it.attr("value")))
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

        val relativePath = System.getProperty("user.dir") + "/src/en/rainofsnow/src/eu/kanade/tachiyomi/extension/en/rainofsnow/output.kt"

        @JvmStatic
        fun main(args: Array<String>) {
            test().generate()
        }
    }
}
