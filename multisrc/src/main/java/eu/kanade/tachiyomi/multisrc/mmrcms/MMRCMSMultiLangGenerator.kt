package eu.kanade.tachiyomi.multisrc.mmrcms

import generator.ThemeSourceData.MultiLang
import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class MMRCMSMultiLangGenerator : ThemeSourceGenerator {

    override val themePkg = "mmrcms"

    override val themeClass = "MMRCMSMultiLang"

    override val baseVersionCode: Int = 3

    override val sources = listOf(

        MultiLang("HentaiShark", "https://www.hentaishark.com", listOf("en", "ja", "zh", "de", "nl", "ko", "cz", "eo", "mn", "ar", "sk", "la", "ua", "ceb", "tl", "fi", "bg", "tr"), isNsfw = true, className = "HentaiSharkFactory", pkgName = "hentaishark"),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MMRCMSMultiLangGenerator().createAll()
        }
    }
}
