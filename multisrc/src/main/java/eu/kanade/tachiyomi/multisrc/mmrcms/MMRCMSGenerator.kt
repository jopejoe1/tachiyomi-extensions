package eu.kanade.tachiyomi.multisrc.mmrcms

import generator.ThemeSourceData
import generator.ThemeSourceData.SingleLang
import generator.ThemeSourceGenerator

class MMRCMSGenerator : ThemeSourceGenerator {

    override val themePkg = "mmrcms"

    override val themeClass = "MMRCMS"

    override val baseVersionCode: Int = 4

    override val sources = sourceList

    companion object {
        val sourceList = listOf(
            SingleLang("مانجا اون لاين", "https://onma.me", "ar", className = "onma"),
            SingleLang("Read Comics Online", "https://readcomicsonline.ru", "en"),
            SingleLang("Fallen Angels", "https://manga.fascans.com", "en"),
            SingleLang("Zahard", "https://zahard.top", "en"),
            SingleLang("Manhwas Men", "https://manhwas.men", "en"),
            SingleLang("Scan FR", "https://www.scan-fr.cc", "fr"),
            SingleLang("Scan VF", "https://www.scan-vf.net", "fr"),
            SingleLang("Scan OP", "https://scan-op.cc", "fr"),
            SingleLang("Komikid", "https://www.komikid.com", "id"),
            SingleLang("Nikushima", "http://azbivo.webd.pro", "pl"),
            SingleLang("MangaHanta", "http://mangahanta.com", "tr"),
            SingleLang("Fallen Angels Scans", "https://truyen.fascans.com", "vi"),
            SingleLang("LeoManga", "https://leomanga.me", "es"),
            SingleLang("submanga", "https://submanga.io", "es"),
            SingleLang("Mangadoor", "https://mangadoor.com", "es"),
            SingleLang("Mangas.pw", "https://mangas.in", "es", className = "MangasPw"),
            SingleLang("Utsukushii", "https://manga.utsukushii-bg.com", "bg"),
            SingleLang("Phoenix-Scans", "https://phoenix-scans.pl", "pl", className = "PhoenixScans"),
            SingleLang("Puzzmos", "https://puzzmos.com", "tr"),
            SingleLang("Scan-1", "https://wwv.scan-1.com", "fr", className = "ScanOne"),
            SingleLang("Lelscan-VF", "https://lelscan-vf.co", "fr", className = "LelscanVF"),
            SingleLang("Komik Manga", "https://adm.komikmanga.com", "id"),
            SingleLang("Mangazuki Raws", "https://raws.mangazuki.co", "ko"),
            SingleLang("Mangazuki", "https://mangazuki.co", "en"),
            SingleLang("Remangas", "https://remangas.top", "pt-BR"),
            SingleLang("AnimaRegia", "https://animaregia.net", "pt-BR"),
            SingleLang("MangaVadisi", "http://manga-v2.mangavadisi.org", "tr"),
            SingleLang("MangaID", "https://mangaid.click", "id"),
            SingleLang("Jpmangas", "https://jpmangas.co", "fr"),
            SingleLang("Op-VF", "https://www.op-vf.com", "fr", className = "OpVF"),
            SingleLang("FR Scan", "https://frscan.cc", "fr"),
            // NOTE: THIS SOURCE CONTAINS A CUSTOM LANGUAGE SYSTEM (which will be ignored)!
            SingleLang("HentaiShark", "https://www.hentaishark.com", "all", isNsfw = true),
            //MultiLang("HentaiShark", "https://www.hentaishark.com", listOf("en", "ja", "zh", "de", "nl", "ko", "cz", "eo", "mn", "ar", "sk", "la", "ua", "ceb", "tl", "fi", "bg", "tr"), isNsfw = true, className = "HentaiSharkFactory"),
        )

        @JvmStatic
        fun main(args: Array<String>) {
            MMRCMSGenerator().createAll()
        }
    }
}
