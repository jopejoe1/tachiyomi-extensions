package eu.kanade.tachiyomi.extension.en.mangakakalotcity

import eu.kanade.tachiyomi.multisrc.mangabox.MangaBox
import okhttp3.Headers

class MangakakalotCity : MangaBox("Mangakakalot.city (unoriginal)", "http://mangakakalot.city/", "en") {
    override val simpleQueryPath = "search?q="
    override fun popularUrlPath = "popular-manga?page="
    override fun latestUrlPath = "latest-manga?page="
}
