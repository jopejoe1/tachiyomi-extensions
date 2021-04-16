package eu.kanade.tachiyomi.multisrc.mmrcms

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.jvm.Throws

/**
 * This class generates the sources for MMRCMS.
 * Credit to nulldev for writing the original shell script
 *
 * CMS: https://getcyberworks.com/product/manga-reader-cms/
 */

class MMRCMSJsonGenHS (
        private val name: String,
        private val baseUrl: String,
        private val lang: String){

    init {
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2,TLSv1.3")
    }

    @TargetApi(Build.VERSION_CODES.O)
    public fun generateJson(): String {
        val map = mutableMapOf<String, Any>()
        map["language"] = lang
        map["name"] = name
        map["base_url"] = baseUrl
        map["supports_latest"] = supportsLatest(baseUrl)

        val advancedSearchDocument = getDocument("${baseUrl}/advanced-search", false)

        var parseCategories = mutableListOf<Map<String, String>>()
        var pareseTags = mutableListOf<Map<String, String>>()
        var pareseLanguages = mutableListOf<Map<String, String>>()
        var pareseStatus = mutableListOf<Map<String, String>>()
        var pareseParodies = mutableListOf<Map<String, String>>()
        var pareseCharacters = mutableListOf<Map<String, String>>()
        if (advancedSearchDocument != null) {
            parseCategories = parseOptions(advancedSearchDocument, "categories")
            //pareseTags = parseOptions(advancedSearchDocument, "tags")
            pareseLanguages = parseOptions(advancedSearchDocument, "languages")
            pareseStatus = parseOptions(advancedSearchDocument, "status")
            //pareseParodies = parseOptions(advancedSearchDocument, "parodies")
            //pareseCharacters = parseOptions(advancedSearchDocument, "characters") TOO MANY
        }

        val homePageDocument = getDocument(baseUrl)

        val itemUrl = getItemUrl(homePageDocument, baseUrl)

        // Sometimes itemUrl is the root of the website, and thus the prefix found is the website address.
        // In this case, we set the default prefix as "manga".

        map["item_url"] = "$itemUrl/"
        map["categories"] = "null"
        if (parseCategories.size in 1..49) {
            map["tags"] = parseCategories
        }

        map["languages"] = "null"
        if (pareseLanguages.size in 1..49) {
            map["tags"] = pareseLanguages
        }

        map["status"] = "null"
        if (pareseStatus.size in 1..49) {
            map["tags"] = pareseStatus
        }

        map["parodies"] = "null"
        if (pareseParodies.size in 1..49) {
            map["tags"] = pareseParodies
        }

        map["characters"] = "null"
        if (pareseCharacters.size in 1..49) {
            map["tags"] = pareseCharacters
        }

        map["tags"] = "null"
        if (pareseTags.size in 1..49) {
            map["tags"] = pareseTags
        }

        if (!itemUrl.startsWith(baseUrl)) println("**Note: $name} URL does not match! Check for changes: \n $baseUrl vs $itemUrl")

        return Gson().toJson(map)
    }

    private fun getDocument(url: String, printStackTrace: Boolean = true): Document? {
        val serverCheck = arrayOf("cloudflare-nginx", "cloudflare")

        try {
            val request = Request.Builder().url(url)
            getOkHttpClient().newCall(request.build()).execute().let { response ->
                // Bypass Cloudflare ("Please wait 5 seconds" page)
                if (response.code() == 503 && response.header("Server") in serverCheck) {
                    var cookie = "${response.header("Set-Cookie")!!.substringBefore(";")}; "
                    Jsoup.parse(response.body()!!.string()).let { document ->
                            val path = document.select("[id=\"challenge-form\"]").attr("action")
                        val chk = document.select("[name=\"s\"]").attr("value")
                        getOkHttpClient().newCall(Request.Builder().url("$url/$path?s=$chk").build()).execute().let { solved ->
                                cookie += solved.header("Set-Cookie")!!.substringBefore(";")
                            request.addHeader("Cookie", cookie).build().let {
                                return Jsoup.parse(getOkHttpClient().newCall(it).execute().body()?.string())
                            }
                        }
                    }
                }
                if (response.code() == 200) {
                    return Jsoup.parse(response.body()?.string())
                }
            }
        } catch (e: Exception) {
            if (printStackTrace) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun getItemUrl(document: Document?, url: String): String {
        document ?: throw Exception("Couldn't get document for: $url")
        return document.toString().substringAfter("showURL = \"").substringAfter("showURL=\"").substringBefore("/SELECTION\";")

        // Some websites like mangasyuri use javascript minifiers, and thus "showURL = " becomes "showURL="https://mangasyuri.net/manga/SELECTION""
        // (without spaces). Hence the double substringAfter.
    }

    private fun supportsLatest(third: String): Boolean {
        val document = getDocument("$third/latest-release?page=1", false) ?: return false
        return document.select("div.mangalist div.manga-item a, div.grid-manga tr").isNotEmpty()
    }

    private fun parseOptions(document: Document, option: String): MutableList<Map<String, String>> {
        val array = mutableListOf<Map<String, String>>()
        val elements = document.select("select[name^=$option] option")
        if (elements.size == 0) {
            return mutableListOf()
        }
        elements.forEach {
            val map = mutableMapOf<String, String>()
            map["id"] = it.attr("abs:value")
            map["name"] = it.text()
            array.add(map)
        }
        return array
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
}
