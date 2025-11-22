package project.c14230225.c14230235

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        db = Firebase.firestore

        var _btnAdd = findViewById<Button>(R.id.btnAdd)

        _btnAdd.setOnClickListener {
            var baru = ClsUser(
                "0",
                "Claudia",
                "234",
                "Claudia Harahap"
            )
            // Add a new document with a generated ID
            db.collection("users")
                .add(baru)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(
                        this, "Sukses : DocumentSnapshot added with ID: ${documentReference.id}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this, "Error Adding Document",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

}