    package project.c14230225.c14230235

    import android.annotation.SuppressLint
    import android.app.AlertDialog
    import android.os.Bundle
    import android.text.Editable
    import android.text.TextWatcher
    import androidx.fragment.app.Fragment
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Button
    import android.widget.TextView
    import androidx.navigation.fragment.findNavController
    import androidx.recyclerview.widget.GridLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.google.android.material.button.MaterialButton
    import com.google.android.material.chip.Chip
    import com.google.android.material.chip.ChipGroup
    import com.google.android.material.textfield.TextInputEditText
    import com.google.firebase.firestore.FirebaseFirestore
    import project.c14230225.c14230235.databinding.FragmentHomeBinding

    class HomeFragment : Fragment() {

        private lateinit var db: FirebaseFirestore
        private val sepatuList = mutableListOf<Sepatu>()
        private val filteredList = mutableListOf<Sepatu>()
        private lateinit var adapter: sepatuAdapter
        private val currentFilter = sepatuFilter()

        // Views
        private lateinit var searchEditText: TextInputEditText
        private lateinit var btnFilter: MaterialButton
        private lateinit var btnClearFilters: MaterialButton
        private lateinit var tvActiveFilters: TextView
        private lateinit var chipGroupActiveFilters: ChipGroup

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_home, container, false)
        }


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // Initialize views
            searchEditText = view.findViewById(R.id.searchEditText)
            btnFilter = view.findViewById(R.id.btnFilter)
            btnClearFilters = view.findViewById(R.id.btnClearFilters)
            tvActiveFilters = view.findViewById(R.id.tvActiveFilters)
            chipGroupActiveFilters = view.findViewById(R.id.chipGroupActiveFilters)
            var btnTambah: Button = view.findViewById<Button>(R.id.btnTambah)

            btnTambah.setOnClickListener {
                println("Masuk Add Product")
                findNavController().navigate(R.id.action_menuhome_to_UploadSepatuFragment)
            }

            db = FirebaseFirestore.getInstance()

            // ✅ Give adapter its own empty list
            val recyclerView = view.findViewById<RecyclerView>(R.id.rvProduct)
            adapter = sepatuAdapter(mutableListOf())  // ← Changed from filteredList
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            recyclerView.adapter = adapter

            setupSearch()

            btnFilter.setOnClickListener { showFilterDialog() }
            btnClearFilters.setOnClickListener { clearAllFilters() }

            loadProducts()


        }


        private fun setupSearch() {
            searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    currentFilter.searchQuery = s.toString()
                    applyFilters()
                }
            })
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun loadProducts() {
            db.collection("products")
                .get()
                .addOnSuccessListener { result ->
                    sepatuList.clear()

                    println("🔥 Fetched ${result.size()} products from Firestore")
                    for (doc in result) {
                        val product = doc.toObject(Sepatu::class.java)
                        println("📦 Product: ${product.nama}, ${product.harga}") // Add this
                        sepatuList.add(product)
                    }
                    println("✅ Total in sepatuList: ${sepatuList.size}") // Add this
                    applyFilters()
                }
                .addOnFailureListener { e ->
                    println("❌ Error fetching products: $e")
                }
        }

        private fun showFilterDialog() {
            val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            // Get views from dialog
            val chipGroupJenis = dialogView.findViewById<ChipGroup>(R.id.chipGroupJenis)
            val chipGroupUkuran = dialogView.findViewById<ChipGroup>(R.id.chipGroupUkuran)
            val chipGroupPrice = dialogView.findViewById<ChipGroup>(R.id.chipGroupPrice)
            val etMinPrice = dialogView.findViewById<TextInputEditText>(R.id.etMinPrice)
            val etMaxPrice = dialogView.findViewById<TextInputEditText>(R.id.etMaxPrice)
            val btnReset = dialogView.findViewById<MaterialButton>(R.id.btnResetFilter)
            val btnApply = dialogView.findViewById<MaterialButton>(R.id.btnApplyFilter)

            // Pre-select current filters
            preselectFilters(chipGroupJenis, chipGroupUkuran, etMinPrice, etMaxPrice)

            // Handle quick price filters
            chipGroupPrice.setOnCheckedStateChangeListener { group, checkedIds ->
                if (checkedIds.isNotEmpty()) {
                    when (checkedIds[0]) {
                        R.id.chipPrice1 -> {
                            etMinPrice.setText("")
                            etMaxPrice.setText("500000")
                        }
                        R.id.chipPrice2 -> {
                            etMinPrice.setText("500000")
                            etMaxPrice.setText("1000000")
                        }
                        R.id.chipPrice3 -> {
                            etMinPrice.setText("1000000")
                            etMaxPrice.setText("")
                        }
                    }
                }
            }

            // Reset button
            btnReset.setOnClickListener {
                chipGroupJenis.clearCheck()
                chipGroupUkuran.clearCheck()
                chipGroupPrice.clearCheck()
                etMinPrice.setText("")
                etMaxPrice.setText("")
            }

            // Apply button
            btnApply.setOnClickListener {
                // Get selected jenis
                currentFilter.jenisList.clear()
                for (i in 0 until chipGroupJenis.childCount) {
                    val chip = chipGroupJenis.getChildAt(i) as Chip
                    if (chip.isChecked) {
                        currentFilter.jenisList.add(chip.text.toString())
                    }
                }

                // Get selected ukuran
                currentFilter.ukuranList.clear()
                for (i in 0 until chipGroupUkuran.childCount) {
                    val chip = chipGroupUkuran.getChildAt(i) as Chip
                    if (chip.isChecked) {
                        currentFilter.ukuranList.add(chip.text.toString())
                    }
                }

                // Get price range
                currentFilter.minPrice = etMinPrice.text.toString().toIntOrNull()
                currentFilter.maxPrice = etMaxPrice.text.toString().toIntOrNull()

                applyFilters()
                updateFilterUI()
                dialog.dismiss()
            }

            dialog.show()
        }

        private fun preselectFilters(
            chipGroupJenis: ChipGroup,
            chipGroupUkuran: ChipGroup,
            etMinPrice: TextInputEditText,
            etMaxPrice: TextInputEditText
        ) {
            // Preselect jenis chips
            for (i in 0 until chipGroupJenis.childCount) {
                val chip = chipGroupJenis.getChildAt(i) as Chip
                chip.isChecked = currentFilter.jenisList.contains(chip.text.toString())
            }

            // Preselect ukuran chips
            for (i in 0 until chipGroupUkuran.childCount) {
                val chip = chipGroupUkuran.getChildAt(i) as Chip
                chip.isChecked = currentFilter.ukuranList.contains(chip.text.toString())
            }

            // Set price values
            currentFilter.minPrice?.let { etMinPrice.setText(it.toString()) }
            currentFilter.maxPrice?.let { etMaxPrice.setText(it.toString()) }
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun applyFilters() {
            // ✅ Create a NEW list for filtered results
            val newList = mutableListOf<Sepatu>()

            println("🔍 Applying filters...")
            println("   Search: '${currentFilter.searchQuery}'")
            println("   Jenis: ${currentFilter.jenisList}")
            println("   Ukuran: ${currentFilter.ukuranList}")
            println("   Price: ${currentFilter.minPrice} - ${currentFilter.maxPrice}")
            println("   Total products to filter: ${sepatuList.size}")

            for (sepatu in sepatuList) {
                var matches = true

                // Search query filter (nama)
                if (currentFilter.searchQuery.isNotEmpty()) {
                    if (!sepatu.nama.contains(currentFilter.searchQuery, ignoreCase = true)) {
                        matches = false
                    }
                }

                // Jenis filter
                if (currentFilter.jenisList.isNotEmpty()) {
                    if (!currentFilter.jenisList.contains(sepatu.jenis)) {
                        matches = false
                    }
                }

                // Ukuran filter
                if (currentFilter.ukuranList.isNotEmpty()) {
                    if (!currentFilter.ukuranList.contains(sepatu.ukuran)) {
                        matches = false
                    }
                }

                // Price filter
                val price = sepatu.harga.replace("Rp", "").replace(".", "").trim().toIntOrNull() ?: 0
                currentFilter.minPrice?.let { min ->
                    if (price < min) matches = false
                }
                currentFilter.maxPrice?.let { max ->
                    if (price > max) matches = false
                }

                if (matches) {
                    newList.add(sepatu)  // ← Add to NEW list
                }
            }

            println("🎯 Final filtered list size: ${newList.size}")
            adapter.updateList(newList)  // ← Pass the new list
        }

        private fun updateFilterUI() {
            val activeFilterCount = getActiveFilterCount()

            if (activeFilterCount > 0) {
                tvActiveFilters.visibility = View.VISIBLE
                btnClearFilters.visibility = View.VISIBLE
                tvActiveFilters.text = "$activeFilterCount filter aktif"

                updateActiveFilterChips()
            } else {
                tvActiveFilters.visibility = View.GONE
                btnClearFilters.visibility = View.GONE
                chipGroupActiveFilters.visibility = View.GONE
            }
        }

        private fun getActiveFilterCount(): Int {
            var count = 0
            if (currentFilter.jenisList.isNotEmpty()) count += currentFilter.jenisList.size
            if (currentFilter.ukuranList.isNotEmpty()) count += currentFilter.ukuranList.size
            if (currentFilter.minPrice != null || currentFilter.maxPrice != null) count++
            return count
        }

        private fun updateActiveFilterChips() {
            chipGroupActiveFilters.removeAllViews()

            if (getActiveFilterCount() == 0) {
                chipGroupActiveFilters.visibility = View.GONE
                return
            }

            chipGroupActiveFilters.visibility = View.VISIBLE

            // Add jenis chips
            for (jenis in currentFilter.jenisList) {
                addFilterChip(jenis, "jenis")
            }

            // Add ukuran chips
            for (ukuran in currentFilter.ukuranList) {
                addFilterChip("Ukuran $ukuran", "ukuran")
            }

            // Add price chip
            if (currentFilter.minPrice != null || currentFilter.maxPrice != null) {
                val priceText = when {
                    currentFilter.minPrice != null && currentFilter.maxPrice != null ->
                        "Rp ${currentFilter.minPrice} - ${currentFilter.maxPrice}"
                    currentFilter.minPrice != null ->
                        "> Rp ${currentFilter.minPrice}"
                    else ->
                        "< Rp ${currentFilter.maxPrice}"
                }
                addFilterChip(priceText, "price")
            }
        }

        private fun addFilterChip(text: String, type: String) {
            val chip = Chip(requireContext())
            chip.text = text
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                when (type) {
                    "jenis" -> currentFilter.jenisList.remove(text)
                    "ukuran" -> currentFilter.ukuranList.remove(text.replace("Ukuran ", ""))
                    "price" -> {
                        currentFilter.minPrice = null
                        currentFilter.maxPrice = null
                    }
                }
                applyFilters()
                updateFilterUI()
            }
            chipGroupActiveFilters.addView(chip)
        }

        private fun clearAllFilters() {
            currentFilter.jenisList.clear()
            currentFilter.ukuranList.clear()
            currentFilter.minPrice = null
            currentFilter.maxPrice = null
            searchEditText.setText("")
            applyFilters()
            updateFilterUI()
        }
    }