package com.benjaminearley.chat.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.benjaminearley.chat.model.ChatModel
import com.benjaminearley.chat.ui.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class ChatsViewModel(
    private val mainViewModel: MainViewModel,
    private val chatModel: ChatModel
) : ViewModel() {

    init {
        viewModelScope.launch {
            mainViewModel
                .getAddClicks()
                .collect {
                    mainViewModel.addNavDirections(ChatsFragmentDirections.actionAddChat())
                }
        }
    }

    fun getChatListItems() = chatModel
        .getChats()
        .map { chats -> chats.map { chat -> ChatsListItem(chat.id, chat.name) } }
        .map { list ->
            // This is a hack to get items to show up when scrolled to the top
            listOf(listOf(ChatsListItem("top", "")), list).flatten()
        }
        .asLiveData()

    fun chatListItemClicked(chatsListItem: ChatsListItem) {
        viewModelScope.launch {
            chatModel.getChats().first().find { it.id == chatsListItem.id }?.let {
                mainViewModel.addNavDirections(
                    ChatsFragmentDirections.actionOpenChat(
                        chatsListItem.id,
                        chatsListItem.name
                    )
                )
            }
        }
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
class ChatsViewModelFactory(
    private val mainViewModel: MainViewModel,
    private val chatModel: ChatModel
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ChatsViewModel(mainViewModel, chatModel) as T
    }
}
