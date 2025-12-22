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
            var txt = binding?.etMessage?.text.toString()
            if(txt != "") {
                val newChat = Chat(
                    "",
                    channelname,
                    user1,
                    user2,
                    Timestamp.now(),
                    txt
                )

                db.collection("chatting")
                    .add(newChat)
                    .addOnSuccessListener { document ->
                        Toast.makeText(context, "Message Sent", Toast.LENGTH_SHORT).show()
                        loadChat()
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
        print("debug channel name = " + channelname)
        db.collection("chatting")
            .whereEqualTo("channelname", channelname)
//            .orderBy("tanggal", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                chatList.clear()

                println("🔥 Fetched ${result.size()} products from Firestore")
                for (doc in result) {
                    val chat = Chat(
                        doc.id,
                        doc.get("channelname").toString(),
                        doc.get("user1").toString(),
                        doc.get("user2").toString(),
                        doc.getTimestamp("tanggal"),
                        doc.get("teks").toString()
                    )
                    chatList.add(chat)
                }
                adapter.updateList(chatList)
                print("debug = " + chatList.size.toString())
            }
            .addOnFailureListener { e ->
                println("❌ Error fetching products: $e")
            }
    }

}