package eu.kanade.tachiyomi.extension.all.leagueoflegends

import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.get
import com.google.gson.Gson
import com.google.gson.JsonObject
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.asObservableSuccess
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource
import java.text.SimpleDateFormat
import okhttp3.Request
import okhttp3.Response
import rx.Observable

open class LeagueOfLegends(
    override val lang: String,
    siteLang: String
) : HttpSource()  {
    override val name: String = "League of Legends"
    override val baseUrl: String = "https://universe.leagueoflegends.com"
    val apiUrl = "https://universe-meeps.leagueoflegends.com/v1/$siteLang/comics"
    val imgUrl = "https://universe-comics.leagueoflegends.com/comics/$siteLang"
    override val supportsLatest = false
    val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    // Popular

    override fun popularMangaParse(response: Response): MangasPage {
        val mangas = mutableListOf<SManga>()
        val data = gson.fromJson<JsonObject>(response.body!!.string())
        with(data["sections"]) {
            this["series"]["data"].asJsonArray.map{
                val manga = SManga.create()
                val title = it["title"].asString
                val sectionTitle = it["section-title"].asString
                val subTitle = it["subtitle"].asString

                if (title != sectionTitle && sectionTitle != null){
                    manga.title = "$title: $sectionTitle"
                }else{
                    manga.title = title
                }
                if (subTitle != null){
                    manga.title = "${manga.title} - $subTitle"
                }
                manga.genre = "Comic Series"
                manga.description = it["description"].asString
                manga.thumbnail_url = it["background"]["uri"].asString
                manga.url = it["url"].asString

                mangas.add(manga)
            }
            this["one-shots"]["data"].asJsonArray.map{
                val manga = SManga.create()
                val title = it["title"].asString
                val sectionTitle = it["section-title"].asString
                val subTitle = it["subtitle"].asString

                if (title != sectionTitle && sectionTitle != null){
                    manga.title = "$title: $sectionTitle"
                }else{
                    manga.title = title
                }
                if (subTitle != null){
                    manga.title = "${manga.title} - $subTitle"
                }
                manga.genre = "One Shots"
                manga.description = it["description"].asString
                manga.thumbnail_url = it["background"]["uri"].asString
                manga.url = it["url"].asString

                mangas.add(manga)
            }
        }
        return MangasPage(mangas, false)
    }

    override fun popularMangaRequest(page: Int): Request {
        return GET("$apiUrl/index.json")
    }

    override fun mangaDetailsParse(response: Response): SManga {
        return SManga.create()
    }

    override fun fetchChapterList(manga: SManga): Observable<List<SChapter>> {
        return when (manga.genre) {
            "Comic Series" -> {
                val slug = manga.url.substringAfterLast("comic/")
                client.newCall(GET("$apiUrl/$slug/index.json", headers))
                    .asObservableSuccess()
                    .map { parseSeries(it) }
            }
            "One Shots" -> {
                val slug = manga.url.substringAfterLast("comic/")
                client.newCall(GET("$imgUrl/$slug/index.json", headers))
                    .asObservableSuccess()
                    .map { listOf(parseIssue(it)) }
            }
            else -> Observable.just(emptyList())
        }
    }

    private fun parseSeries(response: Response): List<SChapter>{
        val chapters = mutableListOf<SChapter>()
        val data = gson.fromJson<JsonObject>(response.body!!.string())
        with(data){
            this["issues"].asJsonArray.map{
                val slug = it["url"].asString.substringAfterLast("comic/")
                val chapter = parseIssue(client.newCall(GET("$imgUrl/$slug/index.json", headers)).execute())
                chapter.chapter_number = it["index"].asFloat
                chapters.add(chapter)
            }
        }
        return chapters
    }

    private fun parseIssue(response: Response): SChapter{
        val data = gson.fromJson<JsonObject>(response.body!!.string())
        with(data){
            val chapter = SChapter.create()
            val date = dateFormat.parse(this["staging-date"].asString)
            chapter.name = this["title"].asString
            chapter.chapter_number = 1F
            chapter.url = this["id"].asString
            chapter.date_upload = date?.time ?: 0L
            return chapter
            }
        }



    // Pages

    override fun pageListRequest(chapter: SChapter): Request {
        return GET("$imgUrl/${chapter.url}/index.json")
    }

    override fun pageListParse(response: Response): List<Page> {
        val pages = mutableListOf<Page>()
        val data = gson.fromJson<JsonObject>(response.body!!.string())
        with(data){
            this["desktop-pages"].asJsonArray.mapIndexed(){ index, _ ->
                this[index].asJsonArray.map{ it ->
                    val url = it["uri"].asString
                    val size = pages.size
                    pages.add(Page(size + 1 ,url, url))
                }
            }
        }
        return pages
    }

    // Not used

    override fun imageUrlParse(response: Response): String = throw UnsupportedOperationException("Not used")
    override fun latestUpdatesParse(response: Response): MangasPage = throw UnsupportedOperationException("Not used")
    override fun latestUpdatesRequest(page: Int): Request = throw UnsupportedOperationException("Not used")
    override fun chapterListParse(response: Response): List<SChapter> = throw UnsupportedOperationException("Not used")
    override fun searchMangaParse(response: Response): MangasPage = throw UnsupportedOperationException("Not used")
    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request = throw UnsupportedOperationException("Not used")
}
