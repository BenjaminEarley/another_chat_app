package com.benjaminearley.chat.util

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.toOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.transform

@ExperimentalCoroutinesApi
fun Query.asFlow(): Flow<QuerySnapshot?> {
    return callbackFlow {
        val snapshotListener =
            addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    cancel(
                        message = "error fetching collection data at path",
                        cause = firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }
                offer(querySnapshot)
            }
        awaitClose {
            snapshotListener.remove()
        }
    }
}

@ExperimentalCoroutinesApi
fun DocumentReference.asFlow(): Flow<DocumentSnapshot> {
    return callbackFlow {
        val snapshotListener =
            addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    cancel(
                        message = "error fetching collection data at path",
                        cause = firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }
                offer(documentSnapshot!!)
            }
        awaitClose {
            snapshotListener.remove()
        }
    }
}

@ExperimentalCoroutinesApi
fun DocumentReference.asOptionalFlow(): Flow<Option<DocumentSnapshot>> {
    return callbackFlow {
        val snapshotListener =
            addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    cancel(
                        message = "error fetching collection data at path",
                        cause = firebaseFirestoreException
                    )
                    return@addSnapshotListener
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    offer(Some(documentSnapshot))
                } else {
                    offer(None)
                }

            }
        awaitClose {
            snapshotListener.remove()
        }
    }
}

@ExperimentalCoroutinesApi
fun FirebaseAuth.asFlow(): Flow<Option<FirebaseUser>> {
    return callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener {
            offer(currentUser.toOption())
        }
        addAuthStateListener(authStateListener)
        awaitClose {
            removeAuthStateListener(authStateListener)
        }
    }
}

@ExperimentalCoroutinesApi
inline fun <T, R> Flow<Option<T>>.mapSome(
    crossinline transform: suspend (value: T) -> R
): Flow<Option<R>> = transform { value ->
    when (value) {
        None -> emit(None)
        is Some -> emit(Some(transform(value.t)))
    }
}