package project.c14230225.c14230235

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val transactions: MutableList<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProductImage: ImageView = view.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = view.findViewById(R.id.tvProductPrice)
        val tvProductSize: TextView = view.findViewById(R.id.tvProductSize)
        val tvSellerName: TextView = view.findViewById(R.id.tvSellerName)
        val tvPurchaseDate: TextView = view.findViewById(R.id.tvPurchaseDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.tvProductName.text = transaction.productName
        holder.tvProductPrice.text = transaction.productPrice
        holder.tvProductSize.text = "Ukuran: ${transaction.productSize}"
        holder.tvSellerName.text = "Seller: ${transaction.sellerUsername}"
        holder.tvStatus.text = transaction.status.uppercase()

        // Format date
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val date = transaction.purchaseDate.toDate()
        holder.tvPurchaseDate.text = dateFormat.format(date)

        // Load product image
        if (transaction.productImage.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(transaction.productImage)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.ivProductImage)
        } else {
            holder.ivProductImage.setImageResource(R.drawable.ic_launcher_background)
        }

        // Set status color
        when (transaction.status.lowercase()) {
            "completed" -> {
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            }
            "pending" -> {
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_orange_dark))
            }
            "cancelled" -> {
                holder.tvStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
            }
        }
    }

    override fun getItemCount(): Int = transactions.size
}