package project.c14230225.c14230235

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoryTransaksiFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvHistory: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var adapter: TransactionAdapter
    private val transactionList = mutableListOf<Transaction>()
    private var currentUserEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_transaksi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        currentUserEmail = requireActivity().intent.getStringExtra("email") ?: ""

        // Initialize views
        rvHistory = view.findViewById(R.id.rvHistory)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        // Setup RecyclerView
        adapter = TransactionAdapter(transactionList)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

        // Load transactions
        loadTransactionHistory()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadTransactionHistory() {
        db.collection("transactions")
            .whereEqualTo("buyerEmail", currentUserEmail)
            .orderBy("purchaseDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                transactionList.clear()

                if (result.isEmpty) {
                    showEmptyState(true)
                } else {
                    showEmptyState(false)

                    for (doc in result) {
                        val transaction = doc.toObject(Transaction::class.java)
                        transaction.id = doc.id
                        transactionList.add(transaction)
                    }

                    adapter.notifyDataSetChanged()
                    println("✅ Loaded ${transactionList.size} transactions")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading history: ${e.message}", Toast.LENGTH_SHORT).show()
                showEmptyState(true)
            }
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            rvHistory.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
        } else {
            rvHistory.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
        }
    }
}