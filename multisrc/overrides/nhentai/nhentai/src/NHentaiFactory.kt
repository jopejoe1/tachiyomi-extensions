package eu.kanade.tachiyomi.extension.all.nhentai

import eu.kanade.tachiyomi.multisrc.nhentai.NHentai
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory
import eu.kanade.tachiyomi.lib.ratelimit.RateLimitInterceptor
import okhttp3.OkHttpClient

class NHentaiFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        NHentaiEN(),
        NHentaiJA(),
        NHentaiZH(),
        NHentaiALL(),
    )
}
class NHentaiEN : NHentai("NHentai", "https://nhentai.net", "en") {
    private val rateLimitInterceptor = RateLimitInterceptor(4)
    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .addNetworkInterceptor(rateLimitInterceptor)
        .build()
}
class NHentaiJA : NHentai("NHentai", "https://nhentai.net", "ja") {
    private val rateLimitInterceptor = RateLimitInterceptor(4)
    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .addNetworkInterceptor(rateLimitInterceptor)
        .build()
}
class NHentaiZH : NHentai("NHentai", "https://nhentai.net", "zh") {
    private val rateLimitInterceptor = RateLimitInterceptor(4)
    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .addNetworkInterceptor(rateLimitInterceptor)
        .build()
}
class NHentaiALL : NHentai("NHentai", "https://nhentai.net", "all") {
    private val rateLimitInterceptor = RateLimitInterceptor(4)
    override val client: OkHttpClient = network.cloudflareClient.newBuilder()
        .addNetworkInterceptor(rateLimitInterceptor)
        .build()
}
