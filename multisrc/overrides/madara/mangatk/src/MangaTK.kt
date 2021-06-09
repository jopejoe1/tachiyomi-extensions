package eu.kanade.tachiyomi.extension.en.mangatk

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SManga
import java.util.Locale
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import org.jsoup.nodes.Document

class MangaTK : Madara("MangaTK", "https://mangatk.com", "en") {

    override fun popularMangaSelector() = "div.manga-item"
    override fun searchMangaSelector() = popularMangaSelector()
    override val popularMangaUrlSelector = "div > h3 > a"

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/manga/page/$page?orderby=trending")
    }

    override fun latestUpdatesRequest(page: Int): Request {
        return GET("$baseUrl/manga/page/$page?orderby=latest")
    }

    override fun mangaDetailsParse(document: Document): SManga {
        val manga = super.mangaDetailsParse(document)

        with(document) {
            select("div.story-info-right h1").first()?.let {
                manga.title = it.ownText()
            }
            select("div.manga-info p:contains(Author) a").first()?.let {
                if (it.text().notUpdating()) manga.author = it.text()
            }
            select("div.panel-story-description div.desct").let {
                if (it.select("p").text().isNotEmpty()) {
                    manga.description = it.select("p").joinToString(separator = "\n\n") { p ->
                        p.text().replace("<br>", "\n")
                    }
                } else {
                    manga.description = it.text()
                }
            }
            select("span.info-image img").first()?.let {
                manga.thumbnail_url = imageFromElement(it)
            }
            select("div.manga-info p:contains(Status)").last()?.let {
                manga.status = when (it.text().substringAfter(": ")) {
                    // I don't know what's the corresponding for COMPLETED and LICENSED
                    // There's no support for "Canceled" or "On Hold"
                    "Completed", "Completo", "Concluído", "Concluido", "Terminé" -> SManga.COMPLETED
                    "OnGoing", "Продолжается", "Updating", "Em Lançamento", "Em andamento", "Em Andamento", "En cours", "Ativo", "Lançando" -> SManga.ONGOING
                    else -> SManga.UNKNOWN
                }
            }
            val genres = select("div.genres a")
                .map { element -> element.text().toLowerCase(Locale.ROOT) }
                .toMutableSet()
        }

        return manga
    }

    override val altNameSelector: String = "div.manga-info p:contains(Alternative)"

    override fun searchPage(page: Int): String = "search"

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/${searchPage(page)}".toHttpUrlOrNull()!!.newBuilder()
        url.addQueryParameter("s", query)
        url.addQueryParameter("page", "$page")
        url.addQueryParameter("post_type", "wp-manga")
        filters.forEach { filter ->
            when (filter) {
                is AuthorFilter -> {
                    if (filter.state.isNotBlank()) {
                        url.addQueryParameter("author", filter.state)
                    }
                }
                is ArtistFilter -> {
                    if (filter.state.isNotBlank()) {
                        url.addQueryParameter("artist", filter.state)
                    }
                }
                is YearFilter -> {
                    if (filter.state.isNotBlank()) {
                        url.addQueryParameter("release", filter.state)
                    }
                }
                is StatusFilter -> {
                    filter.state.forEach {
                        if (it.state) {
                            url.addQueryParameter("status[]", it.id)
                        }
                    }
                }
                is OrderByFilter -> {
                    if (filter.state != 0) {
                        url.addQueryParameter("m_orderby", filter.toUriPart())
                    }
                }
                is AdultContentFilter -> {
                    url.addQueryParameter("adult", filter.toUriPart())
                }
                is GenreConditionFilter -> {
                    url.addQueryParameter("op", filter.toUriPart())
                }
                is GenreList -> {
                    filter.state
                        .filter { it.state }
                        .let { list ->
                            if (list.isNotEmpty()) { list.forEach { genre -> url.addQueryParameter("genre[]", genre.id) } }
                        }
                }
            }
        }
        return GET(url.toString(), headers)
    }

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
