package com.benjaminearley.chat.ui.addChat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.benjaminearley.chat.R
import com.benjaminearley.chat.model.ChatModel
import com.benjaminearley.chat.store.ChatStore
import com.benjaminearley.chat.ui.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class AddChatViewModel(
    private val mainViewModel: MainViewModel,
    private val chatModel: ChatModel
) : ViewModel() {

    private val inputErrors: BroadcastChannel<Option<Int>> = BroadcastChannel(Channel.BUFFERED)

    fun getInputErrors() = inputErrors.asFlow().distinctUntilChanged()

    fun enteringChatName(name: String) {
        inputErrors.offer(None)
    }

    fun submitChatName(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                when (val chat = chatModel.addChat(name)) {
                    is Some -> {
                        mainViewModel.addNavDirections(
                            AddChatFragmentDirections.actionOpenNewChat(
                                chat.t.id,
                                chat.t.name
                            )
                        )
                    }
                    None -> {
                        mainViewModel.showSnackBar(R.string.message_error)
                    }
                }
            } else {
                inputErrors.offer(Some(R.string.enter_chat_name))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        inputErrors.cancel()
    }
}

@Suppress("UNCHECKED_CAST")
@FlowPreview
@ExperimentalCoroutinesApi
class AddChatViewModelFactory(
    private val mainViewModel: MainViewModel,
    private val chatModel: ChatModel
) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddChatViewModel(mainViewModel, chatModel) as T
    }
}
