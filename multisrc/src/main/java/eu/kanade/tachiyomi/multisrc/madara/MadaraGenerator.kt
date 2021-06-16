package eu.kanade.tachiyomi.multisrc.madara

import generator.ThemeSourceData.MultiLang
import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class MadaraGenerator : ThemeSourceGenerator {

    override val themePkg = "madara"

    override val themeClass = "Madara"

    override val baseVersionCode: Int = 6

    override val sources = listOf(
        MultiLang("Leviatan Scans", "https://leviatanscans.com", listOf("en", "es"), className = "LeviatanScansFactory", overrideVersionCode = 4),
        MultiLang("Manhwa18.cc", "https://manhwa18.cc", listOf("en", "ko", "all"), isNsfw = true, className = "Manhwa18CcFactory", pkgName = "manhwa18cc"),
        SingleLang("1st Kiss Manhua", "https://1stkissmanhua.com", "en", className = "FirstKissManhua", overrideVersionCode = 1),
        SingleLang("1st Kiss", "https://1stkissmanga.com", "en", className = "FirstKissManga", overrideVersionCode = 2),
        SingleLang("1st Kiss Manga.love", "https://1stkissmanga.love", "en", className = "FirstKissMangaLove"),
        SingleLang("1stKissManga.Club", "https://1stkissmanga.club", "en", className = "FirstKissMangaClub"),
        SingleLang("247Manga", "https://247manga.com", "en", className = "Manga247"),
        SingleLang("24hRomance", "https://24hromance.com", "en", className = "Romance24h"),
        SingleLang("365Manga", "https://365manga.com", "en", className = "ThreeSixtyFiveManga", overrideVersionCode = 1),
        SingleLang("AYATOON", "https://ayatoon.com", "tr", overrideVersionCode = 1),
        SingleLang("Adonis Fansub", "https://manga.adonisfansub.com", "tr", overrideVersionCode = 1),
        SingleLang("Agent of Change Translations", "https://aoc.moe", "en", overrideVersionCode = 1),
        SingleLang("AkuManga", "https://akumanga.com", "ar"),
        SingleLang("AllPornComic", "https://allporncomic.com", "en", isNsfw = true),
        SingleLang("Hiperdex", "https://hiperdex2.com", "en", isNsfw = true, overrideVersionCode = 1),
        SingleLang("Mangceh", "https://mangceh.me", "id", isNsfw = true, overrideVersionCode = 1),
        SingleLang("InfraFandub", "https://infrafandub.xyz", "es"),
        SingleLang("Hades no Fansub", "https://mangareaderpro.com", "es"),
        SingleLang("Hades no Fansub H", "https://h.mangareaderpro.com", "es", isNsfw = true),
        SingleLang("Mangaka3rb", "https://mangaka3rb.com", "ar"),
        SingleLang("Wekomic", "https://wekomic.com", "en"),
    )

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            MadaraGenerator().createAll()
        }
    }
}
