package com.benjaminearley.chat.domain

import com.google.firebase.Timestamp

data class Message(
    val id: String,
    val userId: String,
    val sentDate: Timestamp,
    val body: String
)