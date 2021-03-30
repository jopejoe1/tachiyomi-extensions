package eu.kanade.tachiyomi.extension.en.digitalcomicmuseum

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.POST
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.TimeUnit

open class DigitalComicMuseum : ParsedHttpSource() {
    override val name = "Digital Comic Museum"
    override val baseUrl = "https://digitalcomicmuseum.com"
    override val lang = "en"
    override val supportsLatest = true


    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Popular

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/stats.php?ACT=topdl&start=${page - 1}00&limit=100", headers)
    }

    override fun popularMangaSelector() = "tbody .mainrow"

    override fun popularMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        manga.setUrlWithoutDomain(element.select("a").attr("abs:href").replace("https://digitalcomicmuseum.com", ""))
        manga.title = element.select("a").text()
        manga.thumbnail_url = element.select("img").attr("abs:src")
        return manga
    }

    override fun popularMangaNextPageSelector() = "a img[alt=Next]"

    // Latest

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl//stats.php?ACT=latest&start=${page - 1}00&limit=100", headers)
    }

    override fun latestUpdatesParse(response: Response) = popularMangaParse(response)

    override fun latestUpdatesSelector() = popularMangaSelector()

    override fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)

    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    // Search

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return POST(
            "$baseUrl/index.php?ACT=dosearch",
            body = FormBody.Builder()
                .add("terms", query)
                .build(),
            headers = headers
        )
    }

    override fun searchMangaSelector() = "#search-results + tbody tr"

    override fun searchMangaFromElement(element: Element): SManga {
        val manga = SManga.create()
        manga.setUrlWithoutDomain(element.select("a").attr("abs:href"))
        manga.title = element.select("a").text()
        return manga
    }

    override fun searchMangaNextPageSelector() = throw UnsupportedOperationException("Not used")

    // Details

    override fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()

        val base = document.select(".bodyline")
        manga.thumbnail_url = base.select("tableborder .mainrow img").attr("abs:src")
        manga.description = base.select("tableborder:contains(description) + table").text()
        manga.title = base.select("#catname").text()

        return manga
    }

    // Chapters

    override fun chapterListParse(response: Response) = throw UnsupportedOperationException("Not used")

    override fun chapterListSelector() = "body"

    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()

        val url = element.select("table + tbody + tr + td.bodyline div.tableorder table + tbody tr.tablefooter td + table + tbody + tr td div a")
        chapter.setUrlWithoutDomain(url.attr("abs:href"))
        chapter.name = "Issue"

        return chapter
    }

    // Pages

    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()
        var i = 1
        val url = document.select("[alt=Comic Page - ZIP]").attr("abs:src")
        val check = document.select(".fullscreen").attr("abs:src")
        val check2 = "https://cdn.digitalcomicmuseum.com/preview/images/fullscreen.png"
        while (check == check2) {
            val imgpage = "$baseUrl$url".replace("&page=2", "&page=$i")
            pages.add(Page(i, imgpage))
            i + 1
        }
        return pages
    }

    override fun imageUrlParse(document: Document): String {
        return document.select("[alt=Comic Page]").attr("abs:src")
    }
}
