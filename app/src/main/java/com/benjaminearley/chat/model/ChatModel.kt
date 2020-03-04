package com.benjaminearley.chat.model

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.benjaminearley.chat.domain.Chat
import com.benjaminearley.chat.domain.Message
import com.benjaminearley.chat.domain.User
import com.benjaminearley.chat.store.ChatStore
import com.benjaminearley.chat.store.UserStore
import com.benjaminearley.chat.util.mapSome
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Suppress("MemberVisibilityCanBePrivate")
@FlowPreview
@ExperimentalCoroutinesApi
class ChatModel(private val userStore: UserStore, private val chatStore: ChatStore) {

    fun isAuthenticated(): Flow<Boolean> = userStore.getAuthentication().map { it.isDefined() }

    fun getChats(): Flow<List<Chat>> = isAuthenticated()
        .combineTransform(chatStore.getChats()) { maybeUser, chats ->
            emit(
                if (maybeUser) {
                    chats.map {
                        Chat(
                            it.id,
                            it.name,
                            it.addedDate.toDate(),
                            it.userIds.mapNotNull { getUser(it).first().orNull() }
                        )
                    }
                } else {
                    emptyList()
                }
            )
        }
        .distinctUntilChanged()

    fun getChatDetails(chatId: String): Flow<Option<Chat>> = chatStore
        .getChatDetails(chatId)
        .mapSome {
            Chat(
                it.id,
                it.name,
                it.addedDate.toDate(),
                it.userIds.mapNotNull { getUser(it).first().orNull() }
            )
        }
        .distinctUntilChanged()

    fun getChatMessages(chatId: String): Flow<List<Message>> =
        combineTransform(
            chatStore.getChatMessages(chatId),
            getCurrentUser(),
            getUsers()
        ) { messages, currentUser, users ->
            when (currentUser) {
                None -> emit(emptyList<Message>())
                is Some ->
                    emit(
                        messages
                            .mapNotNull { message ->
                                users.firstOrNull { it.id == message.userId }?.let { user ->
                                    Message(
                                        message.id,
                                        user,
                                        currentUser.t.id == message.userId,
                                        message.sentDate.toDate(),
                                        message.body
                                    )
                                } ?: null
                            }
                    )
            }
        }.distinctUntilChanged()

    fun getChatMessage(chatId: String, messageId: String): Flow<Option<Message>> =
        combineTransform(
            chatStore.getChatMessage(chatId, messageId),
            getCurrentUser(),
            getUsers()
        ) { message, currentUser, users ->
            when (message) {
                None -> emit(None)
                is Some -> when (currentUser) {
                    None -> emit(None)
                    is Some -> {
                        users.firstOrNull { it.id == message.t.id }?.let { user ->
                            emit(
                                Some(
                                    Message(
                                        message.t.id,
                                        user,
                                        currentUser.t.id == message.t.id,
                                        message.t.sentDate.toDate(),
                                        message.t.body
                                    )
                                )
                            )
                        } ?: emit(None)
                    }
                }
            }
        }.distinctUntilChanged()

    fun getUsers(): Flow<List<User>> =
        userStore.getUsers().map { it.map { User(it.id, it.displayName) } }.distinctUntilChanged()

    fun getUser(userId: String): Flow<Option<User>> =
        userStore.getUser(userId).mapSome { User(it.id, it.displayName) }.distinctUntilChanged()

    fun getCurrentUser(): Flow<Option<User>> {
        try {
            return userStore
                .getAuthentication()
                .map { authUser ->
                    when (authUser) {
                        is Some -> getUser(authUser.t.uid).first()
                        None -> None
                    }
                }
                .distinctUntilChanged()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    suspend fun createUser(userId: String, displayName: String): Option<User> {
        val id = userStore.createUser(userId, displayName)
        return getUser(id).first()
    }

    suspend fun sendMessage(chatId: String, body: String): Option<Message> {
        return when (val userId = getCurrentUser().first().map { it.id }) {
            None -> None
            is Some -> {
                val id = chatStore.sendMessage(chatId, userId.t, body)
                getChatMessage(chatId, id).first()
            }
        }
    }

    suspend fun addChat(name: String): Option<Chat> {
        val id = chatStore.addChat(name)
        return getChatDetails(id).first()
    }

}