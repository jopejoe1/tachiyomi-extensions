package eu.kanade.tachiyomi.multisrc.mmrcms

import android.net.Uri
import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import okhttp3.Request

abstract class MMRCMSMultiLang(
    override val name: String,
    override val baseUrl: String,
    override val lang: String,
    val jsonInfo: String = MMRCMSJsonGenHS(name, baseUrl, lang).generateJson(),
    ) : MMRCMS(name, baseUrl, lang, jsonInfo){
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
    override val parser = JsonParser()
    override val jsonObject = parser.parse(jsonInfo) as JsonObject
    override val supportsLatest = jsonObject["supports_latest"].bool
    override val itemUrl = jsonObject["item_url"].string

    val categoriesMappings =  if (jsonObject["categories"].isJsonArray) {
        mapToPairsHS(jsonObject["categories"].asJsonArray)
    } else {
        emptyList<CheckBoxs>()
    }

    val languagesMappings =  if (jsonObject["languages"].isJsonArray) {
        mapToPairsHS(jsonObject["languages"].asJsonArray)
    } else {
        emptyList<CheckBoxs>()
    }

    val statusMappings =  if (jsonObject["status"].isJsonArray) {
        mapToPairsHS(jsonObject["status"].asJsonArray)
    } else {
        emptyList<CheckBoxs>()
    }

    val charactersMappings =  if (jsonObject["characters"].isJsonArray) {
        mapToPairsHS(jsonObject["characters"].asJsonArray)
    } else {
        emptyList<CheckBoxs>()
    }

    val parodiesMappings =  if (jsonObject["parodies"].isJsonArray) {
        mapToPairsHS(jsonObject["parodies"].asJsonArray)
    } else {
        emptyList<CheckBoxs>()
    }

    var tagsMappings =  if (jsonObject["tags"].isJsonArray) {
        mapToPairsHS(jsonObject["tags"].asJsonArray)
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
    fun mapToPairsHS(array: JsonArray): List<CheckBoxs> = array.map {
        it as JsonObject

        CheckBoxs(it["id"].string, it["name"].string)
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
}
