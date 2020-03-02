package com.benjaminearley.chat.store

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.benjaminearley.chat.domain.User
import com.benjaminearley.chat.store.data.FSUser
import com.benjaminearley.chat.util.asFlow
import com.benjaminearley.chat.util.asOptionalFlow
import com.benjaminearley.chat.util.mapSome
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class UserStore(private val db: FirebaseFirestore, private val firebaseAuth: FirebaseAuth) {

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun getCurrentUser(): Flow<Option<User>> {
        try {
            return firebaseAuth
                .asFlow()
                .map { authUser ->
                    when (authUser) {
                        is Some -> getUser(authUser.t.uid).first()
                        None -> None
                    }
                }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun getAuthentication(): Flow<Option<FirebaseUser>> {
        try {
            return firebaseAuth.asFlow()
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    fun getUser(userId: String): Flow<Option<User>> {
        try {
            return db
                .collection("users")
                .document(userId)
                .asOptionalFlow()
                .mapSome { data ->
                    data
                        .toObject<FSUser>()!!
                        .let { User(it.id, it.displayName) }
                }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    fun getUsers(): Flow<List<User>> {
        try {
            return db
                .collection("users")
                .asFlow()
                .map { data ->
                    data
                        ?.toObjects<FSUser>()
                        ?.map { User(it.id, it.displayName) }
                        ?: emptyList()
                }
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun createUser(userId: String, displayName: String): User {
        val user = hashMapOf(
            "id" to userId,
            "createdDate" to FieldValue.serverTimestamp(),
            "name" to displayName
        )
        try {
            db
                .collection("users")
                .document(userId)
                .set(user)
                .await()
            return getUser(userId).first().orNull()!!
        } catch (e: FirebaseFirestoreException) {
            throw e
        }
    }
}