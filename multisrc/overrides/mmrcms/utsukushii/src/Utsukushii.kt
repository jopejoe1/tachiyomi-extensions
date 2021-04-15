package eu.kanade.tachiyomi.extension.bg.utsukushii

import eu.kanade.tachiyomi.multisrc.mmrcms.MMRCMS

class Utsukushii : MMRCMS("Utsukushii", "https://manga.utsukushii-bg.com", "bg", source) {
    private const val source = """{"language":"bg","name":"Utsukushii","base_url":"https://manga.utsukushii-bg.com","supports_latest":true,"isNsfw":false,"item_url":"https://manga.utsukushii-bg.com/manga/","categories":[{"id":"1","name":"Екшън"},{"id":"2","name":"Приключенски"},{"id":"3","name":"Комедия"},{"id":"4","name":"Драма"},{"id":"5","name":"Фентъзи"},{"id":"6","name":"Исторически"},{"id":"7","name":"Ужаси"},{"id":"8","name":"Джосей"},{"id":"9","name":"Бойни изкуства"},{"id":"10","name":"Меха"},{"id":"11","name":"Мистерия"},{"id":"12","name":"Самостоятелна/Пилотна глава"},{"id":"13","name":"Психологически"},{"id":"14","name":"Романтика"},{"id":"15","name":"Училищни"},{"id":"16","name":"Научна фантастика"},{"id":"17","name":"Сейнен"},{"id":"18","name":"Шоджо"},{"id":"19","name":"Реализъм"},{"id":"20","name":"Спорт"},{"id":"21","name":"Свръхестествено"},{"id":"22","name":"Трагедия"},{"id":"23","name":"Йокаи"},{"id":"24","name":"Паралелна вселена"},{"id":"25","name":"Супер сили"},{"id":"26","name":"Пародия"},{"id":"27","name":"Шонен"}],"tags":"null"}"""

    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/manga-list", headers)
    }
    private fun internalMangaParse(response: Response): MangasPage {
        val document = response.asJsoup()

        val internalMangaSelector = "div.content div.col-sm-6"
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
}
