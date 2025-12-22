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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import project.c14230225.c14230235.databinding.FragmentUploadSepatuBinding
class UploadSepatuFragment : Fragment() {
    var binding: FragmentUploadSepatuBinding? = null
    lateinit var db: FirebaseFirestore
    lateinit var pickedUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUploadSepatuBinding.inflate(inflater, container, false)

        db = FirebaseFirestore.getInstance()

        binding!!.upImage.setImageResource(R.drawable.product_icon)

        var dp = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            MainActivity.JenisSepatu
        )
        binding!!.upJenis.adapter = dp

        binding!!.btnAddImage.setOnClickListener {
            pickFile.launch("*/*")
        }

        binding!!.btnUploadSepatu.setOnClickListener {
            if (!::pickedUri.isInitialized) {
                Toast.makeText(context, "Tambahkan gambar dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val url = uploadFileToCloudinary(requireContext(), pickedUri)
                if (url != null) {
                    Log.d("Cloudinary", "Uploaded URL: $url")
                    Toast.makeText(context, "Upload sukses!", Toast.LENGTH_SHORT).show()

                    val _upNama = binding!!.upNama.text.toString().trim()
                    val _upJenis = binding!!.upJenis.selectedItem.toString().trim()
                    val _upUkuran = binding!!.upUkuran.text.toString().trim()
                    val _upHarga = binding!!.upHarga.text.toString().trim()
                    val _upDeskripsi = binding!!.upDeskripsi.text.toString().trim()

                    var newSepatu = Sepatu(
                        "",
                        MainActivity._UserSession.email,
                        _upNama,
                        _upJenis,
                        _upUkuran,
                        _upHarga,
                        _upDeskripsi,
                        url
                    )

                    db.collection("products")
                        .add(newSepatu)
                        .addOnSuccessListener { documentReference ->
                            Toast.makeText(
                                context, "Sukses : Product added with ID: ${documentReference.id}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context, "Error Adding Document. Try Again!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Toast.makeText(context, "Upload gagal!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return binding!!.root
    }

    private val pickFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                Log.d("Cloudinary", "URL: $uri")
                pickedUri = uri
                binding!!.upImage.setImageURI(uri)
            }
        }
    suspend fun uploadFileToCloudinary(context: Context, uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val cloudinary = Cloudinary(
                    mapOf(
                        "cloud_name" to "dzpjccspp"
                    )
                )

                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream!!.readBytes()

                val result = cloudinary.uploader().unsignedUpload(
                    bytes,
                    "UAS_Rebox",
                    ObjectUtils.asMap(
                        "resource_type", "auto",
                        "folder", "projectUAS"
                    )
                )

                Log.d("cloudinary result", result["secure_url"] as String)
                result["secure_url"] as String?
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}