package eu.kanade.tachiyomi.extension.en.rainofsnow

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.jsoup.Jsoup
import rx.Observable

open class RainOfSnow() : ParsedHttpSource() {

    override val name = "Rain Of Snow"

    override val baseUrl = "https://rainofsnow.com"

    override val lang = "en"

    override val supportsLatest = false

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/comics/page/$page")
    }

    override fun popularMangaSelector() = ".box .minbox"

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        manga.url = element.select("h3 a").attr("abs:href")
        manga.title = element.select("h3").text()
        manga.thumbnail_url = element.select("img").attr("abs:src")
        return manga
    }

    override fun popularMangaNextPageSelector() = ".page-numbers .next"

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        if (query.isNotEmpty()){
            val url = "$baseUrl/".toHttpUrlOrNull()!!.newBuilder()
            url.addQueryParameter("s", query)
            return GET(url.build().toString(), headers)
        }
        val url = "$baseUrl/comics/".toHttpUrlOrNull()!!.newBuilder()
        filters.forEach { filter ->
            when (filter) {
                is AlbumTypeSelectFilter -> {
                    if (filter.state != 0) {
                        url.addQueryParameter("n_orderby", filter.toUriPart())
                    }
                }
            }
        }
        return GET(url.build().toString(), headers)
    }

    override fun searchMangaSelector() = popularMangaSelector()

    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)

    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    override fun mangaDetailsRequest(manga: SManga): Request {
        if (manga.url.startsWith("http")) {
            return GET(manga.url, headers)
        }
        return super.mangaDetailsRequest(manga)
    }

    override fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()
        manga.title = document.select(".text h2").text()
        manga.author = document.select(".vbtcolor1 li:contains(Author) .vt2").text()
        manga.genre = document.select(".vbtcolor1 li:contains(Tags) .vt2").text()
        manga.description = document.select("#synop p").text()
        manga.thumbnail_url = document.select(".imagboca1 img").attr("abs:src")
        return manga
    }

    override fun chapterListRequest(manga: SManga): Request {
        if (manga.url.startsWith("http")) {
            return GET(manga.url, headers)
        }
        return super.chapterListRequest(manga)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        return super.chapterListParse(response).reversed()
    }

    override fun chapterListSelector() = "#chapter li"

    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()
        chapter.url = element.select("a").attr("abs:href")
        chapter.name = element.select("a").text()
        chapter.date_upload = element.select("small").firstOrNull()?.text()
            ?.let { parseChapterDate(it) } ?: 0
        return chapter
    }

    private fun parseChapterDate(date: String): Long {
        var parsedDate = 0L
        try {
            parsedDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).parse(date)?.time ?: 0L
        } catch (e: ParseException) { /*nothing to do, parsedDate is initialized with 0L*/ }
        return parsedDate
    }

    override fun fetchPageList(chapter: SChapter): Observable<List<Page>> {
        return client.newCall(GET(chapter.url))
            .asObservableSuccess()
            .map { parsePages(it, chapter.url) }
    }
    private fun parsePages(response: Response, refUrl: String): List<Page>{
        val pages = mutableListOf<Page>()
        val images = mutableListOf<String>()
        val document = response.asJsoup()

        document.select("[style=display: block;] img").forEach { element ->
            images.add(element.attr("abs:src")))
        }

        val js = document.select(".zoomdesc-cont .chap-img-smlink + script").html()
        val postId = js.substringAfter("var my_repeater_field_post_id = ").substringBefore(";").trim()
        var postOffset = js.substringAfter("var my_repeater_field_offset = ").substringBefore(";").trim()
        val postNonce = js.substringAfter("var my_repeater_field_nonce = ").substringBefore(";").trim()
        var morePages = js.substringAfter("var my_repeater_more = ").substringBefore(";").trim().toBoolean()


        while (morePages){
            val url = "$baseUrl/wp-admin/admin-ajax.php".toHttpUrlOrNull()!!.newBuilder()
            val requestBody = "action=my_repeater_show_more&post_id=$postId&offset=$postOffset&nonce=$postNonce".toRequestBody(null)
            val request = POST(url.toString(), headers, requestBody)
            val document = client.newCall(request).execute().asJsoup()
            document.select("img").forEach {
                images.add(it.attr("abs:src"))
            }
            morePages = document.select("body").html().contains("\"more\":true")
            postOffset = document.select("body").html().substringAfterLast(":").substringBefore("}")
        }

        for ((pageNum, image) in images.withIndex()) {
            pages.add(Page(pageNum, image, image))
        }
        return pages
    }



    // Filters
    override fun getFilterList(): FilterList = FilterList(
        Filter.Header("NOTE: Ignored if using text search!"),
        AlbumTypeSelectFilter(),
    )
    private class AlbumTypeSelectFilter() : UriPartFilter(
        "Type",
        arrayOf(
            Pair("All", ""),
            Pair("Manga", "95"),
            Pair("Manhua", "115"),
            Pair("Manhwa", "105"),
            Pair("Vietnamese Comic", "306"),
        )
    )


    private open class UriPartFilter(displayName: String, val vals: Array<Pair<String, String>>) :
        Filter.Select<String>(displayName, vals.map { it.first }.toTypedArray()) {
        fun toUriPart() = vals[state].second
    }

    // Unused
    override fun pageListRequest(chapter: SChapter): Request = throw Exception("Not used")
    override fun pageListParse(document: Document): List<Page> = throw Exception("Not used")
    override fun latestUpdatesFromElement(element: Element) = throw Exception("Not used")
    override fun latestUpdatesNextPageSelector() = throw Exception("Not used")
    override fun latestUpdatesRequest(page: Int) = throw Exception("Not used")
    override fun latestUpdatesSelector() = throw Exception("Not used")
    override fun imageUrlParse(document: Document) = throw Exception("Not used")
}
