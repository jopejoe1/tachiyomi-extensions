package eu.kanade.tachiyomi.extension.all.nhentaicom

import eu.kanade.tachiyomi.annotations.Nsfw
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

@Nsfw
class nHentaiComFactory : SourceFactory {
    override fun createSources(): List<Source> = languages.map { nHentaiCom(it) }
}

private val languages = listOf(
    "all", "en", "zh", "ja", "other", "eo", "ceb", "cz", "ar", "sk", "mn", "uk", "la", "tl", "es", "it", "ko", "th", "pl", "fr", "pt", "de", "fi", "ru", "sv", "hu", "id", "vi", "da", "ro", "et", "nl", "ca", "tr", "el", "nn", "sq", "bg", "jv", "sr"
)
