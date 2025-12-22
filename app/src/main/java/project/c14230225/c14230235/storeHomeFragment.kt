package project.c14230225.c14230235

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFAB()
        loadProducts()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        // Assuming you have created storeSepatuAdapter similarly to your previous adapter
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
                .actionStoreHomeFragmentToUploadSepatuFragment(requireActivity().intent.getStringExtra("email") ?: "")
            findNavController().navigate(action)
        }
    }

    private fun loadProducts() {
        // Ambil email dari session saat ini
        val currentUserEmail = arguments?.getString("email") ?: ""

        // Load products from Firestore
        db.collection("products")
            .whereEqualTo("username", currentUserEmail) // Filter berdasarkan email penjual
            .addSnapshotListener { result, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                productList.clear()
                if (result != null) {
                    for (document in result) {
                        // Konversi dokumen ke object Sepatu
                        val product = document.toObject(Sepatu::class.java)

                        // PENTING: Map ID dokumen ke property id agar bisa di-edit/delete nanti
                        product.id = document.id

                        productList.add(product)
                    }
                }
                updateUI()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        if (productList.isEmpty()) {
            binding.recyclerViewProducts.visibility = View.GONE
            // You might want to show a "No products" TextView here
        } else {
            binding.recyclerViewProducts.visibility = View.VISIBLE
            productAdapter.notifyDataSetChanged()
        }
    }

    private fun editProduct(product: Sepatu) {
        // Pass the productId to the upload fragment to repurpose it as an "Edit" screen
        val bundle = Bundle().apply {
            putString("productId", product.id)
        }
//        findNavController().navigate(R.id.action_storeHomeFragment_to_uploadSepatuFragment, bundle)
    }

    private fun deleteProduct(product: Sepatu) {
        // Build the confirmation dialog
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Produk?")
            .setMessage("Apakah Anda yakin ingin menghapus ${product.nama}? Tindakan ini tidak dapat dibatalkan.")
            .setNeutralButton("Batal") { dialog, _ ->
                // Just close the dialog
                dialog.dismiss()
            }
            .setPositiveButton("Hapus") { _, _ ->
                // Perform the actual deletion in Firestore
                db.collection("products")
                    .document(product.id) // Using the ID we mapped in loadProducts
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
                        // If you use addSnapshotListener in loadProducts(),
                        // the list will update automatically.
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Gagal menghapus: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}