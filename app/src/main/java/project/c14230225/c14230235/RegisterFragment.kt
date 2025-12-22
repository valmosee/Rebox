package project.c14230225.c14230235

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import project.c14230225.c14230235.databinding.FragmentLoginBinding
import project.c14230225.c14230235.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    var binding: FragmentRegisterBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = FirebaseFirestore.getInstance()

        binding!!.btnRegister.setOnClickListener {
            val _rName = binding!!.rgsName.text.toString().trim()
            val _rUName = binding!!.rgsUsername.text.toString().trim()
            val _rEmail = binding!!.rgsEmail.text.toString().trim()
            val _rPhone = binding!!.rgsPhone.text.toString().trim()
            val _rPassword = binding!!.rgsPassword.text.toString().trim()
            val _rConfPassword = binding!!.rgsConfPassword.text.toString().trim()

            if (_rUName.isEmpty() || _rPassword.isEmpty()) {
                Toast.makeText(context, "Username dan password wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (_rPassword != _rConfPassword) {
                Toast.makeText(context, "Password dan Confirm Password tidak sama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(
                _rEmail,
                _rUName,
                _rName,
                _rPassword,
                _rPhone,
                "",
                "",
            )

            db.collection("users")
                .document(_rEmail)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        Toast.makeText(context, "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
                    } else {
                        // Simpan dengan ID = email
                        db.collection("users")
                            .document(_rEmail)
                            .set(newUser)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Gagal menyimpan data", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }

        binding!!.btnGoLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}