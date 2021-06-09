package eu.kanade.tachiyomi.extension.all.mangaforfreenet

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class MangaForFreeNetFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        MangaForFreeNetEN(),
        MangaForFreeNetKO(),
    )
}
class MangaForFreeNetEN : Madara("MangaForFree.net", "https://mangaforfree.net", "en") {
    override fun chapterListSelector() = "li.wp-manga-chapter:not(:contains(Raw))"
}
class MangaForFreeNetKO : Madara("MangaForFree.net", "https://mangaforfree.net", "ko") {
    override fun chapterListSelector() = "li.wp-manga-chapter:contains(Raw)"
}
