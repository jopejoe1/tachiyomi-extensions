package eu.kanade.tachiyomi.extension.en.readfullcomic

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable

open class ReadFullComic(): ParsedHttpSource() {
    override val baseUrl = "https://readfullcomic.com"
    val apiUrl = "https://api.readfullcomic.com/ajax/comic"
    override val lang= "en"
    override val name= "Read Full Comic"
    override val supportsLatest = false

    // Popular

    override fun popularMangaFromElement(element: Element): SManga {
        return SManga.create().apply {
            title = element.select("a").text()
            url = "$baseUrl/" + element.select("a").attr("abs:href")
        }
    }

    override fun popularMangaNextPageSelector(): String? = "ul"

    override fun popularMangaRequest(page: Int): Request {
        val validChars = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ{".toCharArray()
        val pageChar = validChars[page -1]
        return GET("$apiUrl/comic_az_ajax_load/kval/$pageChar/")
    }

    override fun popularMangaSelector(): String = "li"


    // Search

    override fun searchMangaFromElement(element: Element): SManga = popularMangaFromElement(element)

    override fun searchMangaNextPageSelector(): String? = ":not(*)"

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        return GET("$apiUrl/comic_search_ajax_load/kval/$query/")
    }

    override fun searchMangaSelector(): String = popularMangaSelector()

    // Manga

    override fun fetchMangaDetails(manga: SManga): Observable<SManga> {
        return client.newCall(GET(manga.url, headers))
            .asObservableSuccess()
            .map { getMangaDetails(it, manga.url) }
    }

    private fun getMangaDetails(response: Response, mangaUrl: String): SManga{
        val html = response.asJsoup()
        val id = html.select("#fajax_comic_view > input[name=\"kval\"]").attr("abs:value")
        val mangaName = html.select(".title-list > h2 > a").text()
        val document = client.newCall(GET("$apiUrl/comic_cat_ajax_load/kval/$id/", headers)).execute().asJsoup()
        return SManga.create().apply {
            url = mangaUrl
            title = mangaName
            thumbnail_url = document.select("img").first().attr("abs:src")
            genre = id
        }
    }



    // Chapter

    override fun chapterListRequest(manga: SManga): Request {
        return GET("https://api.readfullcomic.com/ajax/comic/comic_cat_ajax_load/kval/${manga.genre}/")
    }

    override fun chapterFromElement(element: Element): SChapter {
        return SChapter.create().apply {
            name = element.select("h3").text()
            url = "$baseUrl/" + element.select("a").attr("abs:href")
            date_upload - System.currentTimeMillis()
        }
    }

    override fun chapterListSelector(): String = "div.colxs-4"

    // Page

    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()
        document.select(".chapter-c > img").forEachIndexed() { index, it ->
            val image = it.attr("abs:src")
            pages.add(Page(index, image, image))
        }
        return pages
    }

    // Unused
    override fun mangaDetailsParse(document: Document): SManga = throw UnsupportedOperationException("Not used")
    override fun imageUrlParse(document: Document): String = throw UnsupportedOperationException("Not used")
    override fun latestUpdatesFromElement(element: Element): SManga = throw UnsupportedOperationException("Not supported")
    override fun latestUpdatesNextPageSelector(): String? = throw UnsupportedOperationException("Not supported")
    override fun latestUpdatesRequest(page: Int): Request = throw UnsupportedOperationException("Not supported")
    override fun latestUpdatesSelector(): String = throw UnsupportedOperationException("Not supported")
}
