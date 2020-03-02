package com.benjaminearley.chat.store

import com.benjaminearley.chat.domain.Chat
import com.benjaminearley.chat.domain.Message
import com.benjaminearley.chat.store.data.FSChat
import com.benjaminearley.chat.store.data.FSMessage
import com.benjaminearley.chat.util.asFlow
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatStore(private val db: FirebaseFirestore) {

    @ExperimentalCoroutinesApi
    fun getChats(): Flow<List<Chat>> {
        try {
            return db
                .collection("chats")
                .orderBy("addedDate", Query.Direction.DESCENDING)
                .asFlow()
                .map { data ->
                    data
                        ?.toObjects<FSChat>()
                        ?.map { Chat(it.id, it.name, it.addedDate ?: Timestamp.now(), it.userIds) }
                        ?: emptyList()
                }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    fun getChatDetails(chatId: String): Flow<Chat> {
        try {
            return db
                .collection("chats")
                .document(chatId)
                .asFlow()
                .map { data ->
                    data
                        .toObject<FSChat>()!!
                        .let { Chat(it.id, it.name, it.addedDate ?: Timestamp.now(), it.userIds) }
                }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    fun getChatMessages(chatId: String, limit: Int = 100): Flow<List<Message>> {
        try {
            return db
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("sentDate", Query.Direction.DESCENDING)
                .asFlow()
                .map { data ->
                    data
                        ?.toObjects<FSMessage>()
                        ?.map { Message(it.id, it.userId, it.sentDate ?: Timestamp.now(), it.body) }
                        ?: emptyList()
                }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    fun getChatMessage(chatId: String, messageId: String): Flow<Message> {
        try {
            return db
                .collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .asFlow()
                .map { data ->
                    data
                        .toObject<FSMessage>()!!
                        .let { Message(it.id, it.userId, it.sentDate ?: Timestamp.now(), it.body) }
                }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun addChat(name: String): Chat {
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
            return getChatDetails(id).first()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun sendMessage(chatId: String, userId: String, body: String): Message {
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
            return getChatMessage(chatId, id).first()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }
}