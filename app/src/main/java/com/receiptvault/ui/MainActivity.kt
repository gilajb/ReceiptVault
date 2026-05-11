package com.receiptvault.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.receiptvault.R
import com.receiptvault.databinding.ActivityMainBinding
import com.receiptvault.utils.SyncWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Setup Toolbar
        setSupportActionBar(binding.topAppBar)

        // 2. Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfig = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.historyFragment, R.id.analyticsFragment)
        )

        setupActionBarWithNavController(navController, appBarConfig)
        binding.bottomNavView.setupWithNavController(navController)

        // 3. Bottom Nav Visibility Listener
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.signupFragment,
                R.id.scannerFragment, R.id.confirmationFragment -> {
                    binding.bottomNavView.visibility = View.GONE
                }
                else -> binding.bottomNavView.visibility = View.VISIBLE
            }
        }

        // 4. Initial Auth Check (Only on fresh launch)
        if (savedInstanceState == null) {
            if (auth.currentUser == null) {
                navController.navigate(R.id.loginFragment)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_account -> {
                navController.navigate(R.id.accountFragment)
                true
            }
            R.id.action_sign_out -> {
                auth.signOut()
                navController.navigate(R.id.loginFragment)
                true
            }
            R.id.action_sync -> {
                SyncWorker.schedule(this)
                android.widget.Toast.makeText(this, "Sync scheduled", android.widget.Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
} // This brace must be the very last thing in the file