package project.c14230225.c14230235

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private var emptyStateLayout: LinearLayout? = null  // ✅ Ubah jadi nullable
    private val chatListItems = mutableListOf<ChatList>()
    private val db = FirebaseFirestore.getInstance()
    private var currentUserEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserEmail = MainActivity._UserSession.email

        recyclerView = view.findViewById(R.id.rvChatList)
        emptyStateLayout = view.findViewById(R.id.tvEmptyState)

        if (emptyStateLayout == null) {
            Log.w("ChatListFragment", "⚠️ Empty state layout not found in XML")
        }

        adapter = ChatListAdapter(chatListItems) { chatItem ->
            // Navigate to chat fragment
            val bundle = Bundle().apply {
                putString("user2", chatItem.otherUserEmail)
            }
            findNavController().navigate(R.id.action_chatListFragment_to_chattingFragment, bundle)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadChatList()
    }

    private fun loadChatList() {
        Log.d("ChatListFragment", "Loading chats for: $currentUserEmail")

        db.collection("chatting")
            .orderBy("tanggal", Query.Direction.DESCENDING)
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Log.e("ChatListFragment", "Error loading chats", error)
                    showEmptyState(true)
                    return@addSnapshotListener
                }

                val channelMap = mutableMapOf<String, ChatList>()

                result?.forEach { doc ->
                    try {
                        val chat = doc.toObject(Chat::class.java)

                        // Skip if user2 is null or empty
                        if (chat.user2.isNullOrEmpty() || chat.user1.isNullOrEmpty()) {
                            Log.d("ChatListFragment", "⚠️ Skipping chat with null/empty users")
                            return@forEach
                        }

                        Log.d("ChatListFragment", "Processing chat: user1=${chat.user1}, user2=${chat.user2}, channel=${chat.channelname}")

                        // Check if current user is involved in this chat
                        val isUser1 = chat.user1 == currentUserEmail
                        val isUser2 = chat.user2 == currentUserEmail

                        if (isUser1 || isUser2) {
                            // Determine who is the other user
                            val otherUser = if (isUser1) {
                                chat.user2
                            } else {
                                chat.user1
                            }

                            Log.d("ChatListFragment", "✅ Match found! Other user: $otherUser")

                            // Only keep the latest message per channel
                            if (!channelMap.containsKey(chat.channelname)) {
                                channelMap[chat.channelname] = ChatList(
                                    channelname = chat.channelname,
                                    otherUserEmail = otherUser ?: "",
                                    lastMessage = chat.teks,
                                    lastMessageTime = chat.tanggal
                                )
                            }
                        } else {
                            Log.d("ChatListFragment", "❌ Not a match for current user")
                        }
                    } catch (e: Exception) {
                        Log.e("ChatListFragment", "Error processing document: ${doc.id}", e)
                    }
                }

                Log.d("ChatListFragment", "Total unique channels found: ${channelMap.size}")

                chatListItems.clear()
                chatListItems.addAll(
                    channelMap.values
                        .filter { it.otherUserEmail.isNotEmpty() }
                        .sortedByDescending { it.lastMessageTime }
                )
                adapter.updateList(chatListItems)

                showEmptyState(chatListItems.isEmpty())
            }
    }

    private fun showEmptyState(show: Boolean) {
        emptyStateLayout?.visibility = if (show) View.VISIBLE else View.GONE
        recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }
}