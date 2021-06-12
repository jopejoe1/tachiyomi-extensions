package eu.kanade.tachiyomi.extension.meta.mangaupdates

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import eu.kanade.tachiyomi.util.asJsoup
import java.text.SimpleDateFormat
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable

class MangaUpdates() : ParsedHttpSource() {
    override val baseUrl = "https://www.mangaupdates.com"
    override val lang = "meta"
    override val name = "Baka-Updates Manga"
    override val supportsLatest = true

    // Popular
    override fun popularMangaRequest(page: Int): Request {
        val url = "$baseUrl/series.html".toHttpUrlOrNull()!!.newBuilder()
        url.addQueryParameter("page", "$page")
        url.addQueryParameter("orderby", "rating")
        return GET(url.toString(), headers)
    }

    override fun popularMangaNextPageSelector() = "div.specialtext > a:contains(Next Page)"

    override fun popularMangaSelector() = "div.col-lg-6"

    override fun popularMangaFromElement(element: Element): SManga {
        return SManga.create().apply {
            thumbnail_url = element.select(".series_thumb img").attr("abs:src")
            setUrlWithoutDomain(element.select(".series_thumb a").attr("abs:href"))
            title = element.select(".text").first().text()
        }
    }

    // Latest
    override fun latestUpdatesFromElement(element: Element) = popularMangaFromElement(element)

    override fun latestUpdatesNextPageSelector() = popularMangaNextPageSelector()

    override fun latestUpdatesSelector() = popularMangaSelector()

    override fun latestUpdatesRequest(page: Int): Request {
        val url = "$baseUrl/series.html".toHttpUrlOrNull()!!.newBuilder()
        url.addQueryParameter("page", "$page")
        url.addQueryParameter("orderby", "year")
        return GET(url.toString(), headers)
    }

    // Search

    override fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> {
        if (query.startsWith("ID:")){
            val manga = SManga.create().apply { url = "$baseUrl/series.html?id=${query.substringAfter(":")}" }
            return Observable.just(MangasPage(listOf(manga),false))
        }
        return super.fetchSearchManga(page, query, filters)
    }

    override fun searchMangaFromElement(element: Element) = popularMangaFromElement(element)

    override fun searchMangaNextPageSelector() = popularMangaNextPageSelector()

    override fun searchMangaSelector() = popularMangaSelector()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val searchFilter = filters.findInstance<SearchFilter>()!!
        val sortFilter = filters.findInstance<SortFilter>()!!
        val extendedFilter = filters.findInstance<ExtendedFilter>()!!
        val licenseFilter = filters.findInstance<LicenseFilter>()!!
        val typeFilter = filters.findInstance<TypeFilter>()!!
        val genreFilter = filters.findInstance<GenreGroupFilter>()!!
        val startFilter = filters.findInstance<StartsTextFilter>()!!

        val url = "$baseUrl/series.html".toHttpUrlOrNull()!!.newBuilder()

        url.addQueryParameter("page", "$page")
        url.addQueryParameter("search", "$query")
        url.addQueryParameter("stype", searchFilter.selected)
        url.addQueryParameter("orderby", sortFilter.selected)

        if (startFilter.state.isNotBlank()){
            url.addQueryParameter("letter", startFilter.state)
        }
        if (extendedFilter.selected.isNotBlank()) {
            url.addQueryParameter("filter", extendedFilter.selected)
        }
        if (licenseFilter.selected.isNotBlank()) {
            url.addQueryParameter("licensed", licenseFilter.selected)
        }
        if (typeFilter.selected.isNotBlank()) {
            url.addQueryParameter("type", typeFilter.selected)
        }
        if (genreFilter.included.isNotEmpty()) {
            url.addQueryParameter("genre", genreFilter.included.joinToString("_"))
        }
        if (genreFilter.excluded.isNotEmpty()) {
            url.addQueryParameter("exclude_genre", genreFilter.excluded.joinToString("_"))
        }

        url.addQueryParameter("perpage", "50")

        return GET(url.toString(), headers)
    }

    // Details
    override fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()
        manga.title = document.select("span.releasestitle").text()
        manga.description = document.select("div#div_desc_more").text()
        if (!manga.description.isNullOrBlank()){
            manga.description = document.select("div.sCat:contains(Description) + div").text()
        }
        manga.author = document.select("div.sCat:contains(Author) + div").text()
        manga.artist = document.select("div.sCat:contains(Artist) + div").text()
        manga.status = when {
            document.select("div.sCat:contains(Licensed) + div").text() == "Yes" -> 3
            document.select("div.sCat:contains(Scanlated) + div").text() == "Yes" -> 2
            document.select("div.sCat:contains(Scanlated) + div").text() == "No" -> 1
            document.select("div.sCat:contains(Status in Country of Origin) + div").text().contains("Complete") -> 2
            document.select("div.sCat:contains(Status in Country of Origin) + div").text().contains("Ongoing") -> 1
            else -> 0
        }
        manga.thumbnail_url = document.select(".sContent .img-fluid").attr("abs:src")
        val related = document.select("div.sCat:contains(Related Series) + div").text()
        val altname = document.select("div.sCat:contains(Associated Names) + div").text()
        if (related != "N/A") {
            manga.description = manga.description + "\n\nRelated: $related"
        }
        if (altname != "N/A") {
            manga.description = manga.description + "\n\nAlternative Names: $altname"
        }
        val genres = mutableListOf<String>()
        genres.add(document.select("div.sCat:contains(Type) + div").text())
        document.select("div.sCat:contains(Genre) + div a > u").forEach {
            genres.add(it.text())
        }
        document.select(".tags ul li").forEach {
            genres.add(it.text())
        }
        manga.genre = genres.joinToString(", ")
        return manga
    }

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        val id = manga.url.substringAfter("?id=")
        return client.newCall(chapterListRequest(id, 1)).asObservableSuccess().map { chapterListParse(id, it) }
    }

    private fun chapterListParse(id: String, response: Response): List<SChapter> {
        val chapters = mutableListOf<SChapter>()

        val dates = mutableListOf<String>()
        val chapternums = mutableListOf<String>()
        val volume = mutableListOf<String>()
        val scanlator = mutableListOf<String>()

        var document = response.asJsoup()
        var page = 1
        var nextPage = true

        while (nextPage) {
            page++

            document.select("div.row:has(div.releasestitle) > div.text.col-2").forEach { dates.add(it.text()) }
            document.select("div.row:has(div.releasestitle) > div.text.col-1 + div.text.col-4").forEach { scanlator.add(it.text()) }
            document.select("div.row:has(div.releasestitle) > div.text.col-1 + div.text.col-1").forEach { chapternums.add(it.text()) }
            document.select("div.row:has(div.releasestitle) > div.text.col-2 + div.text.col-1").forEach { volume.add(it.text()) }

            nextPage = !popularMangaNextPageSelector().isNullOrBlank()
            if (nextPage) {
                document = client.newCall(chapterListRequest(id, page)).execute().asJsoup()
            }
        }
        chapternums.forEachIndexed{index, number ->
            val chapter = SChapter.create()
            chapter.scanlator = scanlator[index]
            chapter.date_upload = SimpleDateFormat("MM/dd/yy").parse(dates[index]).time
            chapter.chapter_number = chapternums[index].substringBefore("-").toFloat()
            chapter.name = "${if (volume[index].isNotBlank()){ "Volume: ${volume[index]}" }else{""} } Chapter: ${chapternums[index]}"
            chapters.add(chapter)
        }
        return chapters
    }

    private fun chapterListRequest(id: String, page: Int): Request{
        val url = "$baseUrl/releases.html".toHttpUrlOrNull()!!.newBuilder()
        url.addQueryParameter("page", "$page")
        url.addQueryParameter("stype", "series")
        url.addQueryParameter("search", id)
        return GET(url.toString(), headers)
    }

    // Filters
    class SelectFilterOption(val name: String, val value: String)
    class TriStateFilterOption(name: String, val value: String = name,  default: Int = 0) : Filter.TriState(name, default)

    abstract class TextFilter(name: String) : Filter.Text(name)
    abstract class SelectFilter(name: String, private val options: List<SelectFilterOption>, default: Int = 0) : Filter.Select<String>(name, options.map { it.name }.toTypedArray(), default) {
        val selected: String
            get() = options[state].value
    }

    abstract class TriStateGroupFilter(name: String, options: List<TriStateFilterOption>) : Filter.Group<TriStateFilterOption>(name, options) {
        val included: List<String>
            get() = state.filter { it.isIncluded() }.map { it.value }

        val excluded: List<String>
            get() = state.filter { it.isExcluded() }.map { it.value }
    }

    class SearchFilter(options: List<SelectFilterOption>, default: Int) : SelectFilter("Search Type", options, default)
    class SortFilter(options: List<SelectFilterOption>, default: Int) : SelectFilter("Sort By", options, default)
    class LicenseFilter(options: List<SelectFilterOption>, default: Int) : SelectFilter("License", options, default)
    class ExtendedFilter(options: List<SelectFilterOption>, default: Int) : SelectFilter("Extended", options, default)
    class TypeFilter(options: List<SelectFilterOption>, default: Int) : SelectFilter("Type", options, default)
    class GenreGroupFilter(options: List<TriStateFilterOption>) : TriStateGroupFilter("Genre", options)
    class StartsTextFilter : TextFilter("Starts With")

    override fun getFilterList(): FilterList {
        return FilterList(
            SearchFilter(getSearchilter(), 0),
            SortFilter(getSortFilter(), 2),
            LicenseFilter(getLicenseFilter(), 0),
            ExtendedFilter(getExtendedFilter(), 0),
            TypeFilter(getTypeFilter(), 0),
            GenreGroupFilter(getGenreFilter()),
            StartsTextFilter(),
        )
    }

    private fun getSearchilter() = listOf(
        SelectFilterOption("Title", "title"),
        SelectFilterOption("Description", "description"),
    )

    private fun getSortFilter() = listOf(
        SelectFilterOption("Title", "title"),
        SelectFilterOption("Year", "year"),
        SelectFilterOption("Rating", "rating"),
    )

    private fun getGenreFilter() = listOf(
        TriStateFilterOption("Action"),
        TriStateFilterOption("Adult"),
        TriStateFilterOption("Adventure"),
        TriStateFilterOption("Comedy"),
        TriStateFilterOption("Doujinshi"),
        TriStateFilterOption("Drama"),
        TriStateFilterOption("Ecchi"),
        TriStateFilterOption("Fantasy"),
        TriStateFilterOption("Gender Bender", "Gender+Bender"),
        TriStateFilterOption("Harem"),
        TriStateFilterOption("Hentai"),
        TriStateFilterOption("Historical"),
        TriStateFilterOption("Horror"),
        TriStateFilterOption("Josei"),
        TriStateFilterOption("Lolicon"),
        TriStateFilterOption("Martial Arts", "Martial+Arts"),
        TriStateFilterOption("Mature"),
        TriStateFilterOption("Mecha"),
        TriStateFilterOption("Mystery"),
        TriStateFilterOption("Psychological"),
        TriStateFilterOption("Romance"),
        TriStateFilterOption("School Life", "School+Life"),
        TriStateFilterOption("Sci-fi"),
        TriStateFilterOption("Seinen"),
        TriStateFilterOption("Shotacon"),
        TriStateFilterOption("Shoujo"),
        TriStateFilterOption("Shoujo Ai", "Shoujo+Ai"),
        TriStateFilterOption("Shounen"),
        TriStateFilterOption("Shounen Ai", "Shounen+Ai"),
        TriStateFilterOption("Slice of Life", "Slice+of+Life"),
        TriStateFilterOption("Smut"),
        TriStateFilterOption("Sports"),
        TriStateFilterOption("Supernatural"),
        TriStateFilterOption("Tragedy"),
        TriStateFilterOption("Yaoi"),
        TriStateFilterOption("Yuri"),
    )

    private fun getLicenseFilter() = listOf(
        SelectFilterOption("Show all licensed/unlicensed manga", ""),
        SelectFilterOption("Show only licensed manga", "yes"),
        SelectFilterOption("Show only unlicensed manga", "no"),
    )

    private fun getExtendedFilter() = listOf(
        SelectFilterOption("Show all manga", ""),
        SelectFilterOption("Only show completely scanlated manga", "scanlated"),
        SelectFilterOption("Only show completed (including oneshots) manga", "completed"),
        SelectFilterOption("Only show one shots", "oneshots"),
        SelectFilterOption("Exclude one shots", "no_oneshots"),
        SelectFilterOption("Only show manga with at least one release", "some_releases"),
        SelectFilterOption("Only show manga with no releases", "no_releases"),
    )

    private fun getTypeFilter() = listOf(
        SelectFilterOption("Show All", ""),
        SelectFilterOption("Artbook", "artbook"),
        SelectFilterOption("Doujinshi", "doujinshi"),
        SelectFilterOption("Drama CD", "drama_cd"),
        SelectFilterOption("Filipino", "filipino"),
        SelectFilterOption("Indonesian", "indonesian"),
        SelectFilterOption("Manga", "manga"),
        SelectFilterOption("Manhwa", "manhwa"),
        SelectFilterOption("Manhua", "manhua"),
        SelectFilterOption("Novel", "novel"),
        SelectFilterOption("OEL", "oel"),
        SelectFilterOption("Thai", "thai"),
        SelectFilterOption("Vietnamese", "vietnamese"),
        SelectFilterOption("Malaysian", "malaysian"),
        SelectFilterOption("Nordic", "nordic"),
        SelectFilterOption("French", "french"),
        SelectFilterOption("Spanish", "spanish"),
    )

    private inline fun <reified T> Iterable<*>.findInstance() = find { it is T } as? T

    // Not Used

    override fun chapterListRequest(manga: SManga): Request = throw UnsupportedOperationException("Not used")
    override fun chapterFromElement(element: Element): SChapter = throw UnsupportedOperationException("Not used")
    override fun chapterListSelector(): String = throw UnsupportedOperationException("Not used")
    override fun imageUrlParse(document: Document): String = throw UnsupportedOperationException("Not used")
    override fun pageListParse(document: Document): List<Page> = throw UnsupportedOperationException("Not used")




}
