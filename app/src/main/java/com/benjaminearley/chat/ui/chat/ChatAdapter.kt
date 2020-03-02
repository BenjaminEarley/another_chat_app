package com.benjaminearley.chat.ui.chat

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.benjaminearley.chat.R
import com.benjaminearley.chat.databinding.ReceivedMessageListItemBinding
import com.benjaminearley.chat.databinding.SentMessageListItemBinding
import com.benjaminearley.chat.util.inflate
import com.benjaminearley.chat.util.inflater

data class ChatListItem(val id: String, val body: String, val type: MessageType) {
    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ChatListItem> =
            object : DiffUtil.ItemCallback<ChatListItem>() {
                override fun areItemsTheSame(
                    oldItem: ChatListItem,
                    newItem: ChatListItem
                ) = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: ChatListItem,
                    newItem: ChatListItem
                ) = oldItem == newItem
            }
    }
}

enum class MessageType {
    SENT, RECEIVED, HACK
}


class ChatAdapter :
    ListAdapter<ChatListItem, MessageViewHolder>(ChatListItem.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder =
        when (viewType) {
            MessageType.SENT.ordinal -> SentMessageViewHolder(
                SentMessageListItemBinding.inflate(
                    parent.inflater(),
                    parent,
                    false
                )
            )
            MessageType.RECEIVED.ordinal -> ReceivedMessageViewHolder(
                ReceivedMessageListItemBinding.inflate(
                    parent.inflater(),
                    parent,
                    false
                )
            )
            else -> HackViewHolder(parent.inflate(R.layout.blank_view))
        }


    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        getItem(position)?.let { holder.bindTo(it) }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.ordinal
    }
}

abstract class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bindTo(item: ChatListItem)
}

class SentMessageViewHolder(
    private val chatListItemBinding: SentMessageListItemBinding
) : MessageViewHolder(chatListItemBinding.root) {

    override fun bindTo(item: ChatListItem) =
        with(chatListItemBinding) {
            body.text = item.body
        }
}

class ReceivedMessageViewHolder(
    private val chatListItemBinding: ReceivedMessageListItemBinding
) : MessageViewHolder(chatListItemBinding.root) {

    override fun bindTo(item: ChatListItem) =
        with(chatListItemBinding) {
            body.text = item.body
        }
}

class HackViewHolder(view: View) : MessageViewHolder(view) {
    override fun bindTo(item: ChatListItem) {}
}