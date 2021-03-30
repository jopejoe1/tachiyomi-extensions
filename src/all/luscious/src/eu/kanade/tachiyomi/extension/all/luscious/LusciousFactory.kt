package eu.kanade.tachiyomi.extension.all.luscious

import eu.kanade.tachiyomi.annotations.Nsfw
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

@Nsfw
class LusciousFactory : SourceFactory {

    override fun createSources(): List<Source> = listOf(
        Luscious("en", Luscious.ENGLISH_LUS_LANG_VAL, false),
        Luscious("ja", Luscious.JAPANESE_LUS_LANG_VAL, false),
        Luscious("es", Luscious.SPANISH_LUS_LANG_VAL, false),
        Luscious("it", Luscious.ITALIAN_LUS_LANG_VAL, false),
        Luscious("de", Luscious.GERMAN_LUS_LANG_VAL, false),
        Luscious("fr", Luscious.FRENCH_LUS_LANG_VAL, false),
        Luscious("zh", Luscious.CHINESE_LUS_LANG_VAL, false),
        Luscious("ko", Luscious.KOREAN_LUS_LANG_VAL, false),
        Luscious("other", Luscious.OTHERS_LUS_LANG_VAL, false),
        Luscious("pt", Luscious.PORTUGESE_LUS_LANG_VAL, false),
        Luscious("th", Luscious.THAI_LUS_LANG_VAL, false),
        Luscious("en", Luscious.ENGLISH_LUS_LANG_VAL, true),
        Luscious("ja", Luscious.JAPANESE_LUS_LANG_VAL, true),
        Luscious("es", Luscious.SPANISH_LUS_LANG_VAL, true),
        Luscious("it", Luscious.ITALIAN_LUS_LANG_VAL, true),
        Luscious("de", Luscious.GERMAN_LUS_LANG_VAL, true),
        Luscious("fr", Luscious.FRENCH_LUS_LANG_VAL, true),
        Luscious("zh", Luscious.CHINESE_LUS_LANG_VAL, true),
        Luscious("ko", Luscious.KOREAN_LUS_LANG_VAL, true),
        Luscious("other", Luscious.OTHERS_LUS_LANG_VAL, true),
        Luscious("pt", Luscious.PORTUGESE_LUS_LANG_VAL, true),
        Luscious("th", Luscious.THAI_LUS_LANG_VAL, true)
    )
}
