package project.c14230225.c14230235

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import project.c14230225.c14230235.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {

    private var binding: FragmentChangePasswordBinding? = null
    private lateinit var db: FirebaseFirestore
    private var userEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        userEmail = requireActivity().intent.getStringExtra("email") ?: ""

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding?.btnChangePassword?.setOnClickListener {
            changePassword()
        }

        binding?.btnCancel?.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun changePassword() {
        val oldPassword = binding?.etOldPassword?.text.toString().trim()
        val newPassword = binding?.etNewPassword?.text.toString().trim()
        val confirmPassword = binding?.etConfirmPassword?.text.toString().trim()

        // Validasi input
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.length < 6) {
            Toast.makeText(requireContext(), "Password baru minimal 6 karakter", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(requireContext(), "Password baru dan konfirmasi tidak sama", Toast.LENGTH_SHORT).show()
            return
        }

        if (oldPassword == newPassword) {
            Toast.makeText(requireContext(), "Password baru harus berbeda dengan password lama", Toast.LENGTH_SHORT).show()
            return
        }

        // Cek password lama di Firestore
        db.collection("users")
            .document(userEmail)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val storedPassword = document.getString("password") ?: ""

                    if (storedPassword != oldPassword) {
                        Toast.makeText(requireContext(), "Password lama salah", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Update password di Firestore
                    updatePasswordInFirestore(newPassword)
                } else {
                    Toast.makeText(requireContext(), "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePasswordInFirestore(newPassword: String) {
        db.collection("users")
            .document(userEmail)
            .update("password", newPassword)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Password berhasil diubah", Toast.LENGTH_LONG).show()

                // Clear input fields
                binding?.etOldPassword?.setText("")
                binding?.etNewPassword?.setText("")
                binding?.etConfirmPassword?.setText("")

                // Navigate back
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal mengubah password: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}