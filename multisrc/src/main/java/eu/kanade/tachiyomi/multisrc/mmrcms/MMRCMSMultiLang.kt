package eu.kanade.tachiyomi.multisrc.mmrcms

import android.annotation.SuppressLint
import android.net.Uri
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.Filter
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
import org.jsoup.nodes.Element
import rx.Observable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.github.salomonbrys.kotson.array
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import android.util.Base64
import java.net.URLDecoder

abstract class MMRCMSMultiLang(
    override val name: String,
    override val baseUrl: String,
    override val lang: String,
    private val jsonInfo: String = MMRCMSJsonGenHS(name, baseUrl, lang).generateJson(),
    ) : HttpSource() {
    /**
     * Parse a List of JSON sources into a list of `MyMangaReaderCMSSource`s
     *
     * Example JSON :
     * ```
     *     {
     *         "language": "en",
     *         "name": "Example manga reader",
     *         "base_url": "https://example.com",
     *         "supports_latest": true,
     *         "item_url": "https://example.com/manga/",
     *         "categories": [
     *             {"id": "stuff", "name": "Stuff"},
     *             {"id": "test", "name": "Test"}
     *         ],
     *         "tags": [
     *             {"id": "action", "name": "Action"},
     *             {"id": "adventure", "name": "Adventure"}
     *         ]
     *     }
     *
     *
     * Sources that do not supports tags may use `null` instead of a list of json objects
     *
     * @param sourceString The List of JSON strings 1 entry = one source
     * @return The list of parsed sources
     *
     * isNSFW, language, name and base_url are no longer needed as that is handled by multisrc
     * supports_latest, item_url, categories and tags are still needed
     *
     *
     */
    val parser = JsonParser()
    val jsonObject = parser.parse(jsonInfo) as JsonObject
    override val supportsLatest = jsonObject["supports_latest"].bool
    val itemUrl = jsonObject["item_url"].string

    val categoriesMappings =  if (jsonObject["categories"].isJsonArray) {
        mapToPairs(jsonObject["categories"].asJsonArray)
    } else {
        emptyList<CheckBoxs>()
    }

    val languagesMappings =  if (jsonObject["languages"].isJsonArray) {
        mapToPairs(jsonObject["languages"].asJsonArray)
    } else {
        emptyList<CheckBoxs>()
    }

    val statusMappings =  if (jsonObject["status"].isJsonArray) {
        mapToPairs(jsonObject["status"].asJsonArray)
    } else {
        emptyList<CheckBoxs>()
    }

    val charactersMappings =  if (jsonObject["characters"].isJsonArray) {
        mapToPairs(jsonObject["characters"].asJsonArray)
    } else {
        emptyList<CheckBoxs>()
    }

    val parodiesMappings =  if (jsonObject["parodies"].isJsonArray) {
        mapToPairs(jsonObject["parodies"].asJsonArray)
    } else {
        emptyList<CheckBoxs>()
    }

    var tagsMappings =  if (jsonObject["tags"].isJsonArray) {
        mapToPairs(jsonObject["tags"].asJsonArray)
    } else {
        emptyList<CheckBoxs>()
    }

    /**
     * Map an array of JSON objects to pairs. Each JSON object must have
     * the following properties:
     *
     * id: first item in pair
     * name: second item in pair
     *
     * @param array The array to process
     * @return The new list of pairs
     */
    fun mapToPairs(array: JsonArray): List<CheckBoxs> = array.map {
        it as JsonObject

        CheckBoxs(it["id"].string, it["name"].string)
    }

    private val jsonParser = JsonParser()
    private val itemUrlPath = Uri.parse(itemUrl).pathSegments.firstOrNull()
    private val parsedBaseUrl = Uri.parse(baseUrl)

    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .build()

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/filterList?page=$page&sortBy=views&asc=false", headers)
    }

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url: Uri.Builder
        url = Uri.parse("$baseUrl/advanced-search")!!.buildUpon()
        url.appendQueryParameter("fl", "1")
        var paramsString: String = "%26languages%255B%255D%3D${langCode(lang)}"
        if (query.isNotBlank()){
            paramsString = "$paramsString%26name%3D$query"
        }
        filters.forEach { filter ->
            when (filter) {
                is TagsFilter -> {
                    filter.state
                        .filter { it.state }
                        .let { list ->
                            if (list.isNotEmpty()) { list.forEach { item -> paramsString = "$paramsString%26tags%255B%255D%3D${item.id}"} }
                        }
                }
                is AuthorFilter -> {
                    if (filter.state.isNotBlank()) {
                        paramsString = "$paramsString%26author%3D${filter.state}"
                    }
                }
                is CategoriesFilter-> {
                    filter.state
                        .filter { it.state }
                        .let { list ->
                            if (list.isNotEmpty()) { list.forEach { item -> paramsString = "$paramsString%26categories%255B%255D%3D${item.id}"} }
                        }
                }
                is CharactersFilter-> {
                    filter.state
                        .filter { it.state }
                        .let { list ->
                            if (list.isNotEmpty()) { list.forEach { item -> paramsString = "$paramsString%26characters%255B%255D%3D${item.id}"} }
                        }
                }
                is LanguagesFilter-> {
                    filter.state
                        .filter { it.state }
                        .let { list ->
                            if (list.isNotEmpty()) { list.forEach { item -> paramsString = "$paramsString%26languages%255B%255D%3D${item.id}"} }
                        }
                }
                is StatusFilter-> {
                    filter.state
                        .filter { it.state }
                        .let { list ->
                            if (list.isNotEmpty()) { list.forEach { item -> paramsString = "$paramsString%26status%255B%255D%3D${item.id}"} }
                        }
                }
                is ParodiesFilter-> {
                    filter.state
                        .filter { it.state }
                        .let { list ->
                            if (list.isNotEmpty()) { list.forEach { item -> paramsString = "$paramsString%26parodies%255B%255D%3D${item.id}"} }
                        }
                }

            }
        }
        url.appendQueryParameter("params", paramsString)
        url.appendQueryParameter("page", "$page")
        return GET(url.toString(), headers)
    }

    /**
     * If the usual search engine isn't available, search through the list of titles with this
     */
    private fun selfSearch(query: String): Observable<MangasPage> {
        return client.newCall(GET("$baseUrl/changeMangaList?type=text", headers))
            .asObservableSuccess()
            .map { response ->
                val mangas = response.asJsoup().select("ul.manga-list a")
                    .filter { it.text().contains(query, ignoreCase = true) }
                    .map {
                        SManga.create().apply {
                            title = it.text()
                            setUrlWithoutDomain(it.attr("abs:href"))
                            thumbnail_url = coverGuess(null, it.attr("abs:href"))
                        }
                    }
                MangasPage(mangas, false)
            }
    }

    override fun latestUpdatesRequest(page: Int) = GET("$baseUrl/latest-release?page=$page", headers)

    override fun popularMangaParse(response: Response) = internalMangaParse(response)
    override fun searchMangaParse(response: Response): MangasPage {
        return if (listOf("query", "q").any { it in response.request().url().queryParameterNames() }) {
            // If a search query was specified, use search instead!
            val jsonArray = jsonParser.parse(response.body()!!.string()).let {
                if (name == "Mangas.pw") it.array else it["suggestions"].array
            }
            MangasPage(
                jsonArray
                    .map {
                        SManga.create().apply {
                            val segment = it["data"].string
                            url = getUrlWithoutBaseUrl(itemUrl + segment)
                            title = it["value"].string

                            // Guess thumbnails
                            // thumbnail_url = "$baseUrl/uploads/manga/$segment/cover/cover_250x350.jpg"
                        }
                    },
                false
            )
        } else {
            internalMangaParse(response)
        }
    }

    private val latestTitles = mutableSetOf<String>()

    override fun latestUpdatesParse(response: Response): MangasPage {
        val document = response.asJsoup()

        if (document.location().contains("page=1")) latestTitles.clear()

        val mangas = document.select(latestUpdatesSelector())
            .let { elements ->
                when {
                    // Mangas.pw
                    elements.select("a.fa-info-circle + a").firstOrNull()?.hasText() == true -> elements.map { latestUpdatesFromElement(it, "a.fa-info-circle + a") }
                    // List layout (most sources)
                    elements.select("a").firstOrNull()?.hasText() == true -> elements.map { latestUpdatesFromElement(it, "a") }
                    // Grid layout (e.g. MangaID)
                    else -> document.select(gridLatestUpdatesSelector()).map { gridLatestUpdatesFromElement(it) }
                }
            }
            .filterNotNull()

        return MangasPage(mangas, document.select(latestUpdatesNextPageSelector()) != null)
    }
    private fun latestUpdatesSelector() = "div.mangalist div.manga-item"
    private fun latestUpdatesNextPageSelector() = "a[rel=next]"
    private fun latestUpdatesFromElement(element: Element, urlSelector: String): SManga? {
        return element.select(urlSelector).first().let { titleElement ->
            if (titleElement.text() in latestTitles) {
                null
            } else {
                latestTitles.add(titleElement.text())
                SManga.create().apply {
                    url = titleElement.attr("abs:href").substringAfter(baseUrl) // intentionally not using setUrlWithoutDomain
                    title = titleElement.text().trim()
                    thumbnail_url = "$baseUrl/uploads/manga/${url.substringAfterLast('/')}/cover/cover_250x350.jpg"
                }
            }
        }
    }
    private fun gridLatestUpdatesSelector() = "div.mangalist div.manga-item, div.grid-manga tr"
    private fun gridLatestUpdatesFromElement(element: Element): SManga = SManga.create().apply {
        element.select("a.chart-title").let {
            setUrlWithoutDomain(it.attr("href"))
            title = it.text()
        }
        thumbnail_url = element.select("img").attr("abs:src")
    }

    private fun internalMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()

        val internalMangaSelector = when (name) {
            "Utsukushii" -> "div.content div.col-sm-6"
            else -> "div[class^=col-sm], div.col-xs-6"
        }
        return MangasPage(
            document.select(internalMangaSelector).map {
                SManga.create().apply {
                    val urlElement = it.getElementsByClass("chart-title")
                    if (urlElement.size == 0) {
                        url = getUrlWithoutBaseUrl(it.select("a").attr("href"))
                        title = it.select("div.caption").text()
                        it.select("div.caption div").text().let { if (it.isNotEmpty()) title = title.substringBefore(it) } // To clean submanga's titles without breaking hentaishark's
                    } else {
                        url = getUrlWithoutBaseUrl(urlElement.attr("href"))
                        title = urlElement.text().trim()
                    }

                    it.select("img").let { img ->
                        thumbnail_url = when {
                            it.hasAttr("data-background-image") -> it.attr("data-background-image") // Utsukushii
                            img.hasAttr("data-src") -> coverGuess(img.attr("abs:data-src"), url)
                            else -> coverGuess(img.attr("abs:src"), url)
                        }
                    }
                }
            },
            document.select(".pagination a[rel=next]").isNotEmpty()
        )
    }

    // Guess thumbnails on broken websites
    public fun coverGuess(url: String?, mangaUrl: String): String? {
        return if (url?.endsWith("no-image.png") == true) {
            "$baseUrl/uploads/manga/${mangaUrl.substringAfterLast('/')}/cover/cover_250x350.jpg"
        } else {
            url
        }
    }

    public fun getUrlWithoutBaseUrl(newUrl: String): String {
        val parsedNewUrl = Uri.parse(newUrl)
        val newPathSegments = parsedNewUrl.pathSegments.toMutableList()

        for (i in parsedBaseUrl.pathSegments) {
            if (i.trim().equals(newPathSegments.first(), true)) {
                newPathSegments.removeAt(0)
            } else break
        }

        val builtUrl = parsedNewUrl.buildUpon().path("/")
        newPathSegments.forEach { builtUrl.appendPath(it) }

        var out = builtUrl.build().encodedPath!!
        if (parsedNewUrl.encodedQuery != null)
            out += "?" + parsedNewUrl.encodedQuery
        if (parsedNewUrl.encodedFragment != null)
            out += "#" + parsedNewUrl.encodedFragment

        return out
    }

    @SuppressLint("DefaultLocale")
    override fun mangaDetailsParse(response: Response) = SManga.create().apply {
        val document = response.asJsoup()
        document.select("h2.listmanga-header, h2.widget-title").firstOrNull()?.text()?.trim()?.let { title = it }
        thumbnail_url = coverGuess(document.select(".row [class^=img-responsive]").firstOrNull()?.attr("abs:src"), document.location())
        description = document.select(".row .well p").text().trim()

        val detailAuthor = setOf("author(s)", "autor(es)", "auteur(s)", "著作", "yazar(lar)", "mangaka(lar)", "pengarang/penulis", "pengarang", "penulis", "autor", "المؤلف", "перевод", "autor/autorzy")
        val detailArtist = setOf("artist(s)", "artiste(s)", "sanatçi(lar)", "artista(s)", "artist(s)/ilustrator", "الرسام", "seniman", "rysownik/rysownicy")
        val detailGenre = setOf("categories", "categorías", "catégories", "ジャンル", "kategoriler", "categorias", "kategorie", "التصنيفات", "жанр", "kategori", "tagi")
        val detailStatus = setOf("status", "statut", "estado", "状態", "durum", "الحالة", "статус")
        val detailStatusComplete = setOf("complete", "مكتملة", "complet", "completo", "zakończone")
        val detailStatusOngoing = setOf("ongoing", "مستمرة", "en cours", "em lançamento", "prace w toku")
        val detailDescription = setOf("description", "resumen")

        for (element in document.select(".row .dl-horizontal dt")) {
            when (element.text().trim().toLowerCase()) {
                in detailAuthor -> author = element.nextElementSibling().text()
                in detailArtist -> artist = element.nextElementSibling().text()
                in detailGenre -> genre = element.nextElementSibling().select("a").joinToString {
                    it.text().trim()
                }
                in detailStatus -> status = when (element.nextElementSibling().text().trim().toLowerCase()) {
                    in detailStatusComplete -> SManga.COMPLETED
                    in detailStatusOngoing -> SManga.ONGOING
                    else -> SManga.UNKNOWN
                }
            }
        }
        // When details are in a .panel instead of .row (ES sources)
        for (element in document.select("div.panel span.list-group-item")) {
            when (element.select("b").text().toLowerCase().substringBefore(":")) {
                in detailAuthor -> author = element.select("b + a").text()
                in detailArtist -> artist = element.select("b + a").text()
                in detailGenre -> genre = element.getElementsByTag("a").joinToString {
                    it.text().trim()
                }
                in detailStatus -> status = when (element.select("b + span.label").text().toLowerCase()) {
                    in detailStatusComplete -> SManga.COMPLETED
                    in detailStatusOngoing -> SManga.ONGOING
                    else -> SManga.UNKNOWN
                }
                in detailDescription -> description = element.ownText()
            }
        }
    }

    /**
     * Parses the response from the site and returns a list of chapters.
     *
     * Overriden to allow for null chapters
     *
     * @param response the response from the site.
     */
    override fun chapterListParse(response: Response): List<SChapter> {
        val document = response.asJsoup()
        return document.select(chapterListSelector()).mapNotNull { nullableChapterFromElement(it) }
    }

    /**
     * Returns the Jsoup selector that returns a list of [Element] corresponding to each chapter.
     */
    private fun chapterListSelector() = "ul[class^=chapters] > li:not(.btn), table.table tr"
    // Some websites add characters after "chapters" thus the need of checking classes that starts with "chapters"

    /**
     * titleWrapper can have multiple "a" elements, filter to the first that contains letters (i.e. not "" or # as is possible)
     */
    private val urlRegex = Regex("""[a-zA-z]""")

    /**
     * Returns a chapter from the given element.
     *
     * @param element an element obtained from [chapterListSelector].
     */
    private fun nullableChapterFromElement(element: Element): SChapter? {
        val chapter = SChapter.create()

        try {
            val titleWrapper = if (name == "Mangas.pw") element.select("i a").first() else element.select("[class^=chapter-title-rtl]").first()
            // Some websites add characters after "..-rtl" thus the need of checking classes that starts with that
            val url = titleWrapper.getElementsByTag("a")
                .first { it.attr("href").contains(urlRegex) }
                .attr("href")

            // Ensure chapter actually links to a manga
            // Some websites use the chapters box to link to post announcements
            // The check is skipped if mangas are stored in the root of the website (ex '/one-piece' without a segment like '/manga/one-piece')
            if (itemUrlPath != null && !Uri.parse(url).pathSegments.firstOrNull().equals(itemUrlPath, true)) {
                return null
            }

            chapter.url = getUrlWithoutBaseUrl(url)
            chapter.name = titleWrapper.text()

            // Parse date
            val dateText = element.getElementsByClass("date-chapter-title-rtl").text().trim()
            chapter.date_upload = parseDate(dateText)

            return chapter
        } catch (e: NullPointerException) {
            // For chapter list in a table
            if (element.select("td").hasText()) {
                element.select("td a").let {
                    chapter.setUrlWithoutDomain(it.attr("href"))
                    chapter.name = it.text()
                }
                val tableDateText = element.select("td + td").text()
                chapter.date_upload = parseDate(tableDateText)

                return chapter
            }
        }

        return null
    }

    private fun parseDate(dateText: String): Long {
        return try {
            MMRCMSMultiLang.DATE_FORMAT.parse(dateText)?.time ?: 0
        } catch (e: ParseException) {
            0L
        }
    }

    override fun pageListParse(response: Response) = response.asJsoup().select("#all > .img-responsive")
        .mapIndexed { i, e ->
            var url = (if (e.hasAttr("data-src")) e.attr("abs:data-src") else e.attr("abs:src")).trim()

            // Mangas.pw encodes some of their urls, decode them
            if (name.contains("Mangas.pw") && !url.contains(".")) {
                url = Base64.decode(url.substringAfter("//"), Base64.DEFAULT).toString(Charsets.UTF_8).substringBefore("=")
                url = URLDecoder.decode(url, "UTF-8")
            }

            Page(i, "", url)
        }


    override fun imageUrlParse(response: Response) = throw UnsupportedOperationException("Unused method called!")












    /**
     * Returns the list of filters for the source.
     */
    override fun getFilterList() = FilterList(
        AuthorFilter(),
        CategoriesFilter("Categories", categoriesMappings),
        LanguagesFilter("Languages", languagesMappings),
        StatusFilter("Status", statusMappings),
        CharactersFilter("Characters", charactersMappings),
        ParodiesFilter("Parodies", parodiesMappings),
        TagsFilter("Tags", tagsMappings),
    )

    private class AuthorFilter : Filter.Text("Author")

    open class GroupList(name: String, genres: List<CheckBoxs>) : Filter.Group<CheckBoxs>(name, genres)
    class CheckBoxs(name: String, val id: String = name) : Filter.CheckBox(name)

    class CategoriesFilter(name: String, options: List<CheckBoxs>) : GroupList(name, options)
    class LanguagesFilter(name: String, options: List<CheckBoxs>) : GroupList(name, options)
    class StatusFilter(name: String, options: List<CheckBoxs>) : GroupList(name, options)
    class CharactersFilter(name: String, options: List<CheckBoxs>) : GroupList(name, options)
    class ParodiesFilter(name: String, options: List<CheckBoxs>) : GroupList(name, options)
    class TagsFilter(name: String, options: List<CheckBoxs>) : GroupList(name, options)


    private fun langCode(lang: String): String {
        return when (lang) {
            "Translated" -> "1"
            "English", "en" -> "2"
            "Japanese", "ja"  -> "3"
            "Chinese", "zh" -> "4"
            "German", "de" -> "5"
            "Dutch", "nl" -> "6"
            "Korean", "ko" -> "7"
            "Rewrite" -> "8"
            "Speechless" -> "9"
            "text-cleaned", "other" -> "10"
            "Czech", "cz" -> "11"
            "Esperanto", "eo" -> "12"
            "mongolian", "mn" -> "13"
            "arabic", "ar" -> "14"
            "slovak", "sk" -> "15"
            "latin", "la" -> "16"
            "ukrainian", "ua" -> "17"
            "cebuano", "ceb" -> "18"
            "tagalog", "tl" -> "19"
            "finnish", "fi" -> "20"
            "bulgarian", "bg" -> "21"
            "turkish", "tr" -> "22"
            else -> "0" //Returns no results
        }
    }

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("d MMM. yyyy", Locale.US)
    }
}
