package eu.kanade.tachiyomi.extension.en.hentaimimi

import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import rx.Observable


/*
* To Update Filter Values Run "FilterOptGen.kt"
*
*
 */
class HentaiMimi : ParsedHttpSource() {
    // Meta Data
    override val baseUrl = "https://hentaimimi.com"
    override val lang = "en"
    override val name = "HentaiMimi"
    override val supportsLatest = true

    // Latest

    override fun latestUpdatesFromElement(element: Element): SManga {
        val manga = SManga.create()
        val title = element.select(".white-text")
        manga.url = title.attr("abs:href").replace(baseUrl, "").trim()
        manga.title = title.text()
        manga.thumbnail_url = element.select(".card-img-top").attr("abs:src")
        return manga
    }

    override fun latestUpdatesNextPageSelector(): String? = ":not(*)"

    override fun latestUpdatesRequest(page: Int): Request {
        return GET(baseUrl, headers)
    }

    override fun latestUpdatesSelector(): String = "div.row:nth-child(2) > div"

    // Popular

    override fun popularMangaFromElement(element: Element): SManga = latestUpdatesFromElement(element)

    override fun popularMangaNextPageSelector(): String? = "li.page-item.active + li.page-item"

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/index?page=$page", headers)
    }

    override fun popularMangaSelector(): String = "div.row:nth-child(4) > div"

    // Search

    override fun searchMangaFromElement(element: Element): SManga = latestUpdatesFromElement(element)

    override fun searchMangaNextPageSelector(): String? = popularMangaNextPageSelector()

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url = "$baseUrl/search".toHttpUrlOrNull()?.newBuilder()!!
        if (query.isNotEmpty()) url.addQueryParameter("title", query)
        filters.forEach { filter ->
            when (filter) {
                /*is ArtistGroupFilter -> {
                    filter.state.filter { it.state }.map { it.value }.forEach { url.addQueryParameter("artists[]", it) }
                }*/
                is ParodiesGroupFilter -> {
                    filter.state.filter { it.state }.map { it.value }.forEach { url.addQueryParameter("parodies[]", it) }
                }/*
                is LanguageGroupFilter -> {
                    filter.state.filter { it.state }.map { it.value }.forEach { url.addQueryParameter("langs[]", it) }
                }
                is PublishersGroupFilter -> {
                    filter.state.filter { it.state }.map { it.value }.forEach { url.addQueryParameter("pubs[]", it) }
                }
                is TagGroupFilter -> {
                    filter.state.filter { it.isExcluded() }.map { it.value }.forEach { url.addQueryParameter("tags_ex[]", it) }
                    filter.state.filter { it.isIncluded() }.map { it.value }.forEach { url.addQueryParameter("tags[]", it) }
                }*/
            }
        }
        url.addQueryParameter("page", page.toString())
        return GET(url.toString(), headers)
    }

    override fun searchMangaSelector(): String = "div.row:nth-child(2) > div"

    // Mangas


    override fun mangaDetailsParse(document: Document): SManga {
        val manga = SManga.create()
        val details = document.select("div.p-0")
        val tags = mutableListOf<String>()
        manga.title = details.select("h3").text()
        manga.url = document.location().replace(baseUrl, "").trim()
        manga.thumbnail_url = document.select("div.col-md-4 img").attr("abs:src")
        details.select(".mb-3").forEach {
            when (it.select(".lead").text()) {
                "Artist" -> {
                    manga.artist = it.select("p:nth-child(2)").text()
                    manga.author = manga.artist
                }
                "Description" -> manga.description = it.select("p:nth-child(2)").text()
                "Tags" -> it.select("p:nth-child(2) > a > span").forEach { tag ->
                    tags.add(tag.text())
                }
                "Language" -> tags.add(it.select("p:nth-child(2)").text())
                "Magazine" -> tags.add(it.select("p:nth-child(2)").text())
                "Publisher" -> tags.add(it.select("p:nth-child(2)").text())
            }
        }
        manga.status = 2
        manga.genre = tags.joinToString(", ")
        return manga
    }

    // Chapters

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        return Observable.just(
            listOf(
                SChapter.create().apply {
                    name = "Chapter"
                    url = manga.url
                    chapter_number = 1F
                    date_upload = System.currentTimeMillis()
                }
            )
        )
    }
    override fun chapterFromElement(element: Element): SChapter = throw UnsupportedOperationException("Not used")

    override fun chapterListSelector(): String = throw UnsupportedOperationException("Not used")

    override fun imageUrlParse(document: Document): String = throw UnsupportedOperationException("Not used")

    override fun pageListParse(document: Document): List<Page> {
        val pages = mutableListOf<Page>()
        document.select("body main script").html().substringAfter("[").substringBefore("]").split(",").forEachIndexed { index, it ->
            val url = "$baseUrl/${it.replace("\\", "").replace("\"", "")}"
            pages.add(Page(index, url, url))
        }

        /*document.select("div#lightgallery > a").forEachIndexed() { index, it ->
            val url = it.select("img").attr("abs:src")
            pages.add(Page(index, url, url))
        }*/
        return pages
    }

    // Filters

    class TriStateFilterOption(name: String, val value: String) : Filter.TriState(name)
    class CheckboxFilterOption(name: String, val value: String, default: Boolean = false) : Filter.CheckBox(name, default)

    private class TagGroupFilter(filters: List<TriStateFilterOption>) : Filter.Group<TriStateFilterOption>("Tags", filters)
    private class LanguageGroupFilter(options: List<CheckboxFilterOption>) : Filter.Group<CheckboxFilterOption>("Languages", options)
    private class ArtistGroupFilter(options: List<CheckboxFilterOption>) : Filter.Group<CheckboxFilterOption>("Artist", options)
    private class ParodiesGroupFilter(options: List<CheckboxFilterOption>) : Filter.Group<CheckboxFilterOption>("Parodies", options)
    private class PublishersGroupFilter(options: List<CheckboxFilterOption>) : Filter.Group<CheckboxFilterOption>("Publishers", options)

    override fun getFilterList(): FilterList = FilterList(
        //ArtistGroupFilter(artists),
        ParodiesGroupFilter(parodies2()),
        //LanguageGroupFilter(langs),
        //PublishersGroupFilter(pubs),
        //TagGroupFilter(tags),
    )
    private fun parodies2() = listOf(
        CheckboxFilterOption("original work","2"),
        CheckboxFilterOption("haramase! honoo no oppai isekai ero mahou gakuen!","3"),
        CheckboxFilterOption("my little pony","4"),
        CheckboxFilterOption("toaru majutsu no index","5"),
        CheckboxFilterOption("gintama","6"),
        CheckboxFilterOption("girls und panzer","7"),
        CheckboxFilterOption("fate series","8"),
        CheckboxFilterOption("azur lane","9"),
        CheckboxFilterOption("kantai collection","10"),
        CheckboxFilterOption("precure","11"),
        CheckboxFilterOption("kagai jugyou","12"),
        CheckboxFilterOption("dragon's crown","13"),
        CheckboxFilterOption("the ryuo's work is never done!","14"),
        CheckboxFilterOption("granblue fantasy","15"),
        CheckboxFilterOption("sword art online","16"),
        CheckboxFilterOption("the irregular at magic high school","17"),
        CheckboxFilterOption("delicious in dungeon","18"),
        CheckboxFilterOption("persona 5","19"),
        CheckboxFilterOption("fire emblem fates","20"),
        CheckboxFilterOption("touhou","21"),
        CheckboxFilterOption("a kiss for the petals","22"),
        CheckboxFilterOption("rwby","23"),
        CheckboxFilterOption("utawarerumono","24"),
        CheckboxFilterOption("the [email protected] cinderella girls","25"),
        CheckboxFilterOption("to love-ru","26"),
        CheckboxFilterOption("love live","27"),
        CheckboxFilterOption("my hero academia","28"),
        CheckboxFilterOption("hundred","29"),
        CheckboxFilterOption("cardfight!! vanguard","30"),
        CheckboxFilterOption("amagi brilliant park","31"),
        CheckboxFilterOption("schoolgirl strikers","32"),
        CheckboxFilterOption("fantasy earth zero","33"),
        CheckboxFilterOption("puyo puyo","34"),
        CheckboxFilterOption("vocaloid, dragon quest, voiceroid","35"),
        CheckboxFilterOption("voiceroid","36"),
        CheckboxFilterOption("god eater 2","37"),
        CheckboxFilterOption("puzzle & dragons","38"),
        CheckboxFilterOption("zelda","39"),
        CheckboxFilterOption("nier: automata","40"),
        CheckboxFilterOption("super mario odyssey","41"),
        CheckboxFilterOption("street fighter","42"),
        CheckboxFilterOption("wendy","43"),
        CheckboxFilterOption("go! princess precure","44"),
        CheckboxFilterOption("konosuba: god's blessing on this wonderful world!","45"),
        CheckboxFilterOption("food wars! shokugeki no soma","46"),
        CheckboxFilterOption("gambling emperor legend zero","47"),
        CheckboxFilterOption("the human reignition project","48"),
        CheckboxFilterOption("vocaloid, voiceroid","49"),
        CheckboxFilterOption("ane doki","50"),
        CheckboxFilterOption("kantai collection, hyperdimension neptunia","51"),
        CheckboxFilterOption("muv-luv","52"),
        CheckboxFilterOption("millennium war aigis","53"),
        CheckboxFilterOption("crawling dreams","54"),
        CheckboxFilterOption("dragon quest","55"),
        CheckboxFilterOption("dagashi kashi","56"),
        CheckboxFilterOption("watamote","57"),
        CheckboxFilterOption("last period","58"),
        CheckboxFilterOption("doraemon","59"),
        CheckboxFilterOption("puella magi madoka magica","60"),
        CheckboxFilterOption("gate","61"),
        CheckboxFilterOption("darling in the franxx","62"),
        CheckboxFilterOption("league of legends","63"),
        CheckboxFilterOption("pokemon go","64"),
        CheckboxFilterOption("steven universe","65"),
        CheckboxFilterOption("kill la kill","66"),
        CheckboxFilterOption("is it wrong to try to pick up girls in a dungeon?","67"),
        CheckboxFilterOption("dark souls","68"),
        CheckboxFilterOption("minecraft","69"),
    )
}
