package com.benjaminearley.chat

import android.app.Application
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import androidx.appcompat.app.AppCompatDelegate
import com.benjaminearley.chat.model.ChatModel
import com.benjaminearley.chat.store.ChatStore
import com.benjaminearley.chat.store.UserStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

class ChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        val nightMode = if (SDK_INT >= Q) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        } else {
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    fun getChatModel() =
        ChatModel(
            UserStore(Firebase.firestore, FirebaseAuth.getInstance()),
            ChatStore(Firebase.firestore)
        )

    companion object {
        lateinit var INSTANCE: ChatApplication
    }
}
