package eu.kanade.tachiyomi.extension.es.mangaspw

import eu.kanade.tachiyomi.multisrc.mmrcms.MMRCMS
import android.util.Base64
import java.net.URLDecoder

class MangasPw : MMRCMS("Mangas.pw", "https://mangas.in", "es", source) {

    private const val source = """{"language":"es","name":"Mangas.pw","base_url":"https://mangas.in","supports_latest":true,"isNsfw":false,"item_url":"https://mangas.in/manga/","categories":[{"id":"1","name":"Action"},{"id":"2","name":"Adventure"},{"id":"3","name":"Comedy"},{"id":"4","name":"Doujinshi"},{"id":"5","name":"Drama"},{"id":"6","name":"Ecchi"},{"id":"7","name":"Fantasy"},{"id":"8","name":"Gender Bender"},{"id":"9","name":"Harem"},{"id":"10","name":"Historical"},{"id":"11","name":"Horror"},{"id":"12","name":"Josei"},{"id":"13","name":"Martial Arts"},{"id":"14","name":"Mature"},{"id":"15","name":"Mecha"},{"id":"16","name":"Mystery"},{"id":"17","name":"One Shot"},{"id":"18","name":"Psychological"},{"id":"19","name":"Romance"},{"id":"20","name":"School Life"},{"id":"21","name":"Sci-fi"},{"id":"22","name":"Seinen"},{"id":"23","name":"Shoujo"},{"id":"24","name":"Shoujo Ai"},{"id":"25","name":"Shounen"},{"id":"26","name":"Shounen Ai"},{"id":"27","name":"Slice of Life"},{"id":"28","name":"Sports"},{"id":"29","name":"Supernatural"},{"id":"30","name":"Tragedy"},{"id":"31","name":"Yaoi"},{"id":"32","name":"Yuri"},{"id":"33","name":"Hentai"},{"id":"34","name":"Smut"}],"tags":"null"}"""

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url: Uri.Builder
        url = Uri.parse("$baseUrl/search")!!.buildUpon()
        url.appendQueryParameter("q", query)
        return GET(url.toString(), headers)
    }

    override fun searchMangaParse(response: Response): MangasPage {
        return if (listOf("query", "q").any { it in response.request().url().queryParameterNames() }) {
            // If a search query was specified, use search instead!
            val jsonArray = jsonParser.parse(response.body()!!.string()).let {
                it.array
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

    private fun nullableChapterFromElement(element: Element): SChapter? {
        val chapter = SChapter.create()

        try {
            val titleWrapper = element.select("i a").first()
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

    override fun pageListParse(response: Response) = response.asJsoup().select("#all > .img-responsive")
        .mapIndexed { i, e ->
            var url = (if (e.hasAttr("data-src")) e.attr("abs:data-src") else e.attr("abs:src")).trim()

            // Mangas.pw encodes some of their urls, decode them
            if (!url.contains(".")) {
                url = Base64.decode(url.substringAfter("//"), Base64.DEFAULT).toString(Charsets.UTF_8).substringBefore("=")
                url = URLDecoder.decode(url, "UTF-8")
            }

            Page(i, "", url)
        }

    override fun getFilterList(): FilterList {
        return FilterList()
    }
}
