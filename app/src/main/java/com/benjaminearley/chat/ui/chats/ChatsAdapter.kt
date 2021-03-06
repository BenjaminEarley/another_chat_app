package com.benjaminearley.chat.ui.chats

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.benjaminearley.chat.R
import com.benjaminearley.chat.databinding.ChatListItemBinding
import com.benjaminearley.chat.util.inflater

data class ChatsListItem(val id: String, val name: String) {
    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ChatsListItem> =
            object : DiffUtil.ItemCallback<ChatsListItem>() {
                override fun areItemsTheSame(
                    oldItem: ChatsListItem,
                    newItem: ChatsListItem
                ) = oldItem.id == newItem.id

                override fun areContentsTheSame(
                    oldItem: ChatsListItem,
                    newItem: ChatsListItem
                ) = oldItem == newItem
            }
    }
}

class ChatsAdapter(private val tapListener: (ChatsListItem) -> Unit) :
    ListAdapter<ChatsListItem, ChatViewHolder>(ChatsListItem.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder =
        ChatViewHolder(ChatListItemBinding.inflate(parent.inflater(), parent, false))

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val holderTapListener: (Int) -> Unit = { getItem(it)?.let(tapListener) }
        getItem(position)?.let { holder.bindTo(it, holderTapListener) } ?: holder.clear()
    }
}

class ChatViewHolder(private val chatListItemBinding: ChatListItemBinding) :
    RecyclerView.ViewHolder(chatListItemBinding.root) {

    fun clear() =
        with(chatListItemBinding) {
            chatItem.setOnClickListener(null)
        }

    fun bindTo(item: ChatsListItem, tapListener: (Int) -> Unit) =
        with(chatListItemBinding) {
            if (item.id == "top") {
                itemView.findViewById<LinearLayout>(R.id.chat_item).layoutParams.height = 0
                return
            }
            name.text = item.name
            chatItem.setOnClickListener { tapListener(adapterPosition) }
        }
}
