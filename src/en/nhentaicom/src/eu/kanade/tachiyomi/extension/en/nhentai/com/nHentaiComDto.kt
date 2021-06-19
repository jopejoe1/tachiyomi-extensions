package eu.kanade.tachiyomi.extension.en.nhentai.com

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class nHentaiComDto(
    val auth: JsonObject,
    val user: JsonObject
)
