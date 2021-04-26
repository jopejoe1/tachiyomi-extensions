package eu.kanade.tachiyomi.extension.all.nyahentaime

import eu.kanade.tachiyomi.multisrc.nhentai.NHentai
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class NyaHentaiMeFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        NyaHentaiMeEN(),
        NyaHentaiMeJA(),
        NyaHentaiMeZH(),
        NyaHentaiMeALL(),
    )
}
class NyaHentaiMeEN : NHentai("NyaHentai.me", "https://ja.nyahentai.me", "en")
class NyaHentaiMeJA : NHentai("NyaHentai.me", "https://ja.nyahentai.me", "ja")
class NyaHentaiMeZH : NHentai("NyaHentai.me", "https://ja.nyahentai.me", "zh")
class NyaHentaiMeALL : NHentai("NyaHentai.me", "https://ja.nyahentai.me", "all")
