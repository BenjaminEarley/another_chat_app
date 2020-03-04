package com.benjaminearley.chat.store.data

import com.google.firebase.Timestamp

data class StoreMessage(
    val id: String,
    val userId: String,
    val sentDate: Timestamp,
    val body: String
)
