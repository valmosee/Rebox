package project.c14230225.c14230235

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import project.c14230225.c14230235.databinding.FragmentLoginBinding
import project.c14230225.c14230235.User

class LoginFragment : Fragment() {

    var binding: FragmentLoginBinding? = null
    lateinit var db: FirebaseFirestore

    // Hardcoded credentials untuk testing
    private val HARDCODED_EMAIL = "cloudia@gmail.com"
    private val HARDCODED_PASSWORD = "123456789"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        binding!!.btnLogin.setOnClickListener {
            performLogin()
        }

        binding!!.btnGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun performLogin() {
        try {
            // Ambil input dari EditText
            val emailInput = binding!!.loginEmail.text.toString().trim()
            val passwordInput = binding!!.loginPassword.text.toString().trim()

            // Tentukan email dan password yang akan digunakan
            val email: String
            val password: String

            if (emailInput.isEmpty() && passwordInput.isEmpty()) {
                // Jika kedua field kosong, gunakan hardcoded credentials
                email = HARDCODED_EMAIL
                password = HARDCODED_PASSWORD
                Toast.makeText(context, "Menggunakan akun default untuk testing", Toast.LENGTH_SHORT).show()
            } else if (emailInput.isEmpty() || passwordInput.isEmpty()) {
                // Jika salah satu field kosong, tampilkan error
                Toast.makeText(context, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return
            } else {
                // Jika kedua field terisi, gunakan input user
                email = emailInput
                password = passwordInput
            }

            // Proses login ke Firestore
            loginToFirestore(email, password)

        } catch (e: Exception) {
            Toast.makeText(context, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginToFirestore(email: String, password: String) {
        try {
            db.collection("users").document(email)
                .get()
                .addOnSuccessListener { document ->
                    try {
                        if (!document.exists()) {
                            Toast.makeText(context, "Email belum terdaftar", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        val storedPassword = document.getString("password")

                        if (storedPassword.isNullOrEmpty()) {
                            Toast.makeText(context, "Data password tidak valid", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        if (storedPassword == password) {
                            Toast.makeText(context, "Login berhasil", Toast.LENGTH_LONG).show()

                            // Simpan data user ke session
                            MainActivity._UserSession = project.c14230225.c14230235.User(
                                document.getString("email") ?: "",
                                document.getString("username") ?: "",
                                document.getString("namalengkap") ?: "",
                                document.getString("password") ?: "",
                                document.getString("phonenumber") ?: "",
                                document.getString("alamat") ?: "",
                                document.getString("foto") ?: ""
                            )

                            // Navigate ke UserActivity
                            val intent = Intent(requireContext(), UserActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                            requireActivity().finish()

                            val welcomeMessage = "Welcome, ${MainActivity._UserSession.username}!"
                            Toast.makeText(context, welcomeMessage, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Password salah", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error memproses data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Terjadi kesalahan saat login: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(context, "Error koneksi database: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}