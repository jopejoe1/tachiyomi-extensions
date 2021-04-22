package eu.kanade.tachiyomi.multisrc.luscious

import com.github.salomonbrys.kotson.addProperty
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.set
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
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
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.nodes.Document
import rx.Observable
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class LusciousOLD(
    override val name: String,
    final override val baseUrl: String,
    final override val lang: String ) : HttpSource() {

  //Based on Luscios single source extension form https://github.com/tachiyomiorg/tachiyomi-extensions/commit/aacf56d0c0ddb173372aac69d798ae998f178377
  //with modifiaction to make it support multisrc
    override val supportsLatest: Boolean = true
    private val apiBaseUrl: String = "$baseUrl/graphql/nobatch/"
    private val gson = Gson()
    override val client: OkHttpClient = network.cloudflareClient
    private val lusLang: String = lusLang(lang)
    private fun lusLang(lang: String): String {
        return when (lang) {
            "en" -> "1"
            "ja" -> "2"
            "es" -> "3"
            "it" -> "4"
            "de" -> "5"
            "fr" -> "6"
            "zh" -> "8"
            "ko" -> "9"
            "pt" -> "100"
            "th" -> "101"
            else -> "99"
        }
    }


    // Common

    private fun buildAlbumListRequestInput(page: Int, filters: FilterList, query: String = ""): JsonObject {
        val sortByFilter = filters.findInstance<SortBySelectFilter>()!!
        val albumTypeFilter = filters.findInstance<AlbumTypeSelectFilter>()!!
        val interestsFilter = filters.findInstance<InterestGroupFilter>()!!
        val languagesFilter = filters.findInstance<LanguageGroupFilter>()!!
        val tagsFilter = filters.findInstance<TagGroupFilter>()!!
        val genreFilter = filters.findInstance<GenreGroupFilter>()!!
        val contentTypeFilter = filters.findInstance<ContentTypeSelectFilter>()!!

        return JsonObject().apply {
            add(
                "input",
                JsonObject().apply {
                    addProperty("display", sortByFilter.selected)
                    addProperty("page", page)
                    add(
                        "filters",
                        JsonArray().apply {

                            if (contentTypeFilter.selected != FILTER_VALUE_IGNORE)
                                add(contentTypeFilter.toJsonObject("content_id"))

                            if (albumTypeFilter.selected != FILTER_VALUE_IGNORE)
                                add(albumTypeFilter.toJsonObject("album_type"))

                            with(interestsFilter) {
                                if (this.selected.isEmpty()) {
                                    throw Exception("Please select an Interest")
                                }
                                add(this.toJsonObject("audience_ids"))
                            }

                            add(
                                languagesFilter.toJsonObject("language_ids").apply {
                                    set("value", "+$lusLang${get("value").asString}")
                                }
                            )

                            if (tagsFilter.anyNotIgnored()) {
                                add(tagsFilter.toJsonObject("tagged"))
                            }

                            if (genreFilter.anyNotIgnored()) {
                                add(genreFilter.toJsonObject("genre_ids"))
                            }

                            if (query != "") {
                                add(
                                    JsonObject().apply {
                                        addProperty("name", "search_query")
                                        addProperty("value", query)
                                    }
                                )
                            }
                        }
                    )
                }
            )
        }
    }

    private fun buildAlbumListRequest(page: Int, filters: FilterList, query: String = ""): Request {
        val input = buildAlbumListRequestInput(page, filters, query)
        val url = HttpUrl.parse(apiBaseUrl)!!.newBuilder()
            .addQueryParameter("operationName", "AlbumList")
            .addQueryParameter("query", ALBUM_LIST_REQUEST_GQL)
            .addQueryParameter("variables", input.toString())
            .toString()
        return GET(url, headers)
    }

    private fun parseAlbumListResponse(response: Response): MangasPage {
        val data = gson.fromJson<JsonObject>(response.body()!!.string())
        with(data["data"]["album"]["list"]) {
            return MangasPage(
                this["items"].asJsonArray.map {
                    SManga.create().apply {
                        url = it["url"].asString
                        title = it["title"].asString
                        thumbnail_url = it["cover"]["url"].asString
                    }
                },
                this["info"]["has_next_page"].asBoolean
            )
        }
    }

    // Latest

    override fun latestUpdatesRequest(page: Int): Request = buildAlbumListRequest(page, getSortFilters(LATEST_DEFAULT_SORT_STATE))

    override fun latestUpdatesParse(response: Response): MangasPage = parseAlbumListResponse(response)

    // Chapters

    private fun buildAlbumInfoRequest(id: String): Request {
        val url = HttpUrl.parse(apiBaseUrl)!!.newBuilder()
            .addQueryParameter("operationName", "getAlbumInfo")
            .addQueryParameter("query", ALBUM_INFO_REQUEST_GQL)
            .addQueryParameter("variables", "{\"id\": \"$id\"}")
            .toString()
        return GET(url, headers)
    }

    private fun getID(url: String): String{
        var id = url
        while (id.contains("_")){
            id = id.substringAfter("_").replace("/","").trim()
        }
        return id
    }

    override fun chapterListRequest(manga: SManga): Request {
        val id:String = getID(manga.url)
        return buildAlbumInfoRequest(id)
    }

    override fun chapterListParse(response: Response): List<SChapter> {
        val chapters = mutableListOf<SChapter>()
        val data = gson.fromJson<JsonObject>(response.body()!!.string())
        with(data["data"]["album"]["get"]) {
            return when (this["is_manga"].asBoolean) {
                true -> {
                    val chapter = SChapter.create()
                    chapter.url = "$baseUrl${this["url"].asString}"
                    chapter.name = "Chapter"
                    chapter.date_upload = this["modified"].asLong
                    chapter.chapter_number = 1f
                    chapters.add(chapter)
                    chapters
                }
                false -> pictureChapters(this["id"].asString)
            }
        }
    }

    private fun pictureChapters(id: String): List<SChapter>{
        val chapters = mutableListOf<SChapter>()
        var page = 1
        var nextPage = true
        while (nextPage){
            val url = buildAlbumPicturesPageUrl(id, page, "date_newest")
            val response: Response = client.newCall(GET(url, headers)).execute()
            val data = gson.fromJson<JsonObject>(response.body()!!.string())
            with(data["data"]["picture"]["list"]){
                this["items"].asJsonArray.map {
                    val chapter = SChapter.create()
                    //chapter.url = "$baseUrl${this["url"].asString}"
                    chapter.url = this["thumbnails"][0]["url"].asString
                    chapter.name = this["title"].asString
                    chapter.date_upload = this["created"].asLong
                    chapter.chapter_number = 1f
                    chapters.add(chapter)
                }
                nextPage = this["info"]["has_next_page"].asBoolean
            }
            page++
        }
        return chapters
    }

    // Pages

    private fun buildAlbumPicturesRequestInput(id: String, page: Int, sortPagesByOption: String): JsonObject {
        return JsonObject().apply {
            addProperty(
                "input",
                JsonObject().apply {
                    addProperty(
                        "filters",
                        JsonArray().apply {
                            add(
                                JsonObject().apply {
                                    addProperty("name", "album_id")
                                    addProperty("value", id)
                                }
                            )
                        }
                    )
                    addProperty("display", sortPagesByOption)
                    addProperty("page", page)
                }
            )
        }
    }

    private fun buildAlbumPicturesPageUrl(id: String, page: Int, sortPagesByOption: String): String {
        val input = buildAlbumPicturesRequestInput(id, page, sortPagesByOption)
        return HttpUrl.parse(apiBaseUrl)!!.newBuilder()
            .addQueryParameter("operationName", "AlbumListOwnPictures")
            .addQueryParameter("query", ALBUM_PICTURES_REQUEST_GQL)
            .addQueryParameter("variables", input.toString())
            .toString()
    }

    private fun parseAlbumPicturesResponse(response: Response, sortPagesByOption: String): List<Page> {

        val id = response.request().url().queryParameter("variables").toString()
            .let { gson.fromJson<JsonObject>(it)["input"]["filters"].asJsonArray }
            .let { it.first { f -> f["name"].asString == "album_id" } }
            .let { it["value"].asString }

        val data = gson.fromJson<JsonObject>(response.body()!!.string())
            .let { it["data"]["picture"]["list"].asJsonObject }

        return data["items"].asJsonArray.mapIndexed { index, it ->
            Page(index, imageUrl = it["thumbnails"][0]["url"].asString)
        } + if (data["info"]["total_pages"].asInt > 1) { // get 2nd page onwards
            (ITEMS_PER_PAGE until data["info"]["total_items"].asInt).chunked(ITEMS_PER_PAGE).mapIndexed { page, indices ->
                indices.map { Page(it, url = buildAlbumPicturesPageUrl(id, page + 2, sortPagesByOption)) }
            }.flatten()
        } else emptyList()
    }

    private fun getAlbumSortPagesOption(chapter: SChapter): Observable<String> {
        return client.newCall(GET(chapter.url))
            .asObservableSuccess()
            .map {
                val sortByKey = it.asJsoup().select(".o-input-select:contains(Sorted By) .o-select-value")?.text() ?: ""
                ALBUM_PICTURES_SORT_OPTIONS.getValue(sortByKey)
            }
    }

    override fun fetchPageList(chapter: SChapter): Observable<List<Page>> {
        when {
            chapter.url.contains("$baseUrl/albums/") -> {
                val id = chapter.url.substringAfterLast("_").removeSuffix("/")

                return getAlbumSortPagesOption(chapter)
                    .concatMap { sortPagesByOption ->
                        client.newCall(GET(buildAlbumPicturesPageUrl(id, 1, sortPagesByOption)))
                            .asObservableSuccess()
                            .map { parseAlbumPicturesResponse(it, sortPagesByOption) }
                    }
            }
            else -> {
                throw Exception("Stub!")
            }
        }

    }

    override fun pageListParse(response: Response): List<Page> = throw UnsupportedOperationException("Not used")


    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException("Not used")

    override fun fetchImageUrl(page: Page): Observable<String> {
        if (page.imageUrl != null) {
            return Observable.just(page.imageUrl)
        }

        return client.newCall(GET(page.url, headers))
            .asObservableSuccess()
            .map {
                val data = gson.fromJson<JsonObject>(it.body()!!.string()).let { data ->
                    data["data"]["picture"]["list"].asJsonObject
                }
                data["items"].asJsonArray[page.index % 50].asJsonObject["thumbnails"][0]["url"].asString
            }
    }

    // Details

    override fun mangaDetailsParse(response: Response): SManga {
        val data = gson.fromJson<JsonObject>(response.body()!!.string())
        with(data["data"]["album"]["get"]) {
            val manga = SManga.create()
            manga.url = this["url"].asString
            manga.title = this["title"].asString
            manga.thumbnail_url = this["cover"]["url"].asString
            manga.description = "${this["description"].asString}\n\nImages: ${this["number_of_pictures"].asString}\n GIFs: ${this["number_of_animated_pictures"].asString}"
            var genreList = ""
            if (this["language"]["title"].asString != "") {
                genreList = "${this["language"]["title"].asString}, "
            }
            if (this["tags"].asString != null) {
                for ((i, _) in this["tags"].asJsonArray.withIndex()) {
                    genreList = "$genreList${this["tags"][i]["text"].asString}, "
                }
            }
            if (this["genres"].asString != null) {
                for ((i, _) in this["genres"].asJsonArray.withIndex()) {
                    genreList = "$genreList${this["genres"][i]["text"].asString}, "
                }
            }
            if (this["audiences"].asString != null) {
                for ((i, _) in this["audiences"].asJsonArray.withIndex()) {
                    genreList = "$genreList${this["audiences"][i]["title"].asString}, "
                }
            }
            if (this["labels"].asString != null) {
                for ((i, _) in this["labels"].asJsonArray.withIndex()) {
                    genreList = "$genreList${this["labels"][i].asString}, "
                }
            }
            genreList = "$genreList${this["content"]["title"].asString}"
            manga.genre = genreList
            return manga
        }
    }

    override fun mangaDetailsRequest(manga: SManga): Request {
        val id:String = getID(manga.url)
        return buildAlbumInfoRequest(id)
    }

    override fun popularMangaParse(response: Response): MangasPage = parseAlbumListResponse(response)

    override fun popularMangaRequest(page: Int): Request = buildAlbumListRequest(page, getSortFilters(POPULAR_DEFAULT_SORT_STATE))

    // Search

    override fun searchMangaParse(response: Response): MangasPage = parseAlbumListResponse(response)

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request = buildAlbumListRequest(
        page,
        filters.let {
            if (it.isEmpty()) getSortFilters(SEARCH_DEFAULT_SORT_STATE)
            else it
        },
        query
    )

    class TriStateFilterOption(name: String, val value: String) : Filter.TriState(name)
    abstract class TriStateGroupFilter(name: String, options: List<TriStateFilterOption>) : Filter.Group<TriStateFilterOption>(name, options) {
        private val included: List<String>
            get() = state.filter { it.isIncluded() }.map { it.value }

        private val excluded: List<String>
            get() = state.filter { it.isExcluded() }.map { it.value }

        fun anyNotIgnored(): Boolean = state.any { !it.isIgnored() }

        override fun toString(): String = (included.map { "+$it" } + excluded.map { "-$it" }).joinToString("")
    }

    private fun Filter<*>.toJsonObject(key: String): JsonObject {
        val value = this.toString()
        return JsonObject().apply {
            addProperty("name", key)
            addProperty("value", value)
        }
    }

    private class TagGroupFilter(filters: List<TriStateFilterOption>) : TriStateGroupFilter("Tags", filters)
    private class GenreGroupFilter(filters: List<TriStateFilterOption>) : TriStateGroupFilter("Genres", filters)

    class CheckboxFilterOption(name: String, val value: String, default: Boolean = true) : Filter.CheckBox(name, default)
    abstract class CheckboxGroupFilter(name: String, options: List<CheckboxFilterOption>) : Filter.Group<CheckboxFilterOption>(name, options) {
        val selected: List<String>
            get() = state.filter { it.state }.map { it.value }

        override fun toString(): String = selected.joinToString("") { "+$it" }
    }

    private class InterestGroupFilter(options: List<CheckboxFilterOption>) : CheckboxGroupFilter("Interests", options)
    private class LanguageGroupFilter(options: List<CheckboxFilterOption>) : CheckboxGroupFilter("Languages", options)

    class SelectFilterOption(val name: String, val value: String)

    abstract class SelectFilter(name: String, private val options: List<SelectFilterOption>, default: Int = 0) : Filter.Select<String>(name, options.map { it.name }.toTypedArray(), default) {
        val selected: String
            get() = options[state].value

        override fun toString(): String = selected
    }
    class SortBySelectFilter(options: List<SelectFilterOption>, default: Int) : SelectFilter("Sort By", options, default)
    class AlbumTypeSelectFilter(options: List<SelectFilterOption>) : SelectFilter("Album Type", options)
    class ContentTypeSelectFilter(options: List<SelectFilterOption>) : SelectFilter("Content Type", options)

    override fun getFilterList(): FilterList = getSortFilters(POPULAR_DEFAULT_SORT_STATE)

    private fun getSortFilters(sortState: Int) = FilterList(
        SortBySelectFilter(getSortFilters(), sortState),
        AlbumTypeSelectFilter(getAlbumTypeFilters()),
        ContentTypeSelectFilter(getContentTypeFilters()),
        InterestGroupFilter(getInterestFilters()),
        LanguageGroupFilter(getLanguageFilters()),
        TagGroupFilter(getTagFilters()),
        GenreGroupFilter(getGenreFilters())
    )

    private fun getSortFilters() = listOf(
        SelectFilterOption("Rating - All Time", "rating_all_time"),
        SelectFilterOption("Rating - Last 7 Days", "rating_7_days"),
        SelectFilterOption("Rating - Last 14 Days", "rating_14_days"),
        SelectFilterOption("Rating - Last 30 Days", "rating_30_days"),
        SelectFilterOption("Rating - Last 90 Days", "rating_90_days"),
        SelectFilterOption("Rating - Last Year", "rating_1_year"),
        SelectFilterOption("Rating - Last Year", "rating_1_year"),
        SelectFilterOption("Date - Newest First", "date_newest"),
        SelectFilterOption("Date - 2020", "date_2020"),
        SelectFilterOption("Date - 2019", "date_2019"),
        SelectFilterOption("Date - 2018", "date_2018"),
        SelectFilterOption("Date - 2017", "date_2017"),
        SelectFilterOption("Date - 2016", "date_2016"),
        SelectFilterOption("Date - 2015", "date_2015"),
        SelectFilterOption("Date - 2014", "date_2014"),
        SelectFilterOption("Date - 2013", "date_2013"),
        SelectFilterOption("Date - Oldest First", "date_oldest"),
        SelectFilterOption("Date - Upcoming", "date_upcoming"),
        SelectFilterOption("Date - Trending", "date_trending"),
        SelectFilterOption("Date - Featured", "date_featured"),
        SelectFilterOption("Date - Last Viewed", "date_last_interaction")
    )

    private fun getAlbumTypeFilters() = listOf(
        SelectFilterOption("Manga", "manga"),
        SelectFilterOption("All", FILTER_VALUE_IGNORE),
        SelectFilterOption("Pictures", "pictures")
    )

    private fun getContentTypeFilters() = listOf(
        SelectFilterOption("All", FILTER_VALUE_IGNORE),
        SelectFilterOption("Hentai", "0"),
        SelectFilterOption("Non-Erotic", "5"),
        SelectFilterOption("Real People", "6")
    )

    private fun getInterestFilters() = listOf(
        CheckboxFilterOption("Straight Sex", "1"),
        CheckboxFilterOption("Trans x Girl", "10", false),
        CheckboxFilterOption("Gay / Yaoi", "2"),
        CheckboxFilterOption("Lesbian / Yuri", "3"),
        CheckboxFilterOption("Trans", "5"),
        CheckboxFilterOption("Solo Girl", "6"),
        CheckboxFilterOption("Trans x Trans", "8"),
        CheckboxFilterOption("Trans x Guy", "9")
    )

    private fun getLanguageFilters() = listOf(
        CheckboxFilterOption("English", ENGLISH_LUS_LANG_VAL, false),
        CheckboxFilterOption("Japanese", JAPANESE_LUS_LANG_VAL, false),
        CheckboxFilterOption("Spanish", SPANISH_LUS_LANG_VAL, false),
        CheckboxFilterOption("Italian", ITALIAN_LUS_LANG_VAL, false),
        CheckboxFilterOption("German", GERMAN_LUS_LANG_VAL, false),
        CheckboxFilterOption("French", FRENCH_LUS_LANG_VAL, false),
        CheckboxFilterOption("Chinese", CHINESE_LUS_LANG_VAL, false),
        CheckboxFilterOption("Korean", KOREAN_LUS_LANG_VAL, false),
        CheckboxFilterOption("Others", OTHERS_LUS_LANG_VAL, false),
        CheckboxFilterOption("Portugese", PORTUGESE_LUS_LANG_VAL, false),
        CheckboxFilterOption("Thai", THAI_LUS_LANG_VAL, false)
    ).filterNot { it.value == lusLang }

    private fun getTagFilters() = listOf(
        TriStateFilterOption("Big Breasts", "big_breasts"),
        TriStateFilterOption("Blowjob", "blowjob"),
        TriStateFilterOption("Anal", "anal"),
        TriStateFilterOption("Group", "group"),
        TriStateFilterOption("Big Ass", "big_ass"),
        TriStateFilterOption("Full Color", "full_color"),
        TriStateFilterOption("Schoolgirl", "schoolgirl"),
        TriStateFilterOption("Rape", "rape"),
        TriStateFilterOption("Glasses", "glasses"),
        TriStateFilterOption("Nakadashi", "nakadashi"),
        TriStateFilterOption("Yuri", "yuri"),
        TriStateFilterOption("Paizuri", "paizuri"),
        TriStateFilterOption("Ahegao", "ahegao"),
        TriStateFilterOption("Group: metart", "group%3A_metart"),
        TriStateFilterOption("Brunette", "brunette"),
        TriStateFilterOption("Solo", "solo"),
        TriStateFilterOption("Blonde", "blonde"),
        TriStateFilterOption("Shaved Pussy", "shaved_pussy"),
        TriStateFilterOption("Small Breasts", "small_breasts"),
        TriStateFilterOption("Cum", "cum"),
        TriStateFilterOption("Stockings", "stockings"),
        TriStateFilterOption("Yuri", "yuri"),
        TriStateFilterOption("Ass", "ass"),
        TriStateFilterOption("Creampie", "creampie"),
        TriStateFilterOption("Rape", "rape"),
        TriStateFilterOption("Oral Sex", "oral_sex"),
        TriStateFilterOption("Bondage", "bondage"),
        TriStateFilterOption("Futanari", "futanari"),
        TriStateFilterOption("Double Penetration", "double_penetration"),
        TriStateFilterOption("Threesome", "threesome"),
        TriStateFilterOption("Anal Sex", "anal_sex"),
        TriStateFilterOption("Big Cock", "big_cock"),
        TriStateFilterOption("Straight Sex", "straight_sex"),
        TriStateFilterOption("Yaoi", "yaoi")
    )

    private fun getGenreFilters() = listOf(
        TriStateFilterOption("3D / Digital Art", "25"),
        TriStateFilterOption("Amateurs", "20"),
        TriStateFilterOption("Artist Collection", "19"),
        TriStateFilterOption("Asian Girls", "12"),
        TriStateFilterOption("Cosplay", "22"),
        TriStateFilterOption("BDSM", "27"),
        TriStateFilterOption("Cross-Dressing", "30"),
        TriStateFilterOption("Defloration / First Time", "59"),
        TriStateFilterOption("Ebony Girls", "32"),
        TriStateFilterOption("European Girls", "46"),
        TriStateFilterOption("Fantasy / Monster Girls", "10"),
        TriStateFilterOption("Fetish", "2"),
        TriStateFilterOption("Furries", "8"),
        TriStateFilterOption("Futanari", "31"),
        TriStateFilterOption("Group Sex", "36"),
        TriStateFilterOption("Harem", "56"),
        TriStateFilterOption("Humor", "41"),
        TriStateFilterOption("Interracial", "28"),
        TriStateFilterOption("Kemonomimi / Animal Ears", "39"),
        TriStateFilterOption("Latina Girls", "33"),
        TriStateFilterOption("Mature", "13"),
        TriStateFilterOption("Members: Original Art", "18"),
        TriStateFilterOption("Members: Verified Selfies", "21"),
        TriStateFilterOption("Military", "48"),
        TriStateFilterOption("Mind Control", "34"),
        TriStateFilterOption("Monsters & Tentacles", "38"),
        TriStateFilterOption("Netorare / Cheating", "40"),
        TriStateFilterOption("No Genre Given", "1"),
        TriStateFilterOption("Nonconsent / Reluctance", "37"),
        TriStateFilterOption("Other Ethnicity Girls", "57"),
        TriStateFilterOption("Public Sex", "43"),
        TriStateFilterOption("Romance", "42"),
        TriStateFilterOption("School / College", "35"),
        TriStateFilterOption("Sex Workers", "47"),
        TriStateFilterOption("Softcore / Ecchi", "9"),
        TriStateFilterOption("Superheroes", "17"),
        TriStateFilterOption("Tankobon", "45"),
        TriStateFilterOption("TV / Movies", "51"),
        TriStateFilterOption("Trans", "14"),
        TriStateFilterOption("Video Games", "15"),
        TriStateFilterOption("Vintage", "58"),
        TriStateFilterOption("Western", "11"),
        TriStateFilterOption("Workplace Sex", "50")
    )

    private inline fun <reified T> Iterable<*>.findInstance() = find { it is T } as? T

    private fun SimpleDateFormat.parseOrNull(string: String): Date? {
        return try {
            parse(string)
        } catch (e: ParseException) {
            null
        }
    }

    companion object {

        private val ALBUM_PICTURES_SORT_OPTIONS = hashMapOf(
            Pair("Sort By Newest", "date_newest"),
            Pair("Sort By Rating", "rating_all_time")
        ).withDefault { "position" }

        private const val ITEMS_PER_PAGE = 50

        private val ORDINAL_SUFFIXES = listOf("st", "nd", "rd", "th")
        private val DATE_FORMATS_WITH_ORDINAL_SUFFIXES = ORDINAL_SUFFIXES.map {
            SimpleDateFormat("MMMM dd'$it', yyyy", Locale.US)
        }

        const val ENGLISH_LUS_LANG_VAL = "1"
        const val JAPANESE_LUS_LANG_VAL = "2"
        const val SPANISH_LUS_LANG_VAL = "3"
        const val ITALIAN_LUS_LANG_VAL = "4"
        const val GERMAN_LUS_LANG_VAL = "5"
        const val FRENCH_LUS_LANG_VAL = "6"
        const val CHINESE_LUS_LANG_VAL = "8"
        const val KOREAN_LUS_LANG_VAL = "9"
        const val OTHERS_LUS_LANG_VAL = "99"
        const val PORTUGESE_LUS_LANG_VAL = "100"
        const val THAI_LUS_LANG_VAL = "101"

        private const val POPULAR_DEFAULT_SORT_STATE = 0
        private const val LATEST_DEFAULT_SORT_STATE = 7
        private const val SEARCH_DEFAULT_SORT_STATE = 0

        private const val FILTER_VALUE_IGNORE = "<ignore>"

        private val ALBUM_LIST_REQUEST_GQL = """
            query AlbumList(${'$'}input: AlbumListInput!) {
                album {
                    list(input: ${'$'}input) {
                        info {
                            page
                            has_next_page
                        }
                        items
                    }
                }
            }
        """.replace("\n", " ").replace("\\s+".toRegex(), " ")

        private val ALBUM_PICTURES_REQUEST_GQL = """
            +query+AlbumListOwnPictures(%24input%3A+PictureListInput!)+{+picture+{+list(input%3A+%24input)+{+info+{+...FacetCollectionInfo+}+items+{+...PictureStandardWithoutAlbum+}+}+}+}+fragment+FacetCollectionInfo+on+FacetCollectionInfo+{+page+has_next_page+has_previous_page+total_items+total_pages+items_per_page+url_complete+}+fragment+PictureStandardWithoutAlbum+on+Picture+{+__typename+id+title+description+created+like_status+number_of_comments+number_of_favorites+moderation_status+width+height+resolution+aspect_ratio+url_to_original+url_to_video+is_animated+position+tags+{+category+text+url+}+permissions+url+thumbnails+{+width+height+size+url+}+}+
        """.replace("\n", " ").replace("\\s+".toRegex(), " ")

        private val ALBUM_INFO_REQUEST_GQL = """
            query getAlbumInfo(${"$"}id: ID!) {
                album {
                    get(id: ${"$"}id) {
                        ... on Album {
                            ...AlbumStandard
                        }... on MutationError {
                            errors {
                                code message
                            }
                        }
                    }
                }
            }
            fragment AlbumStandard on Album{
                id
                title
                tags
                is_manga
                content
                genres
                cover
                description
                audiences
                number_of_pictures
                number_of_animated_pictures
                url
                download_url
                created
                modified
                language
                labels
             }
        """.replace("\n", " ").replace("\\s+".toRegex(), " ")
    }
}
