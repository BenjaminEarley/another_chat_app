package com.benjaminearley.chat.store.data

import com.google.firebase.Timestamp

@Suppress("unused")
data class FSMessage(
    val id: String,
    val userId: String,
    val sentDate: Timestamp?,
    val body: String
) {
    constructor() : this("", "", Timestamp.now(), "")
}