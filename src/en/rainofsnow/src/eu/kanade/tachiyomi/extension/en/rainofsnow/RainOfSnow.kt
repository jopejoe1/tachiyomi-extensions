package eu.kanade.tachiyomi.extension.en.rainofsnow

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.HttpUrl
import okhttp3.RequestBody
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

open class RainOfSnow() : ParsedHttpSource() {

    override val name = "Rain Of Snow"

    override val baseUrl = "https://rainofsnow.com"

    override val lang = "en"

    override val supportsLatest = true

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
        val albumTypeFilter = filters.findInstance<AlbumTypeSelectFilter>()!!
        return GET(when{
            query != "" -> {
                HttpUrl.parse("$baseUrl/")!!.newBuilder()
                    .addQueryParameter("s", query)
            }
            else -> {
                HttpUrl.parse("$baseUrl/comics/page/$page")!!.newBuilder()
                    .addQueryParameter("n_orderby", albumTypeFilter.toString())
            }

        }.build().toString(), headers)
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
        val id = chapter.url.substringAfterLast("_").removeSuffix("/")

        return client.newCall(GET(chapter.url))
            .asObservableSuccess()
            .map { parsePages(it, chapter.url) }
    }
    private fun parsePages(response: Response, refUrl: String): List<Page>{
        val pages = mutableListOf<Page>()
        val document = response.asJsoup()
        var postUrl = "$baseUrl/wp-admin/admin-ajax.php"
        var morePages = false
        var postNonce = ""
        var postOffset =""
        var postId = ""
        var pageNum = 0

        document.select("[style=display: block;] img").forEachIndexed { index, element ->
            pages.add(Page(pageNum, "", element.attr("abs:src")))
            pageNum++
        }
        val js = document.select(".zoomdesc-cont .chap-img-smlink + script").text()
        for (s in js.split(";")) {
            when{
                s.contains("var my_repeater_field_post_id =") -> {
                    postId = s.substringAfter("=").substringBefore(";").trim()
                }
                s.contains("var my_repeater_field_offset =") -> {
                    postOffset = s.substringAfter("=").substringBefore(";").trim()
                }
                s.contains("var my_repeater_field_nonce =") -> {
                    postNonce = s.substringAfter("=").substringBefore(";").trim()
                }
                s.contains("var my_repeater_more =") -> {
                    morePages = s.substringAfter("=").substringBefore(";").trim().toBoolean()
                }
            }
        }

        while (morePages){
            var content = ""
            val postBody = RequestBody.create(null, "action=my_repeater_show_more&post_id=$postId&offset=$postOffset&nonce=$postNonce")
            val request = POST("$postUrl", headers, postBody)
            val document = client.newCall(request).execute()
            val data = document.body()!!.string()
            for (s in data.split(",")) {
                when{
                    s.contains("\"content\":") -> {
                        content = s.substringAfter("\":\"").substringBeforeLast("\"").replace("\\t","").replace("\\n","").replace("\\","")
                        content = content.replace("\" /><img class=\"img-responsive comicimgcls\" src=\"",",").replace("<img class=\"img-responsive comicimgcls\" src=\"","").replace("\" />","").trim()
                    }
                    s.contains("\"more\":") -> {
                        morePages = s.substringAfter("\":").trim().toBoolean()
                    }
                    s.contains("\"offset\":") -> {
                        postOffset = s.substringAfter("\":").trim()
                    }
                }
            }
            for (s in content.split(",")){
                pages.add(Page(pageNum, "", s))
                pageNum++
            }
        }
        return pages

    }

    override fun pageListParse(document: Document): List<Page> = throw Exception("Not used")
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)
    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()
    override fun latestUpdatesRequest(page: Int) = popularMangaRequest(page)
    override fun latestUpdatesSelector() = popularMangaSelector()
    override fun imageUrlParse(document: Document) = throw Exception("Not used")

    // Filters
    abstract class SelectFilter(name: String, private val options: List<SelectFilterOption>, default: Int = 0) : Filter.Select<String>(name, options.map { it.name }.toTypedArray(), default) {
        val selected: String
            get() = options[state].value

        override fun toString(): String = selected
    }

    class SelectFilterOption(val name: String, val value: String)

    class AlbumTypeSelectFilter(options: List<SelectFilterOption>) : SelectFilter("Album Type", options)

    fun getAlbumTypeFilters() = listOf(
        SelectFilterOption("", "All"),
        SelectFilterOption("95", "Manga"),
        SelectFilterOption("115", "Manhua"),
        SelectFilterOption("105", "Manhwa"),
        SelectFilterOption("306", "Vietnamese Comic"),
    )
    override fun getFilterList(): FilterList = FilterList(
        AlbumTypeSelectFilter(getAlbumTypeFilters()),
    )

    private inline fun <reified T> Iterable<*>.findInstance() = find { it is T } as? T
}
