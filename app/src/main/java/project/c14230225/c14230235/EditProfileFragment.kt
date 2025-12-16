package project.c14230225.c14230235

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import project.c14230225.c14230235.databinding.FragmentEditProfileBinding
import project.c14230225.c14230235.databinding.FragmentRegisterBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [EditProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var db : FirebaseFirestore
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var originalEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore

        val args = EditProfileFragmentArgs.fromBundle(requireArguments())
        originalEmail = args.userEmail

        loadUserProfile(originalEmail)

        binding.btnSave.setOnClickListener {
            val updatedUser = User(
                email = binding.etEmail.text.toString(),
                username = binding.etUsername.text.toString(),
                namalengkap = binding.etFullName.text.toString(),
                password = "PASSWORD", // ga ngefek
                phonenumber = binding.etPhoneNumber.text.toString(),
                alamat = binding.etAddress.text.toString(),
                foto = binding.ivProfilePicture.toString()
            )

            editProfile(db, updatedUser)
                .addOnSuccessListener {
                    findNavController().popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Update failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadUserProfile(email: String) {
        db.collection("users")
            .document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get data from Firestore
                    val username = document.getString("username") ?: ""
                    val namalengkap = document.getString("namalengkap") ?: ""
                    val email = document.getString("email") ?: ""
                    val phonenumber = document.getString("phonenumber") ?: ""
                    val alamat = document.getString("alamat") ?: ""
                    val foto = document.getString("foto") ?: ""
                    // Update UI
                    binding.etUsername.setText(username)
                    binding.etFullName.setText(namalengkap)
                    binding.etEmail.setText(email)
                    binding.etPhoneNumber.setText(phonenumber)
                    // Show address or placeholder
                    if (alamat.isNotEmpty()) {
                        binding.etAddress.setText(alamat)
                        binding.etAddress.setTextColor(resources.getColor(android.R.color.black, null))
                    } else {
                        binding.etAddress.setText("No address added yet")
                        binding.etAddress.setTextColor(resources.getColor(android.R.color.darker_gray, null))
                    }
                    // Load profile picture
                    if (foto.isNotEmpty()) {
                        Glide.with(requireContext())
                            .load(foto)
                            .circleCrop()
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(binding.ivProfilePicture)
                    } else {
                        // Set default profile picture
                        binding.ivProfilePicture.setImageResource(R.drawable.ic_launcher_foreground)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editProfile(
        db: FirebaseFirestore,
        newUserDetail: User
    ) = db.collection("users")
        .document(originalEmail)
        .update(
            "username", newUserDetail.username,
            "namalengkap", newUserDetail.namalengkap,
            "phonenumber", newUserDetail.phonenumber,
            "alamat", newUserDetail.alamat
        )

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}