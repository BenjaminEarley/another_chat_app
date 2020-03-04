package com.benjaminearley.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import com.benjaminearley.chat.R
import com.benjaminearley.chat.model.ChatModel
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
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModel(private val chatModel: ChatModel) : ViewModel() {

    private val addPresses: BroadcastChannel<Unit> = BroadcastChannel(Channel.BUFFERED)
    private val snackBars: BroadcastChannel<Int> = BroadcastChannel(Channel.BUFFERED)
    private val appBarTitles: BroadcastChannel<String> = BroadcastChannel(Channel.BUFFERED)
    private val navDirections: BroadcastChannel<Either<NavDirections, Int>> =
        BroadcastChannel(Channel.BUFFERED)

    fun getNavDirections() = navDirections.asFlow()

    fun addNavDirections(navDirection: NavDirections) = navDirections.offer(Left(navDirection))

    fun popNavigation(destinationId: Int) = navDirections.offer(Right(destinationId))

    fun getLoginState() = chatModel.isAuthenticated()

    fun addAddClick() = addPresses.offer(Unit)

    fun getAddClicks(): Flow<Unit> = addPresses.asFlow()

    fun showSnackBar(message: Int) = snackBars.offer(message)

    fun getSnackBars(): Flow<Int> = snackBars.asFlow()

    fun setChatTitle(title: String) = appBarTitles.offer(title)

    fun getNewChatTitle(): Flow<String> = appBarTitles.asFlow()

    fun createUser(user: FirebaseUser?) {
        if (user != null) {
            viewModelScope.launch { chatModel.createUser(user.uid, user.displayName ?: "") }
        } else {
            showSnackBar(R.string.error)
        }
    }

    override fun onCleared() {
        super.onCleared()
        addPresses.cancel()
        snackBars.cancel()
        appBarTitles.cancel()
        navDirections.cancel()
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
class MainViewModelFactory(private val chatModel: ChatModel) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(chatModel) as T
    }
}
