package eu.kanade.tachiyomi.multisrc.luscious

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import kotlin.system.exitProcess

class LusciousUrlActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val host = intent?.data?.host
        val pathSegments = intent?.data?.pathSegments

        if (host != null && pathSegments != null) {
            val query = fromLuscious(pathSegments)

            if (query == null) {
                Log.e("LusciousUrlActivity", "Unable to parse URI from intent $intent")
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
                Log.e("LusciousUrlActivity", e.toString())
            }
        }

        finish()
        exitProcess(0)
    }

    private fun fromLuscious(pathSegments: MutableList<String>): String? {
        return if (pathSegments.size >= 2) {
            val id = if(pathSegments[1].contains("_")) {pathSegments[1].substringAfterLast("_") }else { pathSegments[1] }
            "ID:$id"
        } else {
            null
        }
    }
}
