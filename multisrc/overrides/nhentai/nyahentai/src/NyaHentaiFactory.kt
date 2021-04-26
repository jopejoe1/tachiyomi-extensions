package eu.kanade.tachiyomi.extension.all.nyahentai

import eu.kanade.tachiyomi.multisrc.nhentai.NHentai
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class NyaHentaiFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        NyaHentaiEN(),
        NyaHentaiJA(),
        NyaHentaiZH(),
        NyaHentaiALL(),
    )
}
class NyaHentaiEN : NHentai("NyaHentai", "https://nyahentai.com", "en")
class NyaHentaiJA : NHentai("NyaHentai", "https://nyahentai.com", "ja")
class NyaHentaiZH : NHentai("NyaHentai", "https://nyahentai.com", "zh")
class NyaHentaiALL : NHentai("NyaHentai", "https://nyahentai.com", "all")
