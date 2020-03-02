package com.benjaminearley.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.benjaminearley.chat.store.UserStore
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModel(private val userStore: UserStore) : ViewModel() {

    private val loginState: BroadcastChannel<Boolean> = BroadcastChannel(Channel.BUFFERED)
    private val addPresses: BroadcastChannel<Unit> = BroadcastChannel(Channel.BUFFERED)
    private val snackBars: BroadcastChannel<String> = BroadcastChannel(Channel.BUFFERED)
    private val appBarTitles: BroadcastChannel<String> = BroadcastChannel(Channel.BUFFERED)

    init {
        viewModelScope.launch {
            userStore
                .getAuthentication()
                .collect {
                    loginState.offer(it.isDefined())
                }
        }
    }

    suspend fun isAuthenticated() = userStore.getAuthentication().map { it.isDefined() }.first()

    fun getLoginState() = loginState.asFlow()

    fun addAddClick() = addPresses.offer(Unit)

    fun getAddClicks(): Flow<Unit> = addPresses.asFlow()

    fun showSnackBar(message: String) = snackBars.offer(message)

    fun getSnackBars(): Flow<String> = snackBars.asFlow()

    fun setChatTitle(title: String) = appBarTitles.offer(title)

    fun getNewChatTitle(): Flow<String> = appBarTitles.asFlow()

    fun createUser(user: FirebaseUser?) {
        if (user != null) {
            viewModelScope.launch { userStore.createUser(user.uid, user.displayName ?: "") }
        } else {
            showSnackBar("Error")
        }
    }


    override fun onCleared() {
        super.onCleared()
        loginState.cancel()
        addPresses.cancel()
        snackBars.cancel()
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val userStore: UserStore) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(userStore) as T
    }
}