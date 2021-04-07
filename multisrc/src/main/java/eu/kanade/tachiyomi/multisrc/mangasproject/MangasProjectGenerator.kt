package eu.kanade.tachiyomi.multisrc.mangasproject

import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class MangasProjectGenerator : ThemeSourceGenerator {

    override val themePkg = "mangabox"

    override val themeClass = "MangasProject"

    override val baseVersionCode: Int = 1

    override val sources = listOf(
        //SingleLang("Leitor.net", "https://leitor.net", "pt-br", className = "LeitorNet"),
        SingleLang("Mangá Livre", "https://mangalivre.net", "pt-br", className = "MangaLivre"),
        SingleLang("Toonei", "https://toonei.com", "pt-br"),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MangasProjectGenerator().createAll()
        }
    }
}
