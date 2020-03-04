package com.benjaminearley.chat.domain

import java.util.Date

data class Message(
    val id: String,
    val user: User,
    val isMyMessage: Boolean,
    val sentDate: Date,
    val body: String
)
