package project.c14230225.c14230235

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ChatListAdapter(
    private val chatList: MutableList<ChatList>,
    private val onChatClick: (ChatList) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_list_item, parent, false)
        return ChatListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val chatItem = chatList[position]

        holder.tvUserEmail.text = chatItem.otherUserEmail
        holder.tvLastMessage.text = chatItem.lastMessage

        // Format timestamp
        chatItem.lastMessageTime?.let { timestamp ->
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            holder.tvTime.text = sdf.format(timestamp.toDate())
        }

        holder.itemView.setOnClickListener {
            onChatClick(chatItem)
        }
    }

    override fun getItemCount(): Int = chatList.size

    fun updateList(newList: List<ChatList>) {
        chatList.clear()
        chatList.addAll(newList)
        notifyDataSetChanged()
    }

    class ChatListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivUserAvatar: ImageView = view.findViewById(R.id.ivUserAvatar)
        val tvUserEmail: TextView = view.findViewById(R.id.tvUserEmail)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }
}