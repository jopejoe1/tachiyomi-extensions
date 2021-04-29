package eu.kanade.tachiyomi.extension.en.disasterscans

import eu.kanade.tachiyomi.multisrc.madara.Madara
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.ConfigurableSource
import org.jsoup.nodes.Document
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import android.app.Application
import android.content.SharedPreferences
import androidx.preference.ListPreference
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.online.ParsedHttpSource
import okhttp3.Request

abstract class DisasterScans : ConfigurableSource, Madara("Disaster Scans", "https://disasterscans.com", "en") {
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

    override fun pageListRequest(chapter: SChapter): Request {
        val document = client.newCall(GET("${chapter.url}?host=${getHostPref()}")).execute().asJsoup()
        val element = document.select("div.page-break, li.blocks-gallery-item")
        return if (element.isEmpty()){
            GET(chapter.url, headers)
        } else {
            GET("${chapter.url}?host=${getHostPref()}", headers)
        }
    }

    // Prefference
    private val HOST_PREF_KEY = "HOST"
    private val HOST_PREF_TITLE = "Host"
    private val HOST_PREF_ENTRIES = arrayOf("Imgur", "Local", "Amazon")
    private val HOST_PREF_ENTRY_VALUES = arrayOf("Imgur", "Local", "Amazon")
    private val HOST_PREF_DEFAULT_VALUE = HOST_PREF_ENTRY_VALUES[0]

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
}
