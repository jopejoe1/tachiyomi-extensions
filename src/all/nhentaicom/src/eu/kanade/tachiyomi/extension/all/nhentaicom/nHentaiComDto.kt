package eu.kanade.tachiyomi.extension.all.nhentaicom

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class nHentaiComDto(
    val auth: JsonObject,
    val user: JsonObject
)
