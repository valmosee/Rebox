package project.c14230225.c14230235

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import project.c14230225.c14230235.databinding.FragmentDetailProductBinding
import project.c14230225.c14230235.databinding.FragmentHomeBinding

class DetailProductFragment : Fragment() {
    var productId = ""
    var productDetail: Sepatu? = null
    var binding: FragmentDetailProductBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menuchat, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_menu_chat -> {
                val user2 = productDetail?.username
                val bundle = Bundle().apply {
                    putString("user2", user2)
                }
                findNavController().navigate(R.id.action_detailProductFragment_to_chattingFragment, bundle)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailProductBinding.inflate(inflater, container, false)

        productId = arguments?.getString("productId") ?: ""
        println("parameter productid")
        println(productId)

        val db = FirebaseFirestore.getInstance()

        db.collection("products")
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    productDetail = Sepatu(
                        document.id,
                        document.get("username").toString(),
                        document.get("nama").toString(),
                        document.get("jenis").toString(),
                        document.get("ukuran").toString(),
                        document.get("harga").toString(),
                        document.get("deskripsi").toString(),
                        document.get("image").toString()
                    )

                    binding?.tvProductName?.setText(productDetail?.nama)
                } else {
                    println("Dokumen tidak ditemukan")
                }
            }
            .addOnFailureListener { exception ->
                println("Error mengambil dokumen: $exception")
            }

        return binding!!.root
    }

}