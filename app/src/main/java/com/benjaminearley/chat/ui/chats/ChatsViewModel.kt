package com.benjaminearley.chat.ui.chats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import arrow.core.None
import arrow.core.Some
import com.benjaminearley.chat.domain.Chat
import com.benjaminearley.chat.store.ChatStore
import com.benjaminearley.chat.store.UserStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class ChatsViewModel(
    private val chatStore: ChatStore,
    private val userStore: UserStore
) : ViewModel() {

    private val chatsBehavior: ConflatedBroadcastChannel<List<Chat>> =
        ConflatedBroadcastChannel(emptyList())
    private val navDirections: BroadcastChannel<NavDirections> = BroadcastChannel(Channel.BUFFERED)

    init {
        viewModelScope.launch {
            userStore
                .getAuthentication()
                .combineTransform(chatStore.getChats()) { maybeUser, chats ->
                    Log.e("TEST", "TEST: $maybeUser $chats")
                    when (maybeUser) {
                        is Some -> emit(chats)
                        None -> emit(emptyList())
                    }
                }.collect {
                    chatsBehavior.offer(it)
                }
        }
    }

    fun getChatListItems() = chatsBehavior
        .asFlow()
        .map { chats ->
            chats.map { chat ->
                ChatListItem(
                    chat.id,
                    chat.name
                )
            }
        }
        .map { list ->
            // This is a hack to get items to show up when scrolled to the top
            listOf(listOf(ChatListItem("top", "")), list).flatten()
        }
        .asLiveData()

    fun getNavDirections() = navDirections.asFlow()

    fun addChat() = navDirections.offer(ChatsFragmentDirections.actionAddChat())

    fun chatListItemClicked(chatListItem: ChatListItem) {
        chatsBehavior.value.find { it.id == chatListItem.id }?.let {
            navDirections.offer(
                ChatsFragmentDirections.actionOpenChat(
                    chatListItem.id,
                    chatListItem.name
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatsBehavior.cancel()
        navDirections.cancel()
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
class ChatsViewModelFactory(
    private val chatStore: ChatStore,
    private val userStore: UserStore
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatsViewModel(chatStore, userStore) as T
    }

}
