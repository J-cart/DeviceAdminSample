package com.tutorials.deviceadminsample.ui

import android.animation.ObjectAnimator
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.tutorials.deviceadminsample.R
import com.tutorials.deviceadminsample.ui.arch.LockViewModel
import com.tutorials.deviceadminsample.databinding.ActivityMainBinding
import com.tutorials.deviceadminsample.receiver.SampleAdminReceiver
import com.tutorials.deviceadminsample.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val viewModel: LockViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel.checkNetworkState(this)
        SharedPreference.init(applicationContext)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.boardingFragment,
                R.id.loginFragment,
                R.id.allUsers,
                R.id.userFragment
            )
        )

        val fragHost = supportFragmentManager.findFragmentById(R.id.fragHost) as NavHostFragment
        navController = fragHost.findNavController()

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
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}


