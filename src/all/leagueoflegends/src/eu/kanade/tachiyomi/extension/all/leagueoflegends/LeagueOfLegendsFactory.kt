package eu.kanade.tachiyomi.extension.all.leagueoflegends

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class LeagueOfLegendsFactory : SourceFactory {
    override fun createSources(): List<Source> = languages.map { LeagueOfLegends(it.first, it.second) }
}

private val languages = listOf(
    Pair("en-US", "en_us"),
    Pair("en", "en_gb"),
    Pair("de", "de_de"),
    Pair("es", "es_es"),
    Pair("fr", "fr_fr"),
    Pair("it", "it_it"),
    Pair("en-PL", "en_pl"),
    Pair("pl", "pl_pl"),
    Pair("el", "el_gr"),
    Pair("ro", "ro_ro"),
    Pair("hu", "hu_hu"),
    Pair("cs", "cs_cz"),
    Pair("es-MX", "es_mx"),
    Pair("es-AR", "es_ar"),
    Pair("pt-BR", "pt_br"),
    Pair("ja", "ja_jp"),
    Pair("ru", "ru_ru"),
    Pair("tr", "tr_tr"),
    Pair("en-AU", "en_au"),
    Pair("ko", "ko_kr"),
)
