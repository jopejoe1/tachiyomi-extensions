package eu.kanade.tachiyomi.extension.all.qqhentai

import eu.kanade.tachiyomi.multisrc.nhentai.NHentai
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class QQHentaiFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        QQHentaiEN(),
        QQHentaiJA(),
        QQHentaiZH(),
        QQHentaiALL(),
    )
}
class QQHentaiEN : NHentai("QQHentai", "https://zhb.qqhentai.com", "en")
class QQHentaiJA : NHentai("QQHentai", "https://zhb.qqhentai.com", "ja")
class QQHentaiZH : NHentai("QQHentai", "https://zhb.qqhentai.com", "zh")
class QQHentaiALL : NHentai("QQHentai", "https://zhb.qqhentai.com", "all")
