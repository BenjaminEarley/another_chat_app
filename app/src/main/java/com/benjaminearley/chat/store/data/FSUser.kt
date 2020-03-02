package com.benjaminearley.chat.store.data

@Suppress("unused")
data class FSUser(
    val id: String,
    val displayName: String
) {
    constructor() : this("", "")
}