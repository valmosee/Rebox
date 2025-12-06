package project.c14230225.c14230235

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class productAdapter (private var listProduct: MutableList<Product>) :
    RecyclerView.Adapter<productAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_recycle, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ProductViewHolder,
        position: Int
    ) {
        val item = listProduct[position]
        holder.title.text = item.title
        holder.price.text = item.price
        Glide.with(holder.itemView.context)
            .load(item.image) // the URL or file path of your image
            .into(holder.img) // your ImageView
    }

    override fun getItemCount(): Int {
        return listProduct.size
    }

    class ProductViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.imgProduct)
        val title = view.findViewById<TextView>(R.id.txtTitle)
        val price = view.findViewById<TextView>(R.id.txtPrice)
    }
}