package eu.kanade.tachiyomi.multisrc.webtoons

import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceData.MultiLang
import generator.ThemeSourceGenerator

class WebtoonsTranslationGenerator : ThemeSourceGenerator {
    override val themePkg = "webtoons"

    override val themeClass = "WebtoonsTranslation"

    override val baseVersionCode: Int = 1

    override val sources = listOf(
        MultiLang("Webtoons Translation", "https://www.webtoons.com", listOf("en", "zh", "zh-hant", "th", "id", "fr", "vi", "ru", "ar", "fil", "de", "hi", "it", "ja", "pt-br", "tr", "ms", "pl", "pt", "bg", "da", "nl", "ro", "mn", "el", "lt", "cs", "sv", "bn", "fa", "uk", "es"), className = "WebtoonsTranslationFactory", pkgName = "webtoonstranslation"),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            WebtoonsTranslationGenerator().createAll()
        }
    }
}
