package com.benjaminearley.chat.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.benjaminearley.chat.store.ChatStore
import com.benjaminearley.chat.store.UserStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class ChatViewModel(
    private val chatStore: ChatStore,
    private val userStore: UserStore,
    private val chatId: String
) : ViewModel() {

    private val messageDraft: BroadcastChannel<String> = BroadcastChannel(Channel.BUFFERED)

    fun getChatName() = chatStore
        .getChatDetails(chatId)
        .map { it.name }

    fun getChatMessages() = chatStore
        .getChatMessages(chatId)
        .map { messages ->
            userStore.getAuthentication().first().orNull()?.let { user ->
                messages.map { message ->
                    ChatListItem(
                        message.id,
                        message.body,
                        if (message.userId == user.uid) MessageType.SENT else MessageType.RECEIVED
                    )
                }
            } ?: emptyList()
        }
        .map {
            // This is a hack to get items to show up when scrolled to the top
            listOf(listOf(ChatListItem("", "", MessageType.HACK)), it).flatten()
        }
        .asLiveData()

    fun getMessageDraft() = messageDraft.asFlow()

    fun sendMessage(body: String) =
        viewModelScope.launch {
            if (body.isNotBlank()) {
                messageDraft.offer("")
                val user = userStore.getAuthentication().first().orNull()
                user?.let { chatStore.sendMessage(chatId, user.uid, body) }
            }
        }
}

@Suppress("UNCHECKED_CAST")
@FlowPreview
@ExperimentalCoroutinesApi
class ChatViewModelFactory(
    private val chatStore: ChatStore,
    private val userStore: UserStore,
    private val chatId: String
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatViewModel(chatStore, userStore, chatId) as T
    }

}
