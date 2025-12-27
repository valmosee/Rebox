package project.c14230225.c14230235

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
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

        Log.d("DetailProduct", "Product ID: $productId")
        Log.d("DetailProduct", "Current user email: $currentUserEmail")

        if (productId.isNotEmpty()) {
            loadProductData(productId)
        } else {
            Toast.makeText(requireContext(), "Error: Missing Product ID", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        // Setup Buttons
        binding.btnChat.setOnClickListener {
            val sellerEmail = productDetail?.sellerEmail
            if (!sellerEmail.isNullOrEmpty()) {
                val bundle = Bundle().apply {
                    putString("user2", sellerEmail)
                }
                findNavController().navigate(R.id.action_detailProductFragment_to_chattingFragment, bundle)
            } else {
                Toast.makeText(requireContext(), "Seller information not available", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBuy.setOnClickListener {
            showBuyConfirmationDialog()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Navigate back
            findNavController().navigateUp()
        }
    }

    private fun loadProductData(id: String) {
        Log.d("DetailProduct", "Loading product with ID: $id")

        // ✅ FIXED: Use .document(id) instead of .whereEqualTo()
        db.collection("products")
            .document(id) // Direct document access using Firestore document ID
            .get()
            .addOnSuccessListener { document ->
                Log.d("DetailProduct", "Document exists: ${document.exists()}")

                if (document != null && document.exists()) {
                    // Map Firestore document to Sepatu object
                    productDetail = Sepatu(
                        id = document.id,
                        username = document.getString("username") ?: "",
                        nama = document.getString("nama") ?: "No Name",
                        jenis = document.getString("jenis") ?: "",
                        ukuran = document.getString("ukuran") ?: "",
                        harga = document.getString("harga") ?: "0",
                        deskripsi = document.getString("deskripsi") ?: "",
                        image = document.getString("image") ?: "",
                        sellerEmail = document.getString("sellerEmail") ?: "",
                        sellerId = document.getString("sellerId") ?: ""
                    )

                    Log.d("DetailProduct", "Product loaded: ${productDetail?.nama}")
                    updateUI()
                    checkIfAlreadyPurchased()
                } else {
                    Log.e("DetailProduct", "Product document not found")
                    Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DetailProduct", "Error getting document: ", exception)
                Toast.makeText(requireContext(), "Error loading product: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkIfAlreadyPurchased() {
        if (currentUserEmail.isEmpty()) {
            Log.w("DetailProduct", "Current user email is empty")
            return
        }

        Log.d("DetailProduct", "Checking if already purchased...")

        // Check if user already bought this product
        db.collection("transactions")
            .whereEqualTo("buyerEmail", currentUserEmail)
            .whereEqualTo("productId", productId)
            .whereEqualTo("status", "completed")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Log.d("DetailProduct", "Product already purchased")
                    // Product already bought
                    binding.btnBuy.isEnabled = false
                    binding.btnBuy.text = "Sudah Dibeli"
                    binding.btnBuy.alpha = 0.5f
                } else {
                    Log.d("DetailProduct", "Product not yet purchased")
                }
            }
            .addOnFailureListener { e ->
                Log.e("DetailProduct", "Error checking purchase status", e)
            }
    }

    private fun showBuyConfirmationDialog() {
        productDetail?.let { product ->
            AlertDialog.Builder(requireContext())
                .setTitle("Konfirmasi Pembelian")
                .setMessage("Apakah Anda yakin ingin membeli ${product.nama} seharga Rp ${product.harga}?")
                .setPositiveButton("Beli") { _, _ ->
                    processPurchase(product)
                }
                .setNegativeButton("Batal", null)
                .show()
        } ?: run {
            Toast.makeText(requireContext(), "Product data not loaded", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processPurchase(product: Sepatu) {
        if (currentUserEmail.isEmpty()) {
            Toast.makeText(requireContext(), "User email not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        binding.btnBuy.isEnabled = false
        binding.btnBuy.text = "Memproses..."

        // 1. Create transaction record
        val transactionId = db.collection("transactions").document().id
        val transaction = hashMapOf(
            "id" to transactionId,
            "buyerEmail" to currentUserEmail,
            "buyerUsername" to MainActivity._UserSession.username,
            "productId" to product.id,
            "productName" to product.nama,
            "productImage" to product.image,
            "productPrice" to product.harga,
            "productSize" to product.ukuran,
            "sellerUsername" to product.username,
            "sellerEmail" to product.sellerEmail,
            "purchaseDate" to Timestamp.now(),
            "status" to "completed"
        )

        // 2. Save transaction to Firestore
        db.collection("transactions")
            .document(transactionId)
            .set(transaction)
            .addOnSuccessListener {
                Log.d("Purchase", "Transaction saved successfully")
                // 3. Add product to user's purchased list
                db.collection("users")
                    .document(currentUserEmail)
                    .update("purchasedProducts", FieldValue.arrayUnion(product.id))
                    .addOnSuccessListener {
                        Log.d("Purchase", "Added to purchased products")
                        markProductAsSold(product.id)
                    }
                    .addOnFailureListener { e ->
                        Log.w("Purchase", "Field doesn't exist, creating it", e)
                        // If field doesn't exist, create it
                        db.collection("users")
                            .document(currentUserEmail)
                            .update(mapOf("purchasedProducts" to listOf(product.id)))
                            .addOnSuccessListener {
                                markProductAsSold(product.id)
                            }
                            .addOnFailureListener {
                                showPurchaseError(it.message)
                            }
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Purchase", "Failed to save transaction", e)
                showPurchaseError(e.message)
            }
    }

    private fun markProductAsSold(productId: String) {
        db.collection("products")
            .document(productId)
            .update("status", "sold")
            .addOnSuccessListener {
                Log.d("Purchase", "Product marked as sold")
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
            binding.tvPrice.text = "Rp ${product.harga}"
            binding.tvDeskripsi.text = product.deskripsi

            // Load Image
            if (product.image.isNotEmpty()) {
                Glide.with(this)
                    .load(product.image)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(binding.imageProduct)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}