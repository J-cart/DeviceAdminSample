package com.tutorials.deviceadminsample

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tutorials.deviceadminsample.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val fireStoreRef = Firebase.firestore.collection(USERS)
    private var deviceToken: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        SharedPreference.init(applicationContext)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.loginFragment,
                R.id.allUsers,
                R.id.adminLoginFragment,R.id.userFragment
            )
        )

        val fragHost = supportFragmentManager.findFragmentById(R.id.fragHost) as NavHostFragment
        navController = fragHost.findNavController()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                //TODO fix--me
                R.id.loginFragment -> {
                    binding.apply {
                        toggleModeBtn.isVisible= true
                        adminText.isVisible= true
                    }
                    supportActionBar?.hide()
                }
                R.id.adminLoginFragment -> {
                    supportActionBar?.hide()
                    binding.apply {
                        toggleModeBtn.isVisible= true
                        adminText.isVisible= true
                    }
                }
                else -> {
                    supportActionBar?.show()
                    binding.apply {
                        toggleModeBtn.isVisible= false
                        adminText.isVisible= false
                    }
                }
            }
        }
        val msg = "You need to allow permission for app to work properly"
        val title = "ACCEPT ADMIN REQUEST"

        val adminEnabled = SharedPreference.getBoolean(ADMIN_ACCESS, false)
        if (!adminEnabled) {
            showAlert(title, msg) {
                val admin = ComponentName(this, SampleAdminReceiver::class.java)
                val intent2: Intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin)
                startActivity(intent2)
            }
            return
        }

       // setupActionBarWithNavController(navController, appBarConfiguration)
        binding.toggleModeBtn.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                this,
                if (isChecked) "Geek Mode ON" else "Geek Mode OFF",
                Toast.LENGTH_SHORT
            ).show()

            if (isChecked) {
                navController.navigate(R.id.adminLoginFragment)
            } else {
                navController.navigate(R.id.loginFragment)
            }


        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}


