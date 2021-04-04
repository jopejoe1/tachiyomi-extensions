package eu.kanade.tachiyomi.extension.en.readattackontitanshingekinokyojinmanga

import eu.kanade.tachiyomi.multisrc.mangacatalog.MangaCatalog
import eu.kanade.tachiyomi.source.model.MangasPage
import eu.kanade.tachiyomi.util.asJsoup

class ReadAttackOnTitanShingekiNoKyojinManga : MangaCatalog("Read Hunter x Hunter Manga Online", "https://ww2.readhxh.com", "en") {
    override val sourceList = listOf(
        Pair("Shingeki No Kyojin", "$baseUrl/manga/shingeki-no-kyojin/"),
        Pair("Colored", "$baseUrl/manga/shingeki-no-kyojin-colored/"),
        Pair("Before the Fall", "$baseUrl/manga/shingeki-no-kyojin-before-the-fall/"),
        Pair("Lost Girls", "$baseUrl/manga/shingeki-no-kyojin-lost-girls/"),
        Pair("No Regrets", "$baseUrl/manga/attack-on-titan-no-regrets/"),
        Pair("Junior High", "$baseUrl/manga/attack-on-titan-junior-high/"),
        Pair("Harsh Mistress", "$baseUrl/manga/attack-on-titan-harsh-mistress-of-the-city/"),
        Pair("Anthology", "$baseUrl/manga/attack-on-titan-anthology/"),
        Pair("Art Book", "$baseUrl/manga/attack-on-titan-exclusive-art-book/"),
        Pair("Spoof", "$baseUrl/manga/spoof-on-titan/"),
        Pair("Guidebook", "$baseUrl/manga/attack-on-titan-guidebook-inside-outside/"),
        Pair("No Regrets Colored", "$baseUrl/manga/attack-on-titan-no-regrets-colored/"),
    ).sortedBy { it.first }.distinctBy { it.second }
}
