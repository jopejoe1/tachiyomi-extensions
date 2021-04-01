package eu.kanade.tachiyomi.multisrc.luscious

import generator.ThemeSourceData.MultiLang
import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class LusciousGenerator : ThemeSourceGenerator {

    override val themePkg = "luscious"

    override val themeClass = "Luscious"

    override val baseVersionCode: Int = 1

    override val sources = listOf(
        MultiLang("Luscious", "https://www.luscious.net", listOf("en","ja", "es", "it", "de", "fr", "zh", "ko", "other", "pt", "th"), className = "LusciousFactory"),
        MultiLang("Luscious (Members)", "https://members.luscious.net", listOf("en","ja", "es", "it", "de", "fr", "zh", "ko", "other", "pt", "th"), className = "LusciousMembersFactory", pkgName = "lusciousmembersfactory"),
        MultiLang("Luscious (API)", "https://api.luscious.net", listOf("en","ja", "es", "it", "de", "fr", "zh", "ko", "other", "pt", "th"), className = "LusciousAPIFactory", pkgName = "lusciousapifactory"),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            LusciousGenerator().createAll()
        }
    }
}
