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
import project.c14230225.c14230235.databinding.FragmentChatListBinding
import project.c14230225.c14230235.databinding.FragmentStoreBinding

class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null

    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatListAdapter
    private var emptyStateLayout: LinearLayout? = null
    private val chatListItems = mutableListOf<ChatList>()
    private val db = FirebaseFirestore.getInstance()
    private var currentUserEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        currentUserEmail = MainActivity._UserSession.email

        recyclerView = view.findViewById(R.id.rvChatList)
        loadChatList()
    }

    private fun loadChatList() {
        Log.d("ChatListFragment", "Loading chats for: $currentUserEmail")

        db.collection("chatlist")
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Log.e("ChatListFragment", "Error loading chats", error)
                    return@addSnapshotListener
                }

                val channelMap = mutableMapOf<String, ChatList>()

                result?.forEach { doc ->
                    try {
                        val chat = doc.toObject(Chat::class.java)

                        if (chat.user1 == MainActivity._UserSession.email){
                            channelMap[chat.channelname] = ChatList(
                                user1 = chat.user1,
                                user2 = chat.user2,
                                lastMessage = chat.teks,
                                lastMessageTime = chat.tanggal
                            )
                        } else if (chat.user2 == MainActivity._UserSession.email){
                            channelMap[chat.channelname] = ChatList(
                                user1 = chat.user2,
                                user2 = chat.user1,
                                lastMessage = chat.teks,
                                lastMessageTime = chat.tanggal
                            )
                        }

                    } catch (e: Exception) {
                        Log.e("ChatListFragment", "Error processing document: ${doc.id}", e)
                    }
                }

                Log.d("ChatListFragment", "Total unique channels found: ${channelMap.size}")

                chatListItems.clear()
                chatListItems.addAll(
                    channelMap.values
                        .sortedByDescending { it.lastMessageTime }
                )
                Log.d("ChatListFragment", chatListItems.toString())
                Log.d("ChatListFragment", "chatlistitems = " + chatListItems.size.toString())

                adapter = ChatListAdapter(chatListItems) { chatItem ->
                    // Navigate to chat fragment
                    val bundle = Bundle().apply {
                        putString("user2", chatItem.user2)
                    }
                    findNavController().navigate(R.id.action_chatListFragment_to_chattingFragment, bundle)
                }

                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = adapter
            }
    }
}