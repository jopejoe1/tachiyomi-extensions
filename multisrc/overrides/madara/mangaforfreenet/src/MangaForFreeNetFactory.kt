package eu.kanade.tachiyomi.extension.all.mangaforfreenet

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class MangaForFreeNetFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        MangaForFreeNetEN(),
        MangaForFreeNetKO(),
        MangaForFreeNetALL(),
    )
}
class MangaForFreeNetEN : Madara("MangaForFree.net", "https://mangaforfree.net", "en") {
    override fun chapterListSelector() = "li.wp-manga-chapter:not(:contains(Raw))"
}
class MangaForFreeNetKO : Madara("MangaForFree.net", "https://mangaforfree.net", "ko") {
    override fun chapterListSelector() = "li.wp-manga-chapter:contains(Raw)"
}
class MangaForFreeNetALL : Madara("MangaForFree.net", "https://mangaforfree.net", "all")
