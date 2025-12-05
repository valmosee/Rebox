package project.c14230225.c14230235

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import project.c14230225.c14230235.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        var _UserSession : String = ""
        var JenisSepatu = mutableListOf<String>(
            "Flats Shoes",
            "Sneakers",
            "Heels",
            "Slippers",
            "Loafers",
            "Boots",
            "Sports Shoes"
        )
    }
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        var _btnAdd = findViewById<Button>(R.id.btnAdd)
//        _btnAdd.setOnClickListener {
//            var baru = User(
//                "claudia@gmail.com",
//                "Cloud",
//                "Claudia Harahap",
//                "123456",
//                "123456",
//                "Jl Melati No.25",
//                ""
//            )
//            // Add a new document with a generated ID
//            db.collection("users")
//                .add(baru)
//                .addOnSuccessListener { documentReference ->
//                    Toast.makeText(
//                        this, "Sukses : DocumentSnapshot added with ID: ${documentReference.id}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(
//                        this, "Error Adding Document",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//        }
    }
}