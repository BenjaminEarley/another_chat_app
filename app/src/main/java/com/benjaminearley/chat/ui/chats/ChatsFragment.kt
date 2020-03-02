package com.benjaminearley.chat.ui.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.benjaminearley.chat.ChatApplication
import com.benjaminearley.chat.databinding.ChatsBinding
import com.benjaminearley.chat.ui.MainViewModel
import com.benjaminearley.chat.ui.MainViewModelFactory
import com.benjaminearley.chat.util.divider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect

@FlowPreview
@ExperimentalCoroutinesApi
class ChatsFragment : Fragment() {

    private val mainViewModel by activityViewModels<MainViewModel> {
        ChatApplication.INSTANCE.run { MainViewModelFactory(getUserModel()) }
    }
    private val viewModel by viewModels<ChatsViewModel> {
        ChatApplication.INSTANCE.run { ChatsViewModelFactory(getChatModel(), getUserModel()) }
    }

    private var _binding: ChatsBinding? = null
    private val binding get() = _binding!!

    init {
        lifecycleScope.launchWhenStarted {
            mainViewModel.getAddClicks().collect { viewModel.addChat() }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.getNavDirections().collect { findNavController().navigate(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChatsBinding.inflate(inflater, container, false)

        val chatsAdapter = ChatsAdapter { chatListItem ->
            viewModel.chatListItemClicked(chatListItem)
        }
        binding.chats.apply {
            addItemDecoration(divider)
            adapter = chatsAdapter
        }

        viewModel.getChatListItems().observe(viewLifecycleOwner) { chatsAdapter.submitList(it) }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}