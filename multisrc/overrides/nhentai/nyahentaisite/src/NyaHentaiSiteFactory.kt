package eu.kanade.tachiyomi.extension.all.nyahentaisite

import eu.kanade.tachiyomi.multisrc.nhentai.NHentai
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class NyaHentaiSiteFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        NyaHentaiSiteEN(),
        NyaHentaiSiteJA(),
        NyaHentaiSiteZH(),
        NyaHentaiSiteALL(),
    )
}
class NyaHentaiSiteEN : NHentai("NyaHentai.site", "https://nyahentai.site", "en")
class NyaHentaiSiteJA : NHentai("NyaHentai.site", "https://nyahentai.site", "ja")
class NyaHentaiSiteZH : NHentai("NyaHentai.site", "https://nyahentai.site", "zh")
class NyaHentaiSiteALL : NHentai("NyaHentai.site", "https://nyahentai.site", "all")
