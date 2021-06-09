package eu.kanade.tachiyomi.multisrc.madara

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import eu.kanade.tachiyomi.multisrc.madara.Madara
import kotlin.system.exitProcess

class MadaraUrlActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pathSegments = intent?.data?.pathSegments

        if (pathSegments != null && pathSegments.size >= 1) {

            val mainIntent = Intent().apply {
                action = "eu.kanade.tachiyomi.SEARCH"
                putExtra("query","${Madara.URL_SEARCH_PREFIX}${intent?.data?.toString()}")
                putExtra("filter", packageName)
            }
            try {
                startActivity(mainIntent)
            } catch (e: ActivityNotFoundException) {
                Log.e("MadaraUrl", e.toString())
            }
        } else {
            Log.e("MadaraUrl", "could not parse uri from intent $intent")
        }

        finish()
        exitProcess(0)
    }
}
