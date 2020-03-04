package com.benjaminearley.chat.domain

import java.util.Date

data class Chat(
    val id: String,
    val name: String,
    val addedDate: Date,
    val users: List<User>
)
