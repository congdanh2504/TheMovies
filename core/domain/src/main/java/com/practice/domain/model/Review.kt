package com.practice.domain.model

data class Review(
    val author: String,
    val content: String,
    val createdAt: String,
    val avatarPath: String? = null,
    val rating: Float? = null
)
