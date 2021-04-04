package eu.kanade.tachiyomi.multisrc.webtoonstranslation

import eu.kanade.tachiyomi.multisrc.webtoons.Webtoons
import eu.kanade.tachiyomi.multisrc.webtoons.WebtoonsTranslation
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class WebtoonsTranslationFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        WebtoonsTranslationEN(),
        WebtoonsTranslationZH_CMN(),
        WebtoonsTranslationZH_CMY(),
        WebtoonsTranslationTH(),
        WebtoonsTranslationID(),
        WebtoonsTranslationFR(),
        WebtoonsTranslationVI(),
        WebtoonsTranslationRU(),
        WebtoonsTranslationAR(),
        WebtoonsTranslationFIL(),
        WebtoonsTranslationDE(),
        WebtoonsTranslationHI(),
        WebtoonsTranslationIT(),
        WebtoonsTranslationJA(),
        WebtoonsTranslationPT_POR(),
        WebtoonsTranslationTR(),
        WebtoonsTranslationMS(),
        WebtoonsTranslationPL(),
        WebtoonsTranslationPT_POT(),
        WebtoonsTranslationBG(),
        WebtoonsTranslationDA(),
        WebtoonsTranslationNL(),
        WebtoonsTranslationRO(),
        WebtoonsTranslationMN(),
        WebtoonsTranslationEL(),
        WebtoonsTranslationLT(),
        WebtoonsTranslationCS(),
        WebtoonsTranslationSV(),
        WebtoonsTranslationBN(),
        WebtoonsTranslationFA(),
        WebtoonsTranslationUK(),
        WebtoonsTranslationES(),
    )
}
class WebtoonsTranslationEN : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "en", "ENG")
class WebtoonsTranslationZH_CMN : WebtoonsTranslation("Webtoons Translation (Simplified)", "https://www.webtoons.com", "zh", "CMN")
class WebtoonsTranslationZH_CMY : WebtoonsTranslation("Webtoons Translation (Traditional)", "https://www.webtoons.com", "zh", "CMT")
class WebtoonsTranslationTH : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "th", "THA")
class WebtoonsTranslationID : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "id", "IND")
class WebtoonsTranslationFR : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "fr", "FRA")
class WebtoonsTranslationVI : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "vi", "VIE")
class WebtoonsTranslationRU : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "ru", "RUS")
class WebtoonsTranslationAR : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "ar", "ARA")
class WebtoonsTranslationFIL : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "fil", "FIL")
class WebtoonsTranslationDE : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "de", "DEU")
class WebtoonsTranslationHI : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "hi", "HIN")
class WebtoonsTranslationIT : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "it", "ITA")
class WebtoonsTranslationJA : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "ja", "JPN")
class WebtoonsTranslationPT_POR : WebtoonsTranslation("Webtoons Translation (Brazilian)", "https://www.webtoons.com", "pt", "POR")
class WebtoonsTranslationTR : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "tr", "TUR")
class WebtoonsTranslationMS : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "ms", "MAY")
class WebtoonsTranslationPL : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "pl", "POL")
class WebtoonsTranslationPT_POT : WebtoonsTranslation("Webtoons Translation (European)", "https://www.webtoons.com", "pt", "POT")
class WebtoonsTranslationBG : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "bg", "BUL")
class WebtoonsTranslationDA : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "da", "DAN")
class WebtoonsTranslationNL : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "nl", "NLD")
class WebtoonsTranslationRO : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "ro", "RON")
class WebtoonsTranslationMN : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "mn", "MON")
class WebtoonsTranslationEL : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "el", "GRE")
class WebtoonsTranslationLT : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "lt", "LIT")
class WebtoonsTranslationCS : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "cs", "CES")
class WebtoonsTranslationSV : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "sv", "SWE")
class WebtoonsTranslationBN : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "bn", "BEN")
class WebtoonsTranslationFA : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "fa", "PER")
class WebtoonsTranslationUK : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "uk", "UKR")
class WebtoonsTranslationES : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "es", "SPA")


