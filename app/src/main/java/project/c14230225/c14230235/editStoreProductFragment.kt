package project.c14230225.c14230235

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import project.c14230225.c14230235.databinding.FragmentEditStoreProductBinding

class editStoreProductFragment : Fragment() {

    private var _binding: FragmentEditStoreProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private var pickedUri: Uri? = null

    private var productId: String = ""
    private var currentUserEmail: String = ""
    private var currentProduct: Sepatu? = null
    private var imageChanged: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditStoreProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        // Get arguments safely
        val args = editStoreProductFragmentArgs.fromBundle(requireArguments())
        productId = args.productId
        currentUserEmail = args.userEmail

        if (productId.isEmpty()) {
            Toast.makeText(context, "Product ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        setupSpinner()
        setupButtons()
        loadProductData()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            MainActivity.JenisSepatu
        )
        binding.upJenis.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnEditImage.setOnClickListener {
            pickFile.launch("image/*")
        }

        binding.btnSaveUpdatedSepatu.setOnClickListener {
            updateProduct()
        }
    }

    private fun loadProductData() {
        // Fetch specific document from collection
        db.collection("products")
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentProduct = document.toObject(Sepatu::class.java)?.apply {
                        id = document.id // Map document ID manually
                    }

                    currentProduct?.let { product ->
                        binding.upNama.setText(product.nama)
                        binding.upUkuran.setText(product.ukuran)
                        binding.upHarga.setText(product.harga)
                        binding.upDeskripsi.setText(product.deskripsi)

                        val jenisPosition = MainActivity.JenisSepatu.indexOf(product.jenis)
                        if (jenisPosition >= 0) {
                            binding.upJenis.setSelection(jenisPosition)
                        }

                        if (product.image.isNotEmpty()) {
                            Glide.with(this)
                                .load(product.image)
                                .placeholder(R.drawable.product_icon)
                                .into(binding.upImage)
                        }
                    }
                } else {
                    Toast.makeText(context, "Product tidak ditemukan", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
    }

    private val pickFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                pickedUri = uri
                imageChanged = true
                binding.upImage.setImageURI(uri)
            }
        }

    private fun updateProduct() {
        val product = currentProduct ?: return

        val nama = binding.upNama.text.toString().trim()
        val jenis = binding.upJenis.selectedItem.toString().trim()
        val ukuran = binding.upUkuran.text.toString().trim()
        val harga = binding.upHarga.text.toString().trim()
        val deskripsi = binding.upDeskripsi.text.toString().trim()

        // Validate mandatory inputs
        if (nama.isEmpty() || ukuran.isEmpty() || harga.isEmpty()) {
            Toast.makeText(context, "Mohon lengkapi data produk", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button to prevent double clicks during upload
        binding.btnSaveUpdatedSepatu.isEnabled = false
        binding.btnSaveUpdatedSepatu.text = "Updating..."

        lifecycleScope.launch {
            try {
                var imageUrl = product.image

                // Handle Cloudinary upload if image changed
                if (imageChanged && pickedUri != null) {
                    val uploadedUrl = uploadFileToCloudinary(requireContext(), pickedUri!!)
                    if (uploadedUrl != null) {
                        imageUrl = uploadedUrl
                    } else {
                        Toast.makeText(context, "Upload gambar gagal!", Toast.LENGTH_SHORT).show()
                        resetButton()
                        return@launch
                    }
                }

                val updates = hashMapOf<String, Any>(
                    "nama" to nama,
                    "jenis" to jenis,
                    "ukuran" to ukuran,
                    "harga" to harga,
                    "deskripsi" to deskripsi,
                    "image" to imageUrl
                )

                // Update Firestore document
                db.collection("products")
                    .document(productId)
                    .update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Produk berhasil diupdate!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Update gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                        resetButton()
                    }
            } catch (e: Exception) {
                Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                resetButton()
            }
        }
    }

    private fun resetButton() {
        // Reset button state on failure
        binding.btnSaveUpdatedSepatu.isEnabled = true
        binding.btnSaveUpdatedSepatu.text = "Save Updates"
    }

    private suspend fun uploadFileToCloudinary(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val cloudinary = Cloudinary(mapOf("cloud_name" to "dzpjccspp"))
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@withContext null

                val result = cloudinary.uploader().unsignedUpload(
                    bytes,
                    "UAS_Rebox",
                    ObjectUtils.asMap("resource_type", "auto", "folder", "projectUAS")
                )

                result["secure_url"] as? String
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}