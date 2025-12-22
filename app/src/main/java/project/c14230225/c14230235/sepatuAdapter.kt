package project.c14230225.c14230235

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class sepatuAdapter (private var listProduct: MutableList<Sepatu>) :
    RecyclerView.Adapter<sepatuAdapter.ProductViewHolder>() {

    override fun getItemCount(): Int {
        println("📊 getItemCount: ${listProduct.size}")
        return listProduct.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_recycle, parent, false)  // Your item layout
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        println("🎨 Binding position $position")
        val item = listProduct[position]
        holder.title.text = item.nama
        holder.price.text = item.harga
        Glide.with(holder.itemView.context)
            .load(item.image)
            .into(holder.img)
        holder.card.setOnClickListener {
            println(item.id)
            println(item.nama)
            val productId = item.id
            val bundle = Bundle().apply {
                putString("productId", productId)
            }
            context.findNavController().navigate(R.id.action_menuhome_to_detailProductFragment, bundle)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<Sepatu>) {
        println("📱 Adapter updateList called with ${newList.size} items")
        listProduct.clear()
        listProduct.addAll(newList)
        notifyDataSetChanged()
    }

    class ProductViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.imgProduct)
        val title = view.findViewById<TextView>(R.id.txtTitle)
        val price = view.findViewById<TextView>(R.id.txtPrice)
    }
}