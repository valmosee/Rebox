package project.c14230225.c14230235

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import project.c14230225.c14230235.databinding.FragmentStoreBinding

class storeHomeFragment : Fragment() {

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: storeSepatuAdapter
    private val productList = mutableListOf<Sepatu>()

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
            // Navigate to Add Product screen
            // TODO: Implement navigation to AddProductFragment
            Toast.makeText(requireContext(), "Add Product clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProducts() {
        // TODO: Load products from database where sellerId matches current user
        // For now, using dummy data

        // Example:
        // val currentUserId = getCurrentUserId() // Get from SharedPreferences or ViewModel
        // viewModel.getProductsBySeller(currentUserId).observe(viewLifecycleOwner) { products ->
        //     productList.clear()
        //     productList.addAll(products)
        //     updateUI()
        // }

        updateUI()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        if (productList.isEmpty()) {
            binding.recyclerViewProducts.visibility = View.GONE
        } else {
            binding.recyclerViewProducts.visibility = View.VISIBLE
            productAdapter.notifyDataSetChanged()
        }
    }

    private fun editProduct(product: Sepatu) {
        // TODO: Navigate to Edit Product screen
        Toast.makeText(requireContext(), "Edit: ${product.nama}", Toast.LENGTH_SHORT).show()
    }

    private fun deleteProduct(product: Sepatu) {
        // TODO: Show confirmation dialog and delete from database
        Toast.makeText(requireContext(), "Delete: ${product.nama}", Toast.LENGTH_SHORT).show()
        productList.remove(product)
        updateUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}