package com.benjaminearley.chat.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import com.benjaminearley.chat.ChatApplication
import com.benjaminearley.chat.databinding.ChatBinding
import com.benjaminearley.chat.ui.MainViewModel
import com.benjaminearley.chat.util.getDivider
import com.benjaminearley.chat.util.hideKeyboard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect

@FlowPreview
@ExperimentalCoroutinesApi
class ChatFragment : Fragment() {

    private val args: ChatFragmentArgs by navArgs()

    private val viewModel by viewModels<ChatViewModel> {
        val mainViewModel: MainViewModel by activityViewModels()
        ChatViewModelFactory(
            mainViewModel,
            ChatApplication.INSTANCE.getChatModel(),
            args.chatId
        )
    }

    private var _binding: ChatBinding? = null
    private val binding get() = _binding!!

    init {
        lifecycleScope.launchWhenStarted {
            viewModel.getMessageDraft().collect { binding.messageField.setText(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChatBinding.inflate(inflater, container, false)

        viewModel.setChatTitle(args.chatName)

        val chatAdapter = ChatAdapter()

        with(binding) {
            chat.apply {
                addItemDecoration(getDivider())
                adapter = chatAdapter
            }

            viewModel.getChatMessages().observe(viewLifecycleOwner) { chatAdapter.submitList(it) }

            send.setOnClickListener {
                viewModel.sendMessage(messageField.text.toString())
            }

            messageField.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    viewModel.sendMessage(v.text.toString())
                    true
                } else false
            }

            return root
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        super.onStop()
        binding.messageField.hideKeyboard()
    }
}
