package project.c14230225.c14230235

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class deletedAdapter(
    private val listSepatu: MutableList<Sepatu>
    ) : RecyclerView.Adapter<deletedAdapter.deletedViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): deletedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_recycle, parent, false)  // Your item layout
        return deletedViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: deletedViewHolder,
        position: Int
    ) {
        println("🎨 Binding position $position")
        val item = listSepatu[position]
        holder.title.text = item.nama
        holder.price.visibility = View.GONE
        Glide.with(holder.itemView.context)
            .load(item.image)
            .into(holder.img)
    }

    override fun getItemCount(): Int {
        return listSepatu.size
    }

    class deletedViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val img = view.findViewById<ImageView>(R.id.imgProduct)
        val title = view.findViewById<TextView>(R.id.txtTitle)
        val price = view.findViewById<TextView>(R.id.txtPrice)
    }
}