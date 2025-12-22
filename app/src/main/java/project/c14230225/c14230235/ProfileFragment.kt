package project.c14230225.c14230235

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var db: FirebaseFirestore

    // Views
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvUsername: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvAddress: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()

        // Initialize views
        drawerLayout = view.findViewById(R.id.drawerLayout)
        navigationView = view.findViewById(R.id.navigationView)
        toolbar = view.findViewById(R.id.toolbar)

        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)
        tvUsername = view.findViewById(R.id.tvUsername)
        tvFullName = view.findViewById(R.id.tvFullName)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber)
        tvAddress = view.findViewById(R.id.tvAddress)

        // Setup toolbar to open drawer
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_edit_profile -> {
                    Toast.makeText(requireContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show()
                    val action = ProfileFragmentDirections
                        .actionMenuprofileToEditProfileFragment(requireActivity().intent.getStringExtra("email") ?: "")
                    findNavController().navigate(action)
                }
                R.id.nav_my_store -> {
                    Toast.makeText(requireContext(), "My Store clicked", Toast.LENGTH_SHORT).show()
                    // Navigate to my store screen
                     var intent = Intent(requireContext(), StoreActivity::class.java)
                    intent.putExtra("email", requireActivity().intent.getStringExtra("email"))
                    startActivity(intent)
                }
                R.id.nav_change_password -> {
                    Toast.makeText(requireContext(), "Change Password clicked", Toast.LENGTH_SHORT).show()
                    // Navigate to change password screen
                    // findNavController().navigate(R.id.action_profile_to_changePassword)
                }
                R.id.nav_logout -> {
                    Toast.makeText(requireContext(), "Logout clicked", Toast.LENGTH_SHORT).show()
                    // Navigate to login screen
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Load user profile data
        loadUserProfile(requireActivity().intent.getStringExtra("email") ?: "")

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.closeDrawer(GravityCompat.START)
                    } else {
                        // Let NavController handle back navigation
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }
        )
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
                    tvUsername.text = username
                    tvFullName.text = namalengkap
                    tvEmail.text = email
                    tvPhoneNumber.text = phonenumber
                    // Show address or placeholder
                    if (alamat.isNotEmpty()) {
                        tvAddress.text = alamat
                        tvAddress.setTextColor(resources.getColor(android.R.color.black, null))
                    } else {
                        tvAddress.text = "No address added yet"
                        tvAddress.setTextColor(resources.getColor(android.R.color.darker_gray, null))
                    }
                    // Load profile picture
                    if (foto.isNotEmpty()) {
                        Glide.with(requireContext())
                            .load(foto)
                            .circleCrop()
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(ivProfilePicture)
                    } else {
                        // Set default profile picture
                        ivProfilePicture.setImageResource(R.drawable.ic_launcher_foreground)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}