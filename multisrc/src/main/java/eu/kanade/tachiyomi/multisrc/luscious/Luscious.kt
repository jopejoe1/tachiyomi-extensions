package eu.kanade.tachiyomi.multisrc.luscious

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.set
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable

abstract class Luscious(
    override val name: String,
    final override val baseUrl: String,
    final override val lang: String ) : HttpSource() {

    //Define common values

    override val supportsLatest: Boolean = true
    private val apiUrl = "$baseUrl/graphql/nobatch/"
    override val client: OkHttpClient = network.cloudflareClient
    private inline fun <reified T> Iterable<*>.findInstance() = find { it is T } as? T
    private val lusLang: String = when (lang) {
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
    private val gson = Gson()

    // Build Request URLS

    private fun buildAlbumListRequestInput(page: Int, filters: FilterList, query: String = ""): JsonObject {
        val sortByFilter = filters.findInstance<SortBySelectFilter>()!!
        val albumTypeFilter = filters.findInstance<AlbumTypeSelectFilter>()!!
        val albumSizeFilter = filters.findInstance<AlbumSizeSelectFilter>()!!
        val interestsFilter = filters.findInstance<InterestGroupFilter>()!!
        val languagesFilter = filters.findInstance<LanguageGroupFilter>()!!
        val restrictGenresFilter = filters.findInstance<RestrictGenresSelectFilter>()!!
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

                            if (contentTypeFilter.selected != "<ignore>")
                                add(contentTypeFilter.toJsonObject("content_id"))

                            if (albumTypeFilter.selected != "<ignore>")
                                add(albumTypeFilter.toJsonObject("album_type"))

                            if (albumSizeFilter.selected != "<ignore>")
                                add(albumSizeFilter.toJsonObject("picture_count_rank"))

                            if (restrictGenresFilter.selected != "<ignore>")
                                add(restrictGenresFilter.toJsonObject("restrict_genres"))

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

    private fun buildAlbumInfoRequestInput(id: String): JsonObject {
        return JsonObject().apply {
            addProperty("id", id)
        }
    }

    private fun buildImageListRequestInput(page: Int, id: String, display: String): JsonObject {
        return JsonObject().apply {
            add(
                "input",
                JsonObject().apply {
                    addProperty("display", display)
                    addProperty("page", page)
                    add(
                        "filters",
                        JsonArray().apply {
                            JsonObject().apply {
                                addProperty("album_id", id)
                            }
                        }
                    )
                }
            )
        }
    }

    private fun buildAlbumListRequest(page: Int, filters: FilterList, query: String = ""): Request {
        val input = buildAlbumListRequestInput(page, filters, query)
        val url = HttpUrl.parse(apiUrl)!!.newBuilder()
            .addQueryParameter("operationName", "AlbumList")
            .addQueryParameter("query", albumListQuery)
            .addQueryParameter("variables", input.toString())
            .toString()
        return GET(url, headers)
    }

    private fun buildAlbumInfoRequest(id: String): Request {
        val input = buildAlbumInfoRequestInput(id)
        val url = HttpUrl.parse(apiUrl)!!.newBuilder()
            .addQueryParameter("operationName", "AlbumGet")
            .addQueryParameter("query", albumInfoQuery)
            .addQueryParameter("variables", input.toString())
            .toString()
        return GET(url, headers)
    }

    private fun buildImageListRequest(id: String, display: String, page: Int): Request {
        val input = buildImageListRequestInput(page, id, display)
        val url = HttpUrl.parse(apiUrl)!!.newBuilder()
            .addQueryParameter("operationName", "AlbumListOwnPictures")
            .addQueryParameter("query", imageListQuery)
            .addQueryParameter("variables", input.toString())
            .toString()
        return GET(url, headers)
    }

    // Parsing

    private fun parseAlbumListResponse(response: Response): MangasPage {
        val data = gson.fromJson<JsonObject>(response.body()!!.string())
        with(data["data"]["album"]["list"]) {
            return MangasPage(
                this["items"].asJsonArray.map {
                    SManga.create().apply {
                        url = it["url"].asString
                        title = it["title"].asString
                        genre = if (it["is_manga"].asBoolean){
                            "Manga"
                        } else {
                            "Picture Set"
                        }
                        thumbnail_url = it["cover"]["url"].asString
                    }
                },
                this["info"]["has_next_page"].asBoolean
            )
        }
    }

    override fun mangaDetailsParse(response: Response): SManga {
        val data = gson.fromJson<JsonObject>(response.body()!!.string())
        with(data["data"]["album"]["get"]) {
            val manga = SManga.create()
            manga.url = this["url"].asString
            manga.title = this["title"].asString
            manga.thumbnail_url = this["cover"]["url"].asString
            manga.status = 2
            manga.description = "${this["description"].asString}\n\nPictures: ${this["number_of_pictures"].asString}\nAnimated Pictures: ${this["number_of_animated_pictures"].asString}"
            var genreList = this["language"]["title"].asString
            for ((i, _) in this["labels"].asJsonArray.withIndex()) {
                genreList = "$genreList, ${this["labels"][i].asString}"
            }
            for ((i, _) in this["genres"].asJsonArray.withIndex()) {
                genreList = "$genreList, ${this["genres"][i]["title"].asString}"
            }
            for ((i, _) in this["audiences"].asJsonArray.withIndex()) {
                genreList = "$genreList, ${this["audiences"][i]["title"].asString}"
            }
            for ((i, _) in this["tags"].asJsonArray.withIndex()) {
                genreList = "$genreList, ${this["tags"][i]["text"].asString}"
            }
            genreList = "$genreList, ${this["content"]["title"].asString}"
            manga.genre = genreList

            return manga
        }
    }

    private fun singlechapterParse(response: Response, id: String): List<SChapter> {
        val chapters = mutableListOf<SChapter>()
        val ducument = gson.fromJson<JsonObject>(response.body()!!.string())
        with(ducument["data"]["album"]["get"]) {
            val chapter = SChapter.create()
            chapter.date_upload = this["modified"].asLong
            chapter.name = "Chapter"
            chapter.chapter_number = 1F
            chapter.url = id
            chapters.add(chapter)
        }
        return chapters
    }

    private fun chapterListParse(response: Response, id: String): List<SChapter> {
        val chapters = mutableListOf<SChapter>()
        var document = gson.fromJson<JsonObject>(response.body()!!.string())
        var moreChapters = true
        var nextPage = 2

        while (moreChapters) {
            document["data"]["picture"]["list"]["items"].asJsonArray.map {
                chapters.add(
                    SChapter.create().apply {
                        url = it["thumbnails"][0]["url"].asString
                        name = it["title"].asString
                        date_upload = it["created"].asLong
                        chapter_number = it["position"].asFloat
                    }
                )
            }
            moreChapters = document["data"]["picture"]["list"]["info"]["has_next_page"].asBoolean
            val nextPageUrl =  client.newCall(buildImageListRequest(id, "date_newest", nextPage)).execute()
            document = gson.fromJson<JsonObject>(nextPageUrl.body()!!.string())
            nextPage++
        }
        return chapters
    }

    private fun parsePage(response: Response, id: String): List<Page> {
        val pages = mutableListOf<Page>()
        var document = gson.fromJson<JsonObject>(response.body()!!.string())
        var nextPage = 2
        var moreChapters = true
        while (moreChapters) {
            document["data"]["picture"]["list"]["items"].asJsonArray.map {
                var url = it["thumbnails"][0]["url"].asString
                pages.add(Page(it["position"].asInt, url, url))
            }
            moreChapters = document["data"]["picture"]["list"]["info"]["has_next_page"].asBoolean
            val nextPageUrl =  client.newCall(buildImageListRequest(id, "position", nextPage)).execute()
            document = gson.fromJson<JsonObject>(nextPageUrl.body()!!.string())
            nextPage++
        }
        return pages
    }

    // Latest, Popular & Search

    override fun latestUpdatesRequest(page: Int): Request = buildAlbumListRequest(page, getSortFilters(7))
    override fun latestUpdatesParse(response: Response): MangasPage = parseAlbumListResponse(response)
    override fun popularMangaParse(response: Response): MangasPage = parseAlbumListResponse(response)
    override fun popularMangaRequest(page: Int): Request = buildAlbumListRequest(page, getSortFilters(0))
    override fun searchMangaParse(response: Response): MangasPage = parseAlbumListResponse(response)
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request = buildAlbumListRequest(
        page,
        filters.let {
            if (it.isEmpty()) getSortFilters(0)
            else it
        },
        query
    )

    override fun chapterListParse(response: Response): List<SChapter> = throw UnsupportedOperationException("Not used")
    override fun pageListParse(response: Response): List<Page> = throw UnsupportedOperationException("Not used")
    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException("Not used")
    //Requets

    override fun mangaDetailsRequest(manga: SManga): Request {
        val id = manga.url.substringAfterLast("_").removeSuffix("/")
        return buildAlbumInfoRequest(id)
    }

    private fun checkManga(id:String):Boolean{
        val mangaCheck = client.newCall(buildAlbumInfoRequest(id)).execute()
        val mangaCheckJson = gson.fromJson<JsonObject>(mangaCheck.body()!!.string())
        with(mangaCheckJson["data"]["album"]["list"]) {
            return this["is_manga"].asBoolean
        }
    }
    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        val id = manga.url.substringAfterLast("_").removeSuffix("/")
        val isManga = checkManga(id)
        if (isManga) {
            return client.newCall(buildAlbumInfoRequest(id))
                .asObservableSuccess()
                .map { response ->
                    singlechapterParse(response, id)
                }
        } else {
            return client.newCall(buildImageListRequest(id, "date_newest", 1))
                .asObservableSuccess()
                .map { response ->
                    chapterListParse(response, id)
                }
        }
    }


    override fun fetchPageList(chapter: SChapter): Observable<List<Page>> {
        when {
            chapter.url.contains("http") -> {
                throw Exception("Stub!")
            }
            else -> {
                return client.newCall(buildImageListRequest(chapter.url, "position", 1))
                    .asObservableSuccess()
                    .map { response ->
                        parsePage(response, chapter.url)
                    }
            }
        }
    }

    // Filters

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
    class RestrictGenresSelectFilter(options: List<SelectFilterOption>) : SelectFilter("Restrict Genres", options)
    class AlbumSizeSelectFilter(options: List<SelectFilterOption>) : SelectFilter("Album Size", options)
    class ContentTypeSelectFilter(options: List<SelectFilterOption>) : SelectFilter("Content Type", options)

    override fun getFilterList(): FilterList = getSortFilters(0)

    private fun getSortFilters(sortState: Int) = FilterList(
        SortBySelectFilter(getSortOptionsFilters(), sortState),
        AlbumTypeSelectFilter(getAlbumTypeFilters()),
        ContentTypeSelectFilter(getContentTypeFilters()),
        AlbumSizeSelectFilter(getAlbumSizeFilters()),
        InterestGroupFilter(getInterestFilters()),
        LanguageGroupFilter(getLanguageFilters()),
        TagGroupFilter(getTagFilters()),
        RestrictGenresSelectFilter(getRestrictGenresFilters()),
        GenreGroupFilter(getGenreFilters())
    )

    private fun getSortOptionsFilters() = listOf(
        SelectFilterOption("Rating - All Time", "rating_all_time"),
        SelectFilterOption("Rating - Last 7 Days", "rating_7_days"),
        SelectFilterOption("Rating - Last 14 Days", "rating_14_days"),
        SelectFilterOption("Rating - Last 30 Days", "rating_30_days"),
        SelectFilterOption("Rating - Last 90 Days", "rating_90_days"),
        SelectFilterOption("Rating - Last Year", "rating_1_year"),
        SelectFilterOption("Date - Newest First", "date_newest"),
        SelectFilterOption("Date - 2021", "date_2021"),
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
        SelectFilterOption("Date - Last Viewed", "date_last_interaction"),
        SelectFilterOption("First Letter - Any", "alpha_any"),
        SelectFilterOption("First Letter - A", "alpha_a"),
        SelectFilterOption("First Letter - B", "alpha_b"),
        SelectFilterOption("First Letter - C", "alpha_c"),
        SelectFilterOption("First Letter - D", "alpha_d"),
        SelectFilterOption("First Letter - E", "alpha_e"),
        SelectFilterOption("First Letter - F", "alpha_f"),
        SelectFilterOption("First Letter - G", "alpha_g"),
        SelectFilterOption("First Letter - H", "alpha_h"),
        SelectFilterOption("First Letter - I", "alpha_i"),
        SelectFilterOption("First Letter - J", "alpha_j"),
        SelectFilterOption("First Letter - K", "alpha_k"),
        SelectFilterOption("First Letter - L", "alpha_l"),
        SelectFilterOption("First Letter - M", "alpha_m"),
        SelectFilterOption("First Letter - N", "alpha_n"),
        SelectFilterOption("First Letter - O", "alpha_o"),
        SelectFilterOption("First Letter - P", "alpha_p"),
        SelectFilterOption("First Letter - Q", "alpha_q"),
        SelectFilterOption("First Letter - R", "alpha_r"),
        SelectFilterOption("First Letter - S", "alpha_s"),
        SelectFilterOption("First Letter - T", "alpha_t"),
        SelectFilterOption("First Letter - U", "alpha_u"),
        SelectFilterOption("First Letter - V", "alpha_v"),
        SelectFilterOption("First Letter - W", "alpha_w"),
        SelectFilterOption("First Letter - X", "alpha_x"),
        SelectFilterOption("First Letter - Y", "alpha_y"),
        SelectFilterOption("First Letter - Z", "alpha_z"),

    )

    private fun getAlbumTypeFilters() = listOf(
        SelectFilterOption("Manga", "manga"),
        SelectFilterOption("All", "<ignore>"),
        SelectFilterOption("Pictures", "pictures")
    )

    private fun getRestrictGenresFilters() = listOf(
        SelectFilterOption("None", "<ignore>"),
        SelectFilterOption("Loose", "loose"),
        SelectFilterOption("Strict", "strict")
    )

    private fun getContentTypeFilters() = listOf(
        SelectFilterOption("All", "<ignore>"),
        SelectFilterOption("Hentai", "0"),
        SelectFilterOption("Non-Erotic", "5"),
        SelectFilterOption("Real People", "6")
    )

    private fun getAlbumSizeFilters() = listOf(
        SelectFilterOption("All", "<ignore>"),
        SelectFilterOption("0-25", "0"),
        SelectFilterOption("0-50", "1"),
        SelectFilterOption("50-100", "2"),
        SelectFilterOption("100-200", "3"),
        SelectFilterOption("200-800", "4"),
        SelectFilterOption("800-3200", "5"),
        SelectFilterOption("3200-12800", "6"),
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
        CheckboxFilterOption("English", "1", false),
        CheckboxFilterOption("Japanese", "2", false),
        CheckboxFilterOption("Spanish", "3", false),
        CheckboxFilterOption("Italian", "4", false),
        CheckboxFilterOption("German", "5", false),
        CheckboxFilterOption("French", "6", false),
        CheckboxFilterOption("Chinese", "8", false),
        CheckboxFilterOption("Korean", "9", false),
        CheckboxFilterOption("Others", "99", false),
        CheckboxFilterOption("Portugese", "100", false),
        CheckboxFilterOption("Thai", "101", false)
    ).filterNot { it.value == lusLang }

    // This is not a full list of Tags
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

    // Query Strings
    private val albumListQuery = """
        query AlbumList(${"$"}input: AlbumListInput!) {
            album {
                list(input: ${"$"}input) {
                    info {...FacetCollectionInfo}
                    items {...AlbumMinimal}
                }
            }
        }
        fragment FacetCollectionInfo on FacetCollectionInfo {
            page has_next_page has_previous_page total_items total_pages items_per_page url_complete
        }
        fragment AlbumMinimal on Album {
            __typename id title labels description created modified like_status moderation_status number_of_favorites number_of_dislikes number_of_pictures number_of_animated_pictures number_of_duplicates slug is_manga url download_url permissions created_by { id url name display_name user_title avatar { url size } } cover { width height size url } language { id title url } tags { category text url count } genres { id title slug url }
        }
    """.trimIndent()

    val albumInfoQuery = """
        query AlbumGet(${"$"}id: ID!) {
            album {
                get(id: ${"$"}id) {
                    ... on Album { ...AlbumStandard }
                    ... on MutationError {
                        errors {
                            code message
                         }
                    }
                }
            }
        }
        fragment AlbumStandard on Album {
            __typename id title labels description created modified like_status number_of_favorites number_of_dislikes rating moderation_status marked_for_deletion marked_for_processing number_of_pictures number_of_animated_pictures number_of_duplicates slug is_manga url download_url permissions cover { width height size url } created_by { id url name display_name user_title avatar { url size } } content { id title url } language { id title url } tags { category text url count } genres { id title slug url } audiences { id title url url } last_viewed_picture { id position url } is_featured featured_date featured_by { id url name display_name user_title avatar { url size } }
        }
    """.trimIndent()

    val imageListQuery = """
        query AlbumListOwnPictures(%24input%3A PictureListInput!) {
	        picture {
		        list(input%3A %24input) {
			        info { ...FacetCollectionInfo }
			        items { ...PictureStandardWithoutAlbum }
		        }
	        }
        }
        fragment FacetCollectionInfo on FacetCollectionInfo {
	        page has_next_page has_previous_page total_items total_pages items_per_page url_complete
        }
        fragment PictureStandardWithoutAlbum on Picture {
	        __typename id title description created like_status number_of_comments number_of_favorites moderation_status width height resolution aspect_ratio url_to_original url_to_video is_animated position tags { category text url } permissions url thumbnails { width height size url }
        }
    """.trimIndent()

}
