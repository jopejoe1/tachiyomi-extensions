package eu.kanade.tachiyomi.extension.bg.utsukushii

import eu.kanade.tachiyomi.multisrc.mmrcms.MMRCMS
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.util.asJsoup
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class Utsukushii : MMRCMS("Utsukushii", "https://manga.utsukushii-bg.com", "bg", """{"language":"bg","name":"Utsukushii","base_url":"https://manga.utsukushii-bg.com","supports_latest":true,"isNsfw":false,"item_url":"https://manga.utsukushii-bg.com/manga/","categories":[{"id":"1","name":"Екшън"},{"id":"2","name":"Приключенски"},{"id":"3","name":"Комедия"},{"id":"4","name":"Драма"},{"id":"5","name":"Фентъзи"},{"id":"6","name":"Исторически"},{"id":"7","name":"Ужаси"},{"id":"8","name":"Джосей"},{"id":"9","name":"Бойни изкуства"},{"id":"10","name":"Меха"},{"id":"11","name":"Мистерия"},{"id":"12","name":"Самостоятелна/Пилотна глава"},{"id":"13","name":"Психологически"},{"id":"14","name":"Романтика"},{"id":"15","name":"Училищни"},{"id":"16","name":"Научна фантастика"},{"id":"17","name":"Сейнен"},{"id":"18","name":"Шоджо"},{"id":"19","name":"Реализъм"},{"id":"20","name":"Спорт"},{"id":"21","name":"Свръхестествено"},{"id":"22","name":"Трагедия"},{"id":"23","name":"Йокаи"},{"id":"24","name":"Паралелна вселена"},{"id":"25","name":"Супер сили"},{"id":"26","name":"Пародия"},{"id":"27","name":"Шонен"}],"tags":"null"}""") {
    override fun popularMangaRequest(page: Int): Request {
        return GET("$baseUrl/manga-list", headers)
    }
}
