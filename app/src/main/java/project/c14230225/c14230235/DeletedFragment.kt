package project.c14230225.c14230235

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import project.c14230225.c14230235.databinding.FragmentDeletedBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DeletedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DeletedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentDeletedBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: deletedAdapter
    private lateinit var currentEmail: String
    private val args: DeletedFragmentArgs by navArgs() // Use Safe Args
    private var listSepatu: MutableList<Sepatu> ?= mutableListOf()

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
        // Inflate the layout for this fragment
        binding = FragmentDeletedBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack() // Close StoreActivity and go back
        }

        adapter = deletedAdapter(listSepatu ?: mutableListOf())
        binding.rvDeleted.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDeleted.adapter = adapter

        currentEmail = args.currentUserEmail
        loadProducts()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadProducts() {
        db.collection("deleted")
            .whereEqualTo("sellerEmail", currentEmail)
            .get()
            .addOnSuccessListener { result ->
                listSepatu?.clear()

                println("🔥 Fetched ${result.size()} products from Firestore deleted")
                for (doc in result) {
                    val product = doc.toObject(Sepatu::class.java)
                    product.id = doc.id // ✅ ADD THIS LINE - Store Firestore document ID
                    println("📦 Product: ${product.nama}, ${product.harga}, ID: ${product.id}") // Updated log
                    listSepatu?.add(product)
                }

                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                println("❌ Error fetching products: $e")
            }
    }
}