package eu.kanade.tachiyomi.extension.all.foxhentai

import eu.kanade.tachiyomi.multisrc.nhentai.NHentai
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class FoxHentaiFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        FoxHentaiEN(),
        FoxHentaiJA(),
        FoxHentaiZH(),
        FoxHentaiALL(),
    )
}
class FoxHentaiEN : NHentai("FoxHentai", "https://ja.foxhentai.com", "en")
class FoxHentaiJA : NHentai("FoxHentai", "https://ja.foxhentai.com", "ja")
class FoxHentaiZH : NHentai("FoxHentai", "https://ja.foxhentai.com", "zh")
class FoxHentaiALL : NHentai("FoxHentai", "https://ja.foxhentai.com", "all")
