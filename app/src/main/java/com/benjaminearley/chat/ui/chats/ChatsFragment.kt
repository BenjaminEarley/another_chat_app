package com.benjaminearley.chat.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.benjaminearley.chat.ChatApplication
import com.benjaminearley.chat.databinding.ChatsBinding
import com.benjaminearley.chat.ui.MainViewModel
import com.benjaminearley.chat.ui.MainViewModelFactory
import com.benjaminearley.chat.util.getDivider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class ChatsFragment : Fragment() {

    private val viewModel by viewModels<ChatsViewModel> {
        val mainViewModel by activityViewModels<MainViewModel> {
            MainViewModelFactory(ChatApplication.INSTANCE.getChatModel())
        }
        ChatsViewModelFactory(mainViewModel, ChatApplication.INSTANCE.getChatModel())
    }

    private var _binding: ChatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChatsBinding.inflate(inflater, container, false)

        val chatsAdapter = ChatsAdapter { chatListItem ->
            viewModel.chatListItemClicked(chatListItem)
        }

        with(binding) {
            chats.apply {
                addItemDecoration(getDivider())
                adapter = chatsAdapter
            }

            viewModel.getChatListItems().observe(viewLifecycleOwner) { chatsAdapter.submitList(it) }

            return root
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
