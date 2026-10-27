package com.example.omnigo.core.network

import com.google.gson.JsonParser
import retrofit2.HttpException

/**
 * Extracts the human readable `message` field from a failed backend call's JSON error body
 * (ApiResponse shape). Returns null when the body carries no usable text, so callers show a
 * localized default instead of an empty toast or a raw HTTP reason phrase (which is blank on HTTP/2).
 */
fun HttpException.toBackendMessage(): String? {
    return runCatching {
        val raw = response()?.errorBody()?.string().orEmpty()
        val json = JsonParser.parseString(raw).asJsonObject.get("message")
        if (json == null || json.isJsonNull) null else json.asString
    }.getOrNull()?.takeIf { it.isNotBlank() }
}
