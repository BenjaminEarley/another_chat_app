package com.benjaminearley.chat.store.data

import com.google.firebase.Timestamp

@Suppress("unused")
data class FSChat(
    val id: String,
    val name: String,
    val addedDate: Timestamp?,
    val userIds: List<String>
) {
    constructor() : this("", "", Timestamp.now(), emptyList())
}