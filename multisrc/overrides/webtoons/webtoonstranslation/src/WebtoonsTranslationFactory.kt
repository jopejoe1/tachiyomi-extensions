package eu.kanade.tachiyomi.extension.all.webtoonstranslation

import eu.kanade.tachiyomi.multisrc.webtoons.Webtoons
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
class WebtoonsEN : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "en", "ENG")
class WebtoonsZH_CMN : WebtoonsTranslation("Webtoons Translation (Simplified)", "https://www.webtoons.com", "zh", "CMN")
class WebtoonsZH_CMY : WebtoonsTranslation("Webtoons Translation (Traditional)", "https://www.webtoons.com", "zh", "CMT")
class WebtoonsTH : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "th", "THA")
class WebtoonsID : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "id", "IND")
class WebtoonsFR : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "fr", "FRA")
class WebtoonsVI : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "vi", "VIE")
class WebtoonsRU : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "ru", "RUS")
class WebtoonsAR : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "ar", "ARA")
class WebtoonsFIL : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "fil", "FIL")
class WebtoonsDE : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "de", "DEU")
class WebtoonsHI : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "hi", "HIN")
class WebtoonsIT : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "it", "ITA")
class WebtoonsJA : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "ja", "JPN")
class WebtoonsPT_POR : WebtoonsTranslation("Webtoons Translation (Brazilian)", "https://www.webtoons.com", "pt", "POR")
class WebtoonsTR : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "tr", "TUR")
class WebtoonsMS : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "ms", "MAY")
class WebtoonsPL : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "pl", "POL")
class WebtoonsPT_POT : WebtoonsTranslation("Webtoons Translation (European)", "https://www.webtoons.com", "pt", "POT")
class WebtoonsBG : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "bg", "BUL")
class WebtoonsDA : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "da", "DAN")
class WebtoonsNL : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "nl", "NLD")
class WebtoonsRO : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "ro", "RON")
class WebtoonsMN : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "mn", "MON")
class WebtoonsEL : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "el", "GRE")
class WebtoonsLT : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "lt", "LIT")
class WebtoonsCS : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "cs", "CES")
class WebtoonsSV : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "sv", "SWE")
class WebtoonsBN : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "bn", "BEN")
class WebtoonsFA : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "fa", "PER")
class WebtoonsUK : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "uk", "UKR")
class WebtoonsES : WebtoonsTranslation("Webtoons Translation", "https://www.webtoons.com", "es", "SPA")


