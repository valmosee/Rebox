package project.c14230225.c14230235

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import project.c14230225.c14230235.databinding.ActivityMainBinding
import project.c14230225.c14230235.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    var binding: FragmentLoginBinding? = null
    lateinit var db: FirebaseFirestore

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
            binding!!.loginEmail.setText("cloudia@gmail.com")
            binding!!.loginPassword.setText("123456")

            val email = binding!!.loginEmail.text.toString().trim()
            val password = binding!!.loginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Email dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users").document(email)
                .get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        Toast.makeText(context, "Email belum terdaftar", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val storedPassword = document.getString("password")

                    if (storedPassword == password) {
                        Toast.makeText(context, "Login berhasil", Toast.LENGTH_SHORT).show()

                        val intent = Intent(requireContext(), UserActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    } else {
                        Toast.makeText(context, "Password salah", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Terjadi kesalahan saat login", Toast.LENGTH_SHORT).show()
                }
        }

        binding!!.btnGoRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}