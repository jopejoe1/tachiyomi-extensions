package eu.kanade.tachiyomi.multisrc.nhentai

import generator.ThemeSourceData.MultiLang
import generator.ThemeSourceGenerator

class NHentaiGenerator : ThemeSourceGenerator {

    override val themePkg = "nhentai"

    override val themeClass = "NHentai"

    override val baseVersionCode: Int = 1

    override val sources = listOf(
        MultiLang("NHentai", "https://nhentai.net", listOf("en","ja", "zh", "all"), isNsfw = true, className = "NHentaiFactory", overrideVersionCode = 28),
        MultiLang("NyaHentai", "https://nyahentai.com", listOf("en","ja", "zh", "all"), isNsfw = true, className = "NyaHentaiFactory", overrideVersionCode = 3),
        MultiLang("NyaHentai.site", "https://nyahentai.site", listOf("en","ja", "zh", "all"), isNsfw = true, className = "NyaHentaiSiteFactory", pkgName = "nyahentaisite"),
        MultiLang("NyaHentai.me", "https://ja.nyahentai.me", listOf("en","ja", "zh", "all"), isNsfw = true, className = "NyaHentaiMeFactory", pkgName = "nyahentaime"),
        MultiLang("QQHentai", "https://zhb.qqhentai.com", listOf("en","ja", "zh", "all"), isNsfw = true, className = "QQHentaiFactory"),
        MultiLang("FoxHentai", "https://ja.foxhentai.com", listOf("en","ja", "zh", "all"), isNsfw = true, className = "FoxHentaiFactory"),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            NHentaiGenerator().createAll()
        }
    }
}
