package eu.kanade.tachiyomi.extension.meta.mangaupdates

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlin.system.exitProcess

class MangaUpdatesUrlActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val host = intent?.data?.host
        val pathSegments = intent?.data?.pathSegments
        val parameters = intent?.data?.getQueryParameter("id")

        if (parameters != null) {
            val query = "ID:$parameters"

            if (query == null) {
                Log.e("MangaUpdatesUrlActivity", "Unable to parse URI from intent $intent")
                finish()
                exitProcess(1)
            }

            val mainIntent = Intent().apply {
                action = "eu.kanade.tachiyomi.SEARCH"
                putExtra("query", query)
                putExtra("filter", packageName)
            }

            try {
                startActivity(mainIntent)
            } catch (e: ActivityNotFoundException) {
                Log.e("MangaUpdatesUrlActivity", e.toString())
            }
        }

        finish()
        exitProcess(0)
    }
}
