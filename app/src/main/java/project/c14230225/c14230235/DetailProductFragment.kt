package project.c14230225.c14230235

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import project.c14230225.c14230235.databinding.FragmentDetailProductBinding

class DetailProductFragment : Fragment() {
    private var productId: String = ""
    private var productDetail: Sepatu? = null
    private var _binding: FragmentDetailProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private var currentUserEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        currentUserEmail = requireActivity().intent.getStringExtra("email") ?: ""

        // Get Product ID from arguments
        productId = arguments?.getString("productId") ?: ""

        if (productId.isNotEmpty()) {
            loadProductData(productId)
        } else {
            Toast.makeText(requireContext(), "Error: Missing Product ID", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        // Setup Buttons
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

        // ✅ Updated Buy Button - sesuaikan dengan ID di XML
        binding.btnBuy.setOnClickListener {
            showBuyConfirmationDialog()
        }
    }

    private fun loadProductData(id: String) {
        db.collection("products")
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
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
                    checkIfAlreadyPurchased()
                } else {
                    Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting document: ", exception)
                Toast.makeText(requireContext(), "Error loading product", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfAlreadyPurchased() {
        // Cek apakah user sudah membeli produk ini
        db.collection("transactions")
            .whereEqualTo("buyerEmail", currentUserEmail)
            .whereEqualTo("productId", productId)
            .whereEqualTo("status", "completed")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Produk sudah dibeli
                    binding.btnBuy.isEnabled = false
                    binding.btnBuy.text = "Sudah Dibeli"
                    binding.btnBuy.alpha = 0.5f
                }
            }
    }

    private fun showBuyConfirmationDialog() {
        productDetail?.let { product ->
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Pembelian")
                .setMessage("Apakah Anda yakin ingin membeli ${product.nama} seharga ${product.harga}?")
                .setPositiveButton("Beli") { _, _ ->
                    processPurchase(product)
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun processPurchase(product: Sepatu) {
        // Show loading
        binding.btnBuy.isEnabled = false
        binding.btnBuy.text = "Memproses..."

        // 1. Create transaction record
        val transactionId = db.collection("transactions").document().id
        val transaction = Transaction(
            id = transactionId,
            buyerEmail = currentUserEmail,
            buyerUsername = MainActivity._UserSession.username,
            productId = product.id,
            productName = product.nama,
            productImage = product.image,
            productPrice = product.harga,
            productSize = product.ukuran,
            sellerUsername = product.username,
            purchaseDate = Timestamp.now(),
            status = "completed"
        )

        // 2. Save transaction to Firestore
        db.collection("transactions")
            .document(transactionId)
            .set(transaction)
            .addOnSuccessListener {
                // 3. Add product to user's purchased list
                db.collection("users")
                    .document(currentUserEmail)
                    .update("purchasedProducts", FieldValue.arrayUnion(product.id))
                    .addOnSuccessListener {
                        // 4. Mark product as sold
                        markProductAsSold(product.id)
                    }
                    .addOnFailureListener { e ->
                        // If field doesn't exist, create it
                        db.collection("users")
                            .document(currentUserEmail)
                            .update(mapOf("purchasedProducts" to listOf(product.id)))
                            .addOnSuccessListener {
                                markProductAsSold(product.id)
                            }
                            .addOnFailureListener {
                                showPurchaseError(e.message)
                            }
                    }
            }
            .addOnFailureListener { e ->
                showPurchaseError(e.message)
            }
    }

    private fun markProductAsSold(productId: String) {
        db.collection("products")
            .document(productId)
            .update("status", "sold")
            .addOnSuccessListener {
                showPurchaseSuccess()
            }
            .addOnFailureListener { e ->
                // Even if this fails, transaction is still recorded
                Log.e("Purchase", "Failed to mark as sold: ${e.message}")
                showPurchaseSuccess()
            }
    }

    private fun showPurchaseSuccess() {
        Toast.makeText(requireContext(), "Pembelian berhasil!", Toast.LENGTH_LONG).show()

        // Update UI
        binding.btnBuy.text = "Sudah Dibeli"
        binding.btnBuy.alpha = 0.5f

        // Navigate back to home
        findNavController().popBackStack()
    }

    private fun showPurchaseError(message: String?) {
        Toast.makeText(
            requireContext(),
            "Pembelian gagal: ${message ?: "Unknown error"}",
            Toast.LENGTH_SHORT
        ).show()

        // Reset button
        binding.btnBuy.isEnabled = true
        binding.btnBuy.text = "Add To Cart"
    }

    private fun updateUI() {
        productDetail?.let { product ->
            binding.tvProductName.text = product.nama
            binding.tvPrice.text = product.harga
            binding.tvDeskripsi.text = product.deskripsi

            // Load Image
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