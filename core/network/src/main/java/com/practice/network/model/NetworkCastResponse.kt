package com.practice.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkCastResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "cast") val cast: List<NetworkCast>
)
