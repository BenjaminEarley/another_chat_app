package com.benjaminearley.chat.store.data

import com.google.firebase.Timestamp

data class StoreChat(
    val id: String,
    val name: String,
    val addedDate: Timestamp,
    val userIds: List<String>
)
