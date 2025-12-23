package project.c14230225.c14230235

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import project.c14230225.c14230235.databinding.FragmentUploadSepatuBinding

class UploadSepatuFragment : Fragment() {
    private var _binding: FragmentUploadSepatuBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var pickedUri: Uri

    private var currentUserEmail: String = ""
    private var currentUsername: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadSepatuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        // Get email from arguments
        currentUserEmail = requireActivity().intent.getStringExtra("email") ?: ""

        if (currentUserEmail.isEmpty()) {
            Toast.makeText(context, "Email tidak ditemukan", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        // Load username
        loadUsername()

        binding.upImage.setImageResource(R.drawable.product_icon)

        val dp = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            MainActivity.JenisSepatu
        )
        binding.upJenis.adapter = dp

        binding.btnAddImage.setOnClickListener {
            pickFile.launch("image/*") // Changed to only allow images
        }

        binding.btnUploadSepatu.setOnClickListener {
            uploadProduct()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            // Navigate back
            findNavController().navigateUp()
        }
    }

    private fun loadUsername() {
        db.collection("users")
            .document(currentUserEmail)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    currentUsername = document.getString("username") ?: ""
                    Log.d("UploadSepatu", "Username loaded: $currentUsername")
                } else {
                    Toast.makeText(context, "User data tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("UploadSepatu", "Error loading username", e)
                Toast.makeText(context, "Error loading user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProduct() {
        // Validate image
        if (!::pickedUri.isInitialized) {
            Toast.makeText(context, "Tambahkan gambar dulu!", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate username is loaded
        if (currentUsername.isEmpty()) {
            Toast.makeText(context, "Mohon tunggu, loading user data...", Toast.LENGTH_SHORT).show()
            return
        }

        // Get input values
        val _upNama = binding.upNama.text.toString().trim()
        val _upJenis = binding.upJenis.selectedItem.toString().trim()
        val _upUkuran = binding.upUkuran.text.toString().trim()
        val _upHarga = binding.upHarga.text.toString().trim()
        val _upDeskripsi = binding.upDeskripsi.text.toString().trim()

        // Validate inputs
        if (_upNama.isEmpty()) {
            binding.upNama.error = "Nama produk wajib diisi"
            return
        }
        if (_upUkuran.isEmpty()) {
            binding.upUkuran.error = "Ukuran wajib diisi"
            return
        }
        if (_upHarga.isEmpty()) {
            binding.upHarga.error = "Harga wajib diisi"
            return
        }

        // Show loading (optional - add a ProgressBar in your layout)
        binding.btnUploadSepatu.isEnabled = false
        binding.btnUploadSepatu.text = "Uploading..."

        lifecycleScope.launch {
            try {
                // Upload image to Cloudinary
                val url = uploadFileToCloudinary(requireContext(), pickedUri)

                if (url != null) {
                    Log.d("Cloudinary", "Uploaded URL: $url")

                    // Create Sepatu object with proper data
                    // Note: Don't set 'id' here - Firestore will auto-generate it
                    val newSepatu = hashMapOf(
                        "nama" to _upNama,
                        "jenis" to _upJenis,
                        "ukuran" to _upUkuran,
                        "harga" to _upHarga,
                        "deskripsi" to _upDeskripsi,
                        "image" to url,
                        "username" to currentUsername,
                        "sellerEmail" to currentUserEmail,
                        "sellerId" to currentUserEmail
                    )

                    // Add to Firestore
                    db.collection("products")
                        .add(newSepatu)
                        .addOnSuccessListener { documentReference ->
                            Log.d("UploadSepatu", "Product added with ID: ${documentReference.id}")
                            Toast.makeText(
                                context,
                                "Produk berhasil ditambahkan!",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        }
                        .addOnFailureListener { e ->
                            Log.e("UploadSepatu", "Error adding product", e)
                            Toast.makeText(
                                context,
                                "Gagal menambahkan produk: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Re-enable button
                            binding.btnUploadSepatu.isEnabled = true
                            binding.btnUploadSepatu.text = "Upload Sepatu"
                        }
                } else {
                    Toast.makeText(context, "Upload gambar gagal!", Toast.LENGTH_SHORT).show()
                    // Re-enable button
                    binding.btnUploadSepatu.isEnabled = true
                    binding.btnUploadSepatu.text = "Upload Sepatu"
                }
            } catch (e: Exception) {
                Log.e("UploadSepatu", "Error in upload process", e)
                Toast.makeText(
                    context,
                    "Terjadi kesalahan: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                // Re-enable button
                binding.btnUploadSepatu.isEnabled = true
                binding.btnUploadSepatu.text = "Upload Sepatu"
            }
        }
    }

    private val pickFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                Log.d("UploadSepatu", "Image URI: $uri")
                pickedUri = uri
                binding.upImage.setImageURI(uri)
            }
        }

    private suspend fun uploadFileToCloudinary(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val cloudinary = Cloudinary(
                    mapOf(
                        "cloud_name" to "dzpjccspp"
                    )
                )

                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()

                if (bytes == null) {
                    Log.e("Cloudinary", "Failed to read file")
                    return@withContext null
                }

                val result = cloudinary.uploader().unsignedUpload(
                    bytes,
                    "UAS_Rebox",
                    ObjectUtils.asMap(
                        "resource_type", "auto",
                        "folder", "projectUAS"
                    )
                )

                val secureUrl = result["secure_url"] as? String
                Log.d("Cloudinary", "Upload result: $secureUrl")
                secureUrl
            } catch (e: Exception) {
                Log.e("Cloudinary", "Upload failed", e)
                e.printStackTrace()
                null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}