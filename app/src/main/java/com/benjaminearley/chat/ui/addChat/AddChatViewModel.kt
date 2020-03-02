package com.benjaminearley.chat.ui.addChat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.benjaminearley.chat.R
import com.benjaminearley.chat.store.ChatStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class AddChatViewModel(private val chatStore: ChatStore) : ViewModel() {

    private val navDirections: BroadcastChannel<NavDirections> = BroadcastChannel(Channel.BUFFERED)
    private val inputErrors: BroadcastChannel<Option<Int>> = BroadcastChannel(Channel.BUFFERED)

    fun getNavDirections() = navDirections.asFlow()
    fun getInputErrors() = inputErrors.asFlow().distinctUntilChanged()

    fun enteringChatName(name: String) {
        inputErrors.offer(None)
    }

    fun submitChatName(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val chat = chatStore.addChat(name)
                navDirections.offer(AddChatFragmentDirections.actionOpenNewChat(chat.id, chat.name))
            } else {
                inputErrors.offer(Some(R.string.enter_chat_name))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        navDirections.cancel()
        inputErrors.cancel()
    }
}

@Suppress("UNCHECKED_CAST")
@FlowPreview
@ExperimentalCoroutinesApi
class AddChatViewModelFactory(private val chatStore: ChatStore) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddChatViewModel(chatStore) as T
    }

}