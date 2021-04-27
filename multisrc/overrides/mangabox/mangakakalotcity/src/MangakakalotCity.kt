package eu.kanade.tachiyomi.extension.en.mangakakalotcity

import eu.kanade.tachiyomi.multisrc.mangabox.MangaBox
import okhttp3.Headers
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MangakakalotCity : MangaBox("Mangakakalot.city (unoriginal)", "http://mangakakalot.city/", "en") {
    override val simpleQueryPath = "search?q="
    override val popularUrlPath = "popular-manga?page="
    override val latestUrlPath = "latest-manga?page="
    override fun popularMangaNextPageSelector() = "div.phan-trang > ul.pagination > li > a[rel=\"next\"]"
    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()
    override fun searchMangaSelector() = "div.truyen-list > div.list-truyen-item-wrap"
    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    override val pageListSelector = "div.page > select#page_select > option"
    override fun pageListParse(document: Document): List<Page> {
        return document.select(pageListSelector)
            .mapIndexed { i, element ->
                val url = element.attr("abs:value")
                Page(i, document.location(), url)
            }
    }
}
