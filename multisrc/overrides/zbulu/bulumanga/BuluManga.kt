package eu.kanade.tachiyomi.extension.en.bulumanga

import eu.kanade.tachiyomi.multisrc.mangabox.MangaBox
import eu.kanade.tachiyomi.network.GET
import okhttp3.Request
import eu.kanade.tachiyomi.source.model.Page
import org.jsoup.nodes.Document


class BuluManga : Zbulu("Bulu Manga", "https://ww8.bulumanga.net", "en") {
    override fun pageListParse(document: Document): List<Page> {
        return document.select("div.chapter-content img").mapIndexed { i, img ->
            Page(i, "", img.attr("abs:src").substringAfter("url="))
        }
    }
}
