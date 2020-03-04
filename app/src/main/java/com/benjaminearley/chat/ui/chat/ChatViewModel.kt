package com.benjaminearley.chat.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import arrow.core.Some
import com.benjaminearley.chat.R
import com.benjaminearley.chat.model.ChatModel
import com.benjaminearley.chat.ui.MainViewModel
import com.benjaminearley.chat.util.mapSome
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class ChatViewModel(
    private val mainViewModel: MainViewModel,
    private val chatModel: ChatModel,
    private val chatId: String
) : ViewModel() {

    private val messageDraft: BroadcastChannel<String> = BroadcastChannel(Channel.BUFFERED)

    init {
        viewModelScope.launch {
            chatModel
                .getChatDetails(chatId)
                .mapSome { it.name }
                .collect {
                    when (it) {
                        is Some -> setChatTitle(it.t)
                        else -> {
                            mainViewModel.popNavigation(R.id.chats_fragment)
                            mainViewModel.showSnackBar(R.string.chat_deleted)
                        }
                    }
                }
        }
    }

    fun setChatTitle(title: String) = mainViewModel.setChatTitle(title)

    fun getChatMessages() = chatModel
        .getChatMessages(chatId)
        .map { messages ->
            messages.map { message ->
                ChatListItem(
                    message.id,
                    message.body,
                    if (message.isMyMessage) MessageType.SENT else MessageType.RECEIVED
                )
            }
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
                chatModel.sendMessage(chatId, body)
            }
        }
}


@Suppress("UNCHECKED_CAST")
@FlowPreview
@ExperimentalCoroutinesApi
class ChatViewModelFactory(
    private val mainViewModel: MainViewModel,
    private val chatModel: ChatModel,
    private val chatId: String
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatViewModel(mainViewModel, chatModel, chatId) as T
    }
}
