package eu.kanade.tachiyomi.extension.all.hentaishark

import eu.kanade.tachiyomi.multisrc.mmrcms.MMRCMSMultiLang
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class HentaiSharkFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        HentaiSharkEN(),
        HentaiSharkJA(),
        HentaiSharkZH(),
        HentaiSharkDE(),
        HentaiSharkNL(),
        HentaiSharkKO(),
        HentaiSharkCZ(),
        HentaiSharkEO(),
        HentaiSharkMN(),
        HentaiSharkAR(),
        HentaiSharkSK(),
        HentaiSharkLA(),
        HentaiSharkUA(),
        HentaiSharkCEB(),
        HentaiSharkTL(),
        HentaiSharkFI(),
        HentaiSharkBG(),
        HentaiSharkTR(),
    )
}
class HentaiSharkEN : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "en")
class HentaiSharkJA : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "ja")
class HentaiSharkZH : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "zh")
class HentaiSharkDE : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "de")
class HentaiSharkNL : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "nl")
class HentaiSharkKO : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "ko")
class HentaiSharkCZ : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "cz")
class HentaiSharkEO : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "eo")
class HentaiSharkMN : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "mn")
class HentaiSharkAR : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "ar")
class HentaiSharkSK : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "sk")
class HentaiSharkLA : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "la")
class HentaiSharkUA : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "ua")
class HentaiSharkCEB : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "ceb")
class HentaiSharkTL : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "tl")
class HentaiSharkFI : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "fi")
class HentaiSharkBG : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "bg")
class HentaiSharkTR : MMRCMSMultiLang("HentaiShark", "https://www.hentaishark.com", "tr")

