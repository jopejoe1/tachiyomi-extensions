package eu.kanade.tachiyomi.multisrc.webtoons

import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceData.MultiLang
import generator.ThemeSourceGenerator

class WebtoonsDefaultGenerator : ThemeSourceGenerator {

    override val themePkg = "webtoons"

    override val themeClass = "WebtoonsDefault"

    override val baseVersionCode: Int = 1

    override val sources = listOf(
        MultiLang("Webtoons", "https://www.webtoons.com", listOf("en", "fr", "es", "id", "th", "zh")),
        SingleLang("Dongman Manhua", "https://www.dongmanmanhua.cn", "zh")
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            WebtoonsDefaultGenerator().createAll()
        }
    }
}
