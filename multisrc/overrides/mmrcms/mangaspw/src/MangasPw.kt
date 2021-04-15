package eu.kanade.tachiyomi.extension.es.mangaspw

import eu.kanade.tachiyomi.multisrc.mmrcms.MMRCMS
import android.util.Base64
import java.net.URLDecoder
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.network.GET
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import eu.kanade.tachiyomi.util.asJsoup

class MangasPw : MMRCMS("Mangas.pw", "https://mangas.in", "es", """{"language":"es","name":"Mangas.pw","base_url":"https://mangas.in","supports_latest":true,"isNsfw":false,"item_url":"https://mangas.in/manga/","categories":[{"id":"1","name":"Action"},{"id":"2","name":"Adventure"},{"id":"3","name":"Comedy"},{"id":"4","name":"Doujinshi"},{"id":"5","name":"Drama"},{"id":"6","name":"Ecchi"},{"id":"7","name":"Fantasy"},{"id":"8","name":"Gender Bender"},{"id":"9","name":"Harem"},{"id":"10","name":"Historical"},{"id":"11","name":"Horror"},{"id":"12","name":"Josei"},{"id":"13","name":"Martial Arts"},{"id":"14","name":"Mature"},{"id":"15","name":"Mecha"},{"id":"16","name":"Mystery"},{"id":"17","name":"One Shot"},{"id":"18","name":"Psychological"},{"id":"19","name":"Romance"},{"id":"20","name":"School Life"},{"id":"21","name":"Sci-fi"},{"id":"22","name":"Seinen"},{"id":"23","name":"Shoujo"},{"id":"24","name":"Shoujo Ai"},{"id":"25","name":"Shounen"},{"id":"26","name":"Shounen Ai"},{"id":"27","name":"Slice of Life"},{"id":"28","name":"Sports"},{"id":"29","name":"Supernatural"},{"id":"30","name":"Tragedy"},{"id":"31","name":"Yaoi"},{"id":"32","name":"Yuri"},{"id":"33","name":"Hentai"},{"id":"34","name":"Smut"}],"tags":"null"}""") {

    override fun searchMangaRequest(page: Int, query: String, filters: FilterList): Request {
        val url: Uri.Builder
        url = Uri.parse("$baseUrl/search")!!.buildUpon()
        url.appendQueryParameter("q", query)
        return GET(url.toString(), headers)
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
