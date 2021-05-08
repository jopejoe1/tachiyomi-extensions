package eu.kanade.tachiyomi.extension.all.genkanio

import eu.kanade.tachiyomi.annotations.Nsfw
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

@Nsfw
class GenkanIOFactory : SourceFactory {
    override fun createSources(): List<Source> = languages.map { GenkanIO(it.first, it.second) }
}
private val languages = listOf(
    Pair("all", ""),
    Pair("ar", "Arabic"),
    Pair("en", "English"),
    Pair("fr", "French"),
    Pair("pl", "Polish"),
    Pair("pt", "Portuguese"),
    Pair("ru", "Russian"),
    Pair("es", "Spanish"),
    Pair("tr", "Turkish"),
)
