package project.c14230225.c14230235

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import project.c14230225.c14230235.databinding.FragmentChattingBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ChattingFragment : Fragment() {

    var binding: FragmentChattingBinding? = null
    var user2: String = ""
    var user1: String = ""
    private val chatList = mutableListOf<Chat>()

    lateinit var adapter: chatAdapter

    private lateinit var db: FirebaseFirestore
    var channelname = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChattingBinding.inflate(inflater, container, false)

        user1 = MainActivity._UserSession.email;
        user2 = arguments?.getString("user2") ?: ""
        channelname = user1 + user2
        print("debug c1 = " + channelname)
        if(user1 > user2) {
            channelname = user2 + user1
        }

        val db = FirebaseFirestore.getInstance()

        binding?.btnSend?.setOnClickListener {
            val txt = binding?.etMessage?.text.toString().trim()
            if (txt.isNotEmpty()) {
                val newChat = Chat(
                    id = "",
                    channelname = channelname,
                    user1 = user1, // The logged-in person (Sender)
                    user2 = user2, // The other person (Receiver)
                    tanggal = Timestamp.now(),
                    teks = txt
                )

                db.collection("chatting").add(newChat)
                    .addOnSuccessListener {
                        binding?.etMessage?.text?.clear() // Clear input after sending
                    }
            }
        }

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        // Give adapter its own empty list
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvChats)
        adapter = chatAdapter(mutableListOf(), this as ChattingFragment)  // ← Changed from filteredList
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
        recyclerView.adapter = adapter

        loadChat()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadChat() {
        // Change .get() to .addSnapshotListener for real-time talk-back
        db.collection("chatting")
            .whereEqualTo("channelname", channelname)
            .orderBy("tanggal", Query.Direction.ASCENDING) // Order by time
            .addSnapshotListener { result, e ->
                if (e != null) return@addSnapshotListener

                chatList.clear()
                for (doc in result!!) {
                    val chat = doc.toObject(Chat::class.java).apply { id = doc.id }
                    chatList.add(chat)
                }
                adapter.updateList(chatList)

                // Auto-scroll to bottom so User 2 sees the latest reply
                binding?.rvChats?.scrollToPosition(chatList.size - 1)
            }
    }

}