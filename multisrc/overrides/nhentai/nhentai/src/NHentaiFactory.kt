package eu.kanade.tachiyomi.extension.all.nhentai

import eu.kanade.tachiyomi.multisrc.nhentai.NHentai
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class NHentaiFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        NHentaiEN(),
        NHentaiJA(),
        NHentaiZH(),
        NHentaiALL(),
    )
}
class NHentaiEN : NHentai("NHentai", "https://nhentai.net", "en")
class NHentaiJA : NHentai("NHentai", "https://nhentai.net", "ja")
class NHentaiZH : NHentai("NHentai", "https://nhentai.net", "zh")
class NHentaiALL : NHentai("NHentai", "https://nhentai.net", "all")
