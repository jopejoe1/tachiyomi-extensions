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

    // Preference
    override var imageHosts = arrayOf(
        getHostPref().toString()
    ) + HOST_PREF_ENTRY_VALUES.filterNot { it == getHostPref() }

    override fun setupPreferenceScreen(screen: PreferenceScreen) {
        val hostPref = ListPreference(screen.context).apply {
            key = HOST_PREF_KEY
            title = HOST_PREF_TITLE
            entries = HOST_PREF_ENTRIES
            entryValues = HOST_PREF_ENTRY_VALUES
            setDefaultValue(HOST_PREF_DEFAULT_VALUE)
            summary = "%s"

            setOnPreferenceChangeListener { _, newValue ->
                val selected = newValue as String
                val index = findIndexOfValue(selected)
                val entry = entryValues[index] as String
                preferences.edit().putString("${HOST_PREF_KEY}_$lang", entry).commit()
            }
        }
        screen.addPreference(hostPref)
    }

    private fun getHostPref(): String? = preferences.getString(HOST_PREF_KEY, HOST_PREF_DEFAULT_VALUE)

    private val preferences: SharedPreferences by lazy {
        Injekt.get<Application>().getSharedPreferences("source_$id", 0x0000)
    }
    companion object {
        private val HOST_PREF_KEY = "HOST"
        private val HOST_PREF_TITLE = "Host"
        private val HOST_PREF_ENTRIES = arrayOf("Imgur", "Local", "Amazon")
        private val HOST_PREF_ENTRY_VALUES = arrayOf("Imgur", "Local", "Amazon")
        private val HOST_PREF_DEFAULT_VALUE = HOST_PREF_ENTRY_VALUES[0]
    }
}
