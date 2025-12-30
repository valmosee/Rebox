package project.c14230225.c14230235

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import project.c14230225.c14230235.databinding.FragmentStoreBinding

class storeHomeFragment : Fragment() {

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: storeSepatuAdapter
    private val productList = mutableListOf<Sepatu>()
    private val db = FirebaseFirestore.getInstance()

    // Add this variable
    private var currentUserEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = storeHomeFragmentArgs.fromBundle(requireArguments())
        currentUserEmail = args.userEmail

        if (currentUserEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Email tidak ditemukan", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack() // Close StoreActivity and go back
            return
        }

        Log.d("StoreFragment", "Current user email: $currentUserEmail")

        setupToolbar()
        setupRecyclerView()
        setupFAB()
        loadProducts()

        binding.btnChat.setOnClickListener {
            val action = storeHomeFragmentDirections
                .actionStoreHomeFragmentToChatListFragment()
            findNavController().navigate(action)
        }

        binding.btnDelHistory.setOnClickListener {
            val action = storeHomeFragmentDirections
                .actionStoreHomeFragmentToDeletedFragment()
            findNavController().navigate(action)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Navigate back
            findNavController().navigateUp()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack() // Close StoreActivity and go back
        }
    }

    private fun setupRecyclerView() {
        productAdapter = storeSepatuAdapter(
            productList,
            onEditClick = { product -> editProduct(product) },
            onDeleteClick = { product -> deleteProduct(product) }
        )

        binding.recyclerViewProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun setupFAB() {
        binding.fabAddProduct.setOnClickListener {
            // Navigate to UploadSepatuFragment
            val action = storeHomeFragmentDirections
                .actionStoreHomeFragmentToUploadSepatuFragment(currentUserEmail)
            findNavController().navigate(action)
        }
    }

    private fun loadProducts() {
        Log.d("StoreFragment", "Loading products for email: $currentUserEmail")

        db.collection("products")
            .whereEqualTo("sellerEmail", currentUserEmail) // Use sellerEmail for querying
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Log.e("StoreFragment", "Error loading products", error)
                    Toast.makeText(
                        requireContext(),
                        "Error loading products: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }

                productList.clear()

                if (result != null && !result.isEmpty) {
                    Log.d("StoreFragment", "Found ${result.size()} products")
                    for (document in result) {
                        val product = document.toObject(Sepatu::class.java)
                        product.id = document.id // IMPORTANT: Store the Firestore document ID
                        productList.add(product)
                        Log.d("StoreFragment", "Added product: ${product.nama}")
                    }
                } else {
                    Log.d("StoreFragment", "No products found")
                }

                updateUI()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        // 1. Safety check: ensure fragment is attached and binding exists
        if (!isAdded || _binding == null) {
            Log.d("StoreFragment", "updateUI skipped: Fragment not attached or binding is null")
            return
        }

        Log.d("StoreFragment", "Updating UI with ${productList.size} products")

        // 2. Access binding safely
        if (productList.isEmpty()) {
            binding.recyclerViewProducts.visibility = View.GONE
        } else {
            binding.recyclerViewProducts.visibility = View.VISIBLE
            productAdapter.notifyDataSetChanged()
        }
    }

    private fun editProduct(product: Sepatu) {
        Toast.makeText(requireContext(), "Edit: ${product.nama}", Toast.LENGTH_SHORT).show()

        val action = storeHomeFragmentDirections
            .actionStoreHomeFragmentToEditStoreProductFragment(
                productId = product.id,
                userEmail = currentUserEmail
            )
        findNavController().navigate(action)
    }

    private fun deleteProduct(product: Sepatu) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Produk?")
            .setMessage("Apakah Anda yakin ingin menghapus ${product.nama}? Tindakan ini tidak dapat dibatalkan.")
            .setNeutralButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Hapus") { _, _ ->
                // First archive, then delete
                db.collection("deleted")
                    .document(product.id)  // Use ID instead of title for consistency
                    .set(product)  // Add deletion timestamp
                    .addOnSuccessListener {
                        // Only delete from products after successful archive
                        db.collection("products")
                            .document(product.id)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Produk berhasil dihapus",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Gagal menghapus dari produk: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Gagal mengarsipkan: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}