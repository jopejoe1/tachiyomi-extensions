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
        return GET("$baseUrl/stats.php?ACT=latest&start=${page - 1}00&limit=100", headers)
    }

    override fun latestUpdatesParse(response: Response) = popularMangaParse(response)

    override fun latestUpdatesSelector() = popularMangaSelector()

    override fun latestUpdatesFromElement(element: Element): SManga = popularMangaFromElement(element)

    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    // Search

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return POST(
            "$baseUrl/index.php",
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
        manga.thumbnail_url = base.select("div.tableborder:first-of-type > div#banner + table  tbody > .tableheader + .mainrow img").attr("abs:src")
        manga.description = base.select("div.tableborder:nth-of-type(2) > div#banner:first-child + table > tbody > tr > [colspan=\"3\"] > [cellpadding=\"3\"]  > tbody > tr.mainrow > td").text()
        manga.title = base.select(".tableborder:first-of-type > #banner > #catname > a").text()

        return manga
    }

    // Chapters

    override fun chapterListParse(response: Response) = throw UnsupportedOperationException("Not used")

    override fun chapterListSelector() = "div.tableborder:first-of-type > div#banner + table .tablefooter > td > table > tbody > tr > td:nth-of-type(3) div"

    override fun chapterFromElement(element: Element): SChapter {
        val chapter = SChapter.create()
        chapter.url = baseUrl + element.select("a").attr("abs:href")
        chapter.name = "N/A"
        chapter.date_upload = System.currentTimeMillis()
        return chapter
    }

    // Pages

    override fun pageListParse(document: Document): List<Page> = mutableListOf<Page>().apply {
        var npages = document.select("#container div[align=\"center\"]").text().substringAfter("of ").trim().split(" ")
        var pagen = npages.first().toInt()
        var i = 1
        while (i <= pagen){
            var url = document.select("body > .navbar:first-of-type a#showdiv").attr("abs:href").replace("#", "&page=$i")
            add(Page(i, url))
        }
    }

    override fun imageUrlParse(document: Document): String {
        return document.select("[alt=\"Comic Page\"]").attr("abs:src")
    }
}
