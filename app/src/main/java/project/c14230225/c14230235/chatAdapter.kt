package project.c14230225.c14230235

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
import com.bumptech.glide.Glide

class chatAdapter(private var listChat: MutableList<Chat>, var context: ChattingFragment) :
    RecyclerView.Adapter<chatAdapter.ChatViewHolder>() {

    override fun getItemCount(): Int {
        println("📊 getItemCount: ${listChat.size}")
        return listChat.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_recycle, parent, false)  // Your item layout
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = listChat[position]

        if (item.user1 == MainActivity._UserSession.email) {
            // I am the sender (user1) - Show RIGHT, Hide LEFT
            holder.kanan.visibility = View.VISIBLE
            holder.kiri.visibility = View.GONE

            holder.tv3.text = item.user1
            holder.tv4.text = item.teks
        } else {
            // The other person is the sender - Show LEFT, Hide RIGHT
            holder.kiri.visibility = View.VISIBLE
            holder.kanan.visibility = View.GONE

            holder.tv1.text = item.user1
            holder.tv2.text = item.teks
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Chat>) {
        println("📱 Adapter updateList called with ${newList.size} items")
        listChat.clear()
        listChat.addAll(newList)
        notifyDataSetChanged()
    }

    class ChatViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tv1 = view.findViewById<TextView>(R.id.tv1)
        val tv2 = view.findViewById<TextView>(R.id.tv2)
        val tv3 = view.findViewById<TextView>(R.id.tv3)
        val tv4 = view.findViewById<TextView>(R.id.tv4)
        val kiri = view.findViewById<ConstraintLayout>(R.id.kiri)
        val kanan = view.findViewById<ConstraintLayout>(R.id.kanan)
    }
}