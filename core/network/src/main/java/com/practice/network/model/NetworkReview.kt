package com.practice.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkReview(
    @Json(name = "author") val author: String,
    @Json(name = "content") val content: String,
    @Json(name = "created_at") val createdAt: String
)
