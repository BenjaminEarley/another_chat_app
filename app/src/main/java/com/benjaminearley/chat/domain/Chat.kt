package com.benjaminearley.chat.domain

import com.google.firebase.Timestamp

data class Chat(
    val id: String,
    val name: String,
    val addedDate: Timestamp,
    val userIds: List<String>
)