package eu.kanade.tachiyomi.extension.en.disasterscans

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.source.ConfigurableSource
import eu.kanade.tachiyomi.source.model.SManga
import org.jsoup.nodes.Document
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class DisasterScans : ConfigurableSource, Madara("Disaster Scans", "https://disasterscans.com", "en") {
    override val popularMangaUrlSelector = "div.post-title a:last-child"

    override fun mangaDetailsParse(document: Document): SManga {
        val manga = super.mangaDetailsParse(document)

        with(document) {
            select("div.post-title h1").first()?.let {
                manga.title = it.ownText()
            }
        }

        return manga
    }
}
