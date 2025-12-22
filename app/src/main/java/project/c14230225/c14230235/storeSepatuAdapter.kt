package project.c14230225.c14230235

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class storeSepatuAdapter(
    private val products: List<Sepatu>,
    private val onEditClick: (Sepatu) -> Unit,
    private val onDeleteClick: (Sepatu) -> Unit
) : RecyclerView.Adapter<storeSepatuAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditProduct)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_store_recycle, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.tvProductName.text = product.nama
        holder.tvProductPrice.text = "Rp ${String.format("%,d", product.harga.toInt())}"

         Glide.with(holder.itemView.context)
             .load(product.image)
             .into(holder.ivProductImage)

        holder.btnEdit.setOnClickListener { onEditClick(product) }
        holder.btnDelete.setOnClickListener { onDeleteClick(product) }
    }

    override fun getItemCount() = products.size
}