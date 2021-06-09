package eu.kanade.tachiyomi.extension.en.manhwa18cc

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SManga
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class Manhwa18Cc : Madara("Manhwa18.cc", "https://manhwa18.cc", "en") {

    override fun popularMangaSelector() = "div.manga-item"
    override fun searchMangaSelector() = popularMangaSelector()
    override val popularMangaUrlSelector = "div.data > h3 > a"


    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/webtoons/$page?orderby=trending")
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/webtoons/$page?orderby=latest")
    }

    open val mangaSubString = "webtoon"

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/search?q=$query&page=$page"
        return GET(url, headers)
    }

    override fun searchMangaFromElement(element: Element): SManga {
        val manga = SManga.create()

        with(element) {
            select("h3 a").first()?.let {
                manga.setUrlWithoutDomain(it.attr("abs:href"))
                manga.title = it.ownText()
            }
            select("thumb img").first()?.let {
                manga.thumbnail_url = imageFromElement(it)
            }
        }

        return manga
    }

    override fun chapterListSelector() = "li.wleft"

    override val pageListParseSelector = "div.read-content img"

    override fun pageListParse(document: Document): List<Page> {
        return document.select(pageListParseSelector).mapIndexed { index, element ->
            Page(
                index,
                document.location(),
                element?.let {
                    it.absUrl(if (it.hasAttr("data-src")) "data-src" else "src")
                }
            )
        }
    }
}
