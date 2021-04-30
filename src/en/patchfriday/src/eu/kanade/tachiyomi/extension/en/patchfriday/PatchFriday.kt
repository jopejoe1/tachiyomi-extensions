package eu.kanade.tachiyomi.extension.en.patchfriday

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable

class PatchFriday : HttpSource() {

    override val name = "Patch Friday"

    override val baseUrl = "https://patchfriday.com"

    override val lang = "en"

    override val supportsLatest = false

    override val client: OkHttpClient = network.cloudflareClient

    private fun createManga(): SManga {
        return SManga.create().apply {
            initialized = true
            title = "Patch Friday"
            url = ""
            thumbnail_url = "https://patchfriday.com/patches/68.png"
            description = "The IT security webcomic"
        }
    }

    // Popular

    override fun fetchPopularManga(page: Int): Observable<MangasPage> {
        return Observable.just(MangasPage(listOf(createManga()), false))
    }

    override fun popularMangaRequest(page: Int): Request = throw UnsupportedOperationException("Not used")

    override fun popularMangaParse(response: Response): MangasPage = throw UnsupportedOperationException("Not used")

    // Latest

    override fun latestUpdatesRequest(page: Int): Request = throw UnsupportedOperationException("Not used")

    override fun latestUpdatesParse(response: Response): MangasPage = throw UnsupportedOperationException("Not used")

    // Search

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> = Observable.just(MangasPage(emptyList(), false))

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request = throw UnsupportedOperationException("Not used")

    override fun searchMangaParse(response: Response): MangasPage = throw UnsupportedOperationException("Not used")

    // Details

    override fun fetchMangaDetails(manga: SManga): Observable<SManga> {
        return Observable.just(createManga())
    }

    override fun mangaDetailsParse(response: Response): SManga = throw UnsupportedOperationException("Not used")

    // Chapters

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        return client.newCall(GET("$baseUrl/search/", headers))
            .asObservableSuccess()
            .map { parseChapters(it) }
    }

    private fun parseChapters (response: Response): List<SChapter>{
        val chapters = mutableListOf<SChapter>()
        var document = response.asJsoup()
        var page = document.select("div > div:first-of-type > div:first-of-type > a").attr("abs:href").replace("/","").trim().toInt()
        while (page > 0) {
            val element = document.select("div > div > div:first-of-type > a")
            element.forEach {
                val chapter = SChapter.create()
                chapter.url = it.attr("abs:href")
                chapter.name = "${chapter.url.replace("/", "").trim()} - ${it.text()}"
                chapter.date_upload = System.currentTimeMillis()
                chapters.add(chapter)
            }
            page -= 10
            document = client.newCall(GET("$baseUrl/search/?search=;id=$page", headers)).execute().asJsoup()
        }
        return chapters
    }

    override fun chapterListParse(response: Response): List<SChapter> = throw UnsupportedOperationException("Not used")


    // Pages

    override fun fetchPageList(chapter: SChapter): Observable<List<Page>> {
        return Observable.just(listOf(Page(0, baseUrl + chapter.url)))
    }

    override fun pageListParse(response: Response): List<Page> = throw UnsupportedOperationException("Not used")

    override fun imageUrlParse(response: Response): String {
        return response.asJsoup().select("div#strip_image img").attr("abs:src")
    }

    override fun getFilterList() = FilterList()
}
