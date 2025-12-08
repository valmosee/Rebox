package project.c14230225.c14230235

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import project.c14230225.c14230235.databinding.ActivityUserBinding

class UserActivity : AppCompatActivity() {

    lateinit var binding: ActivityUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val root = findViewById<View>(R.id.framecontainer)

        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                top = systemBars.top
            )
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.framecontainer) as NavHostFragment

        val navController = navHostFragment.navController
        binding.bottomNavView.setupWithNavController(navController)

    }
}