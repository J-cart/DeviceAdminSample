package com.tutorials.deviceadminsample

import android.Manifest
import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tutorials.deviceadminsample.arch.LockViewModel
import com.tutorials.deviceadminsample.databinding.FragmentAllDevicesBinding
import com.tutorials.deviceadminsample.model.Resource
import com.tutorials.deviceadminsample.model.User
import com.tutorials.deviceadminsample.service.FirebaseMessagingReceiver
import com.tutorials.deviceadminsample.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@SuppressLint("MissingPermission")
class AllDevicesFragment : Fragment() {
    private var _binding: FragmentAllDevicesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()
    private val deviceAdapter by lazy { AllDevicesAdapter() }
    private val fUser = Firebase.auth.currentUser

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    private val locationRequest =
        LocationRequest.Builder(PRIORITY_BALANCED_POWER_ACCURACY, 120000L).apply {
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setMinUpdateIntervalMillis(10000L)

        }.build()

    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            val location = p0.lastLocation
            Log.d("LOCATING", "Location CallBack $location")
            location?.let {
                getCityName(it.longitude, it.latitude)
            }

        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAllDevicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        if (requireContext().checkPermissions()) {
            requestLocationUpdates()
        } else {
            makeAlert()
        }

        binding.profileImg.clipToOutline = true

        fUser?.email?.let {
            viewModel.getCurrentUserInfo(it)
            viewModel.getAllUserDevice(it)
            viewModel.addAllDevicesSnapshot(it)
            viewModel.addDeviceUserInfoSnapshot(it)
            viewModel.getCurrentUserDeviceInfo(it, Build.ID)

            observeAllDevices()
            binding.recyclerView.adapter = deviceAdapter
            observeDeviceCurrentUserInfo()

            deviceAdapter.adapterClick {
                val action = AllDevicesFragmentDirections.actionAllUsersToUserFragment(it.deviceId)
                findNavController().navigate(action)
            }
            binding.profileImg.setOnClickListener {
                val route = AllDevicesFragmentDirections.actionAllUsersToSettingsFragment()
                findNavController().navigate(route)
            }

        } ?: noUserAvailable()




    }


    private fun observeDeviceCurrentUserInfo() {
        lifecycleScope.launch {
            viewModel.currentUserInfo.collect { state ->
                when (state) {
                    is Resource.Successful -> {
                        state.data?.let {
                            if (it.userName.isNotEmpty()){
                                binding.helloText.text = "Hello ${it.userName} 👋"
                                binding.infoText.text = "Welcome back ${it.userName}"
                                if (it.imageUrl.isEmpty()) {
                                    binding.profileImg.setImageResource(R.drawable.account_avatar)
                                } else {
                                    binding.profileImg.load(it.imageUrl.toUri()) {
                                        crossfade(true)
                                        error(R.drawable.cloud_error_)
                                    }
                                }
                            }

                        } ?: requireContext().showToast("User Info Not Available")
                    }
                    is Resource.Failure -> {
                        state.msg?.let { requireContext().showToast(it) }
                    }
                    else -> Unit
                }

            }
        }

    }

    private fun observeAllDevices() {
        lifecycleScope.launch {
            viewModel.allDevices.collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        //show Loading
                        binding.apply {
                            errorText.isVisible = false
                            progressBar.isVisible = true
                            recyclerView.isVisible = false
                            deviceCountText.text = "Total Devices - 0"
                        }
                    }
                    is Resource.Successful -> {
                        //display result
                        resource.data?.let {
                            if (it.isNotEmpty()) {
                                binding.apply {
                                    errorText.isVisible = false
                                    progressBar.isVisible = false
                                    recyclerView.isVisible = true

                                    deviceAdapter.submitList(it)
                                    deviceCountText.text = "Total Devices - ${it.size}"
                                }
                            } else {
                                binding.apply {
                                    errorText.isVisible = true
                                    progressBar.isVisible = false
                                    recyclerView.isVisible = false

                                    deviceCountText.text = "Total Devices - 0"
                                    errorText.text = "No Devices Available..."
                                    deviceAdapter.submitList(emptyList())
                                }
                            }
                        } ?: emptyList<User>()

                    }
                    is Resource.Failure -> {
                        //show error
                        resource.msg?.let {
                            binding.apply {
                                errorText.isVisible = true
                                errorText.text = it
                                progressBar.isVisible = false
                                recyclerView.isVisible = false
                                deviceCountText.text = "Total Devices - 0"
                            }
                        }
                    }
                }

            }
        }
    }

    private fun requestLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallBack,
            Looper.getMainLooper()
        )
    }

    private fun getCityName(lon: Double, lat: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            val geoCoder = Geocoder(requireContext(), Locale.getDefault())

            try {
                val address = geoCoder.getFromLocation(lat, lon, 1)
                val sCityName = (address[0].subLocality
                    ?: address[0].subAdminArea) + ", " + address[0].locality + ", " + address[0].adminArea
                Log.d("City Name", "getCityName: $sCityName")
                fUser?.email?.let {
                    viewModel.updateUserLocation(it, sCityName)
                }

            } catch (e: Exception) {
                Log.d("City Name Exception", "getCityName: ${e.message}")
            }
        }

    }

    private val permissionsRequestLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            when {
                it.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                        it.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    requestLocationUpdates()
                }

                else -> {
                    makeAlert()
                }
            }
        }


    private fun makeAlert() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage("You need to allow permission for app to work properly")
            setTitle("ACCEPT PERMISSION REQUEST")
            setCancelable(false)
            setPositiveButton("OK") { dialogInterface, int ->
                requestPermissions(permissionsRequestLauncher)
                setCancelable(true)
                dialogInterface.dismiss()
            }
            create()
            show()
        }
    }


   private fun noUserAvailable(){
        Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
        binding.apply {
            errorText.isVisible = true
            errorText.text = "No user Logged In..."
            recyclerView.isVisible = false
            progressBar.isVisible = false

        }
    }
    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    }
}