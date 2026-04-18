package com.practice.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkReview(
    @Json(name = "author") val author: String,
    @Json(name = "content") val content: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "author_details") val authorDetails: NetworkAuthorDetails? = null
)

@JsonClass(generateAdapter = true)
data class NetworkAuthorDetails(
    @Json(name = "avatar_path") val avatarPath: String? = null,
    @Json(name = "rating") val rating: Float? = null
)
