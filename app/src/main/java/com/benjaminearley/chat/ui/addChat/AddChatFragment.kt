package com.benjaminearley.chat.ui.addChat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import arrow.core.None
import arrow.core.Some
import com.benjaminearley.chat.ChatApplication
import com.benjaminearley.chat.R
import com.benjaminearley.chat.databinding.AddChatBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect

@FlowPreview
@ExperimentalCoroutinesApi
class AddChatFragment : BottomSheetDialogFragment() {

    private val viewModel by viewModels<AddChatViewModel> {
        AddChatViewModelFactory(ChatApplication.INSTANCE.getChatModel())
    }

    private var _binding: AddChatBinding? = null
    private val binding get() = _binding!!

    init {
        lifecycleScope.launchWhenStarted {
            viewModel.getNavDirections().collect { findNavController().navigate(it) }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.getInputErrors()
                .collect {
                    when (it) {
                        None -> binding.chatNameFieldLayout.error = ""
                        is Some -> binding.chatNameFieldLayout.error = getString(it.t)
                    }
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.ThemeOverlay_Chat_Pink_AddChatBottomSheetDialogTheme
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = AddChatBinding.inflate(inflater, container, false)

        with(binding) {
            chatNameFieldLayout.isErrorEnabled = true
            chatNameField.requestFocus()
            chatNameField.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    viewModel.submitChatName(v.text.toString())
                    true
                } else false
            }

            chatNameField.doOnTextChanged { text, _, _, _ ->
                viewModel.enteringChatName(text.toString())
            }

            button.setOnClickListener {
                viewModel.submitChatName(chatNameField.text.toString())
            }

            return root
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}