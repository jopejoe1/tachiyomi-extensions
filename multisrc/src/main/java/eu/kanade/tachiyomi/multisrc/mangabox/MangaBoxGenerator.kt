package eu.kanade.tachiyomi.multisrc.mangabox

import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class MangaBoxGenerator : ThemeSourceGenerator {

    override val themePkg = "mangabox"

    override val themeClass = "MangaBox"

    override val baseVersionCode: Int = 1

    override val sources = listOf(
        SingleLang("Mangakakalot", "https://mangakakalot.com", "en"),
        SingleLang("Manganelo", "https://manganelo.com", "en"),
        SingleLang("Mangabat", "https://m.mangabat.com", "en", overrideVersionCode = 4),
        SingleLang("Mangakakalots (unoriginal)", "https://mangakakalots.com", "en", className = "Mangakakalots", pkgName = "mangakakalots"),
        SingleLang("Mangairo", "https://h.mangairo.com", "en", overrideVersionCode = 3),
        SingleLang("Mangakakalot.tv (unoriginal)", "https://ww.mangakakalot.tv", "en", className = "MangakakalotTv", pkgName = "mangakakalottv"),
        SingleLang("Mangakakalotz (unoriginal)", "https://chapter.mangakakalotz.com/", "en", className = "Mangakakalotz", pkgName = "mangakakalotz"),
        SingleLang("Mangakakalot/Mangarock (unoriginal)", "https://mangarock.herokuapp.com/", "en", className = "MangakakalotMangarock", pkgName = "mangakakalotmangarock"), //url says mangarock title says Mangakakalot
        SingleLang("Mangakakalot.xyz (unoriginal)", "http://mangakakalot.xyz/", "en", className = "Mangakakalotxyz", pkgName = "mangakakalotxyz"),
        SingleLang("Mangakakalot.cloud (unoriginal)", "http://mangakakalot.cloud/", "en", className = "MangakakalotCloud", pkgName = "mangakakalotcloud"),
        SingleLang("Mangakakalot.top (unoriginal)", "http://mangakakalot.top/", "en", className = "MangakakalotTop", pkgName = "mangakakalottop"),
        SingleLang("Mangakakalot.live (unoriginal)", "http://mangakakalot.live/", "en", className = "MangakakalotLive", pkgName = "mangakakalotlive"),
        SingleLang("Mangakakalot.city (unoriginal)", "http://mangakakalot.city/", "en", className = "MangakakalotCity", pkgName = "mangakakalotcity")
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MangaBoxGenerator().createAll()
        }
    }
}
