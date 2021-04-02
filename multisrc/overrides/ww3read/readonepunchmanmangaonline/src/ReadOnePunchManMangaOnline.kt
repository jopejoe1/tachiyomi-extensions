package eu.kanade.tachiyomi.extension.en.readonepunchmanmangaonline

import eu.kanade.tachiyomi.multisrc.ww3read.Ww3Read
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.util.asJsoup
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Request
import rx.Observable
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class ReadOnePunchManMangaOnline : Ww3Read("Read One Punch Man Manga Online", "https://ww3.readopm.com", "en") {
    override val sourceList = listOf(
        Pair("One Punch Man", "$baseUrl/manga/one-punch-man/"),
        Pair("Onepunch-Man (ONE)", "$baseUrl/manga/onepunch-man-one/"),
        Pair("Colored", "$baseUrl/manga/one-punch-man-colored/"),
        Pair("Mob Psycho 100", "$baseUrl/manga/mob-psycho-100/"),
        Pair("Reigen", "$baseUrl/manga/reigen/"),
        Pair("Eyeshield 21", "$baseUrl/manga/eyeshield-21/"),
        Pair("Colored", "$baseUrl/manga/boku-no-hero-academia-colored/"),
        Pair("Oumagadoki Zoo", "$baseUrl/manga/oumagadoki-zoo/"),
        Pair("Sensei no Bulge", "$baseUrl/manga/sensei-no-bulge/")
    ).sortedBy { it.first }.distinctBy { it.second }

    override fun mangaDetailsParse(document: Document): SManga = SManga.create().apply {
        description = document.select("div.card-body > p").text()
        title = document.select("h2 > span").text().substringAfter("Manga: ").trim()
        thumbnail_url = document.select(".card-img-right").attr("src")
    }
    override fun chapterListSelector(): String = "tbody > tr"
    override fun chapterFromElement(element: Element): SChapter = SChapter.create().apply {
        name = element.select("td:first-child").text()
        url = element.select("a").attr("abs:href")
        date_upload = System.currentTimeMillis() //I have no idear how to parse Date stuff
    }
}
