package com.benjaminearley.chat.store

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.benjaminearley.chat.domain.Chat
import com.benjaminearley.chat.domain.Message
import com.benjaminearley.chat.store.data.StoreChat
import com.benjaminearley.chat.store.data.StoreMessage
import com.benjaminearley.chat.util.asFlow
import com.benjaminearley.chat.util.flatMapSome
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ChatStore(private val db: FirebaseFirestore) {

    @Suppress("unused")
    private data class FSChat(
        val id: String,
        val name: String,
        val addedDate: Timestamp?,
        val userIds: List<String>
    ) {
        constructor() : this("", "", Timestamp.now(), emptyList())
    }

    @Suppress("unused")
    private data class FSMessage(
        val id: String,
        val userId: String,
        val sentDate: Timestamp?,
        val body: String
    ) {
        constructor() : this("", "", Timestamp.now(), "")
    }


    @ExperimentalCoroutinesApi
    fun getChats(): Flow<List<StoreChat>> {
        try {
            return db
                .collection("chats")
                .orderBy("addedDate", Query.Direction.DESCENDING)
                .asFlow()
                .map { data ->
                    data
                        ?.toObjects<FSChat>()
                        ?.map { StoreChat(it.id, it.name, it.addedDate ?: Timestamp.now(), it.userIds) }
                        ?: emptyList()
                }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    fun getChatDetails(chatId: String): Flow<Option<StoreChat>> {
        try {
            return db
                .collection("chats")
                .document(chatId)
                .asFlow()
                .flatMapSome { data ->
                    data
                        .toObject<FSChat>()
                        ?.let {
                            Some(StoreChat(it.id, it.name, it.addedDate ?: Timestamp.now(), it.userIds))
                        }
                        ?: None
                }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    fun getChatMessages(chatId: String, limit: Long = 100L): Flow<List<StoreMessage>> {
        try {
            return db
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("sentDate", Query.Direction.DESCENDING)
                .limit(limit)
                .asFlow()
                .map { data ->
                    data
                        ?.toObjects<FSMessage>()
                        ?.map { StoreMessage(it.id, it.userId, it.sentDate ?: Timestamp.now(), it.body) }
                        ?: emptyList()
                }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    fun getChatMessage(chatId: String, messageId: String): Flow<Option<StoreMessage>> {
        try {
            return db
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .asFlow()
                .flatMapSome { data ->
                    data
                        .toObject<FSMessage>()
                        ?.let {
                            Some(StoreMessage(it.id, it.userId, it.sentDate ?: Timestamp.now(), it.body))
                        }
                        ?: None
                }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun addChat(name: String): String {
        val id = UUID.randomUUID().toString()
        val chat = hashMapOf(
            "id" to id,
            "addedDate" to FieldValue.serverTimestamp(),
            "name" to name
        )
        try {
            db
                .collection("chats")
                .document(id)
                .set(chat)
                .await()
            return id
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun sendMessage(chatId: String, userId: String, body: String): String {
        val id = UUID.randomUUID().toString()
        val message = hashMapOf(
            "id" to id,
            "sentDate" to FieldValue.serverTimestamp(),
            "userId" to userId,
            "body" to body
        )
        try {
            db
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .document(id)
                .set(message)
                .await()
            return id
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }
}
