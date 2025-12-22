package project.c14230225.c14230235

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import project.c14230225.c14230235.databinding.FragmentDetailProductBinding

class DetailProductFragment : Fragment() {
    private var productId: String = ""
    private var productDetail: Sepatu? = null
    private var _binding: FragmentDetailProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Get Product ID safely from arguments
        productId = arguments?.getString("productId") ?: ""

        if (productId.isNotEmpty()) {
            loadProductData(productId)
        } else {
            Toast.makeText(requireContext(), "Error: Missing Product ID", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        // 2. Setup Buttons
        binding.btnChat.setOnClickListener {
            val sellerUsername = productDetail?.username
            if (!sellerUsername.isNullOrEmpty()) {
                val bundle = Bundle().apply {
                    putString("user2", sellerUsername)
                }
                findNavController().navigate(R.id.action_detailProductFragment_to_chattingFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "Seller information not available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnCart.setOnClickListener {
            Toast.makeText(requireContext(), "Added to Cart!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProductData(id: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("products")
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Map data to your Sepatu object
                    productDetail = Sepatu(
                        id = document.id,
                        username = document.getString("username") ?: "",
                        nama = document.getString("nama") ?: "No Name",
                        jenis = document.getString("jenis") ?: "",
                        ukuran = document.getString("ukuran") ?: "",
                        harga = document.getString("harga") ?: "0",
                        deskripsi = document.getString("deskripsi") ?: "",
                        image = document.getString("image") ?: ""
                    )

                    updateUI()
                } else {
                    Log.d("Firestore", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting document: ", exception)
            }
    }

    private fun updateUI() {
        productDetail?.let { product ->
            binding.tvProductName.text = product.nama
            binding.tvPrice.text = product.harga
            binding.tvDeskripsi.text = product.deskripsi

            // Set the Size text or handle size buttons here
            // Example: binding.btnSize38.text = product.ukuran

            // Load Image using Glide
            if (product.image.isNotEmpty()) {
                Glide.with(this)
                    .load(product.image)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(binding.imageProduct)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}