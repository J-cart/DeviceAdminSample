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
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
    private var cityName = "Not Innit."

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    private val locationRequest =
        LocationRequest.Builder(PRIORITY_BALANCED_POWER_ACCURACY, 120000L).apply {
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setMinUpdateIntervalMillis(10000L)//5mins-300000L

        }.build()

    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            val location = p0.lastLocation
            Log.d("LOCATING", "Location CallBack $location")
            location?.let {
                // assign values
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
        } ?: Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()

        observeAllDevices()
        binding.recyclerView.adapter = deviceAdapter
        observeDeviceCurrentUserInfo()
        observeDeviceCurrentUserDeviceInfo()

        deviceAdapter.adapterClick {
            val action = AllDevicesFragmentDirections.actionAllUsersToUserFragment(it.deviceId)
            findNavController().navigate(action)
        }


    }

    private fun observeDeviceCurrentUserDeviceInfo() {
        lifecycleScope.launch {
            viewModel.currentUserDeviceInfo.collect { state ->
                when (state) {
                    is Resource.Successful -> {
                        binding.helloText.setOnClickListener {
                            state.data?.let {
                                FirebaseMessagingReceiver.updateDeviceToken(
                                    requireContext(),
                                    "0",
                                    it
                                )
                                Firebase.auth.signOut().also {
                                    val navigate =
                                        AllDevicesFragmentDirections.actionAllUsersToLoginFragment()
                                    findNavController().navigate(navigate)
                                    viewModel.resetOnSignOut()
                                }
                            } ?: requireContext().showToast("User Device Info Not Available")

                        }

                    }
                    is Resource.Failure -> {
                        state.msg?.let { requireContext().showToast(it) }
                    }
                    else -> Unit
                }

            }
        }

    }

    private fun observeDeviceCurrentUserInfo() {
        lifecycleScope.launch {
            viewModel.currentUserInfo.collect { state ->
                when (state) {
                    is Resource.Successful -> {
                        state.data?.let {
                            if (it.userName.isNotEmpty()){
                                binding.helloText.text = "Hello ${it.userName}"
                                binding.infoText.text = "Welcome back ${it.userName}"
                            }

                        } ?: requireContext().showToast("User Device Info Not Available")
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

    private fun newMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.user_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.logoutMenu -> {
                        FirebaseMessagingReceiver.updateDeviceToken(requireContext(), "0")
                        Firebase.auth.signOut().also {
                            findNavController().navigate(R.id.loginFragment)
                            viewModel.resetOnSignOut()
                        }
                        true
                    }
                    else -> false
                }

            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
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
            val address = geoCoder.getFromLocation(lat, lon, 1)

            try {
                cityName = (address[0].subLocality
                    ?: address[0].subAdminArea) + ", " + address[0].locality + ", " + address[0].adminArea
                val sCityName = (address[0].subLocality
                    ?: address[0].subAdminArea) + ", " + address[0].locality + ", " + address[0].adminArea
                fUser?.email?.let {
                    viewModel.updateUserLocation(it, sCityName)
                }

            } catch (e: Exception) {
                cityName = ""
                Log.d("City Name Exception", "getCityName: ${e.message}")
            }
        }
        Log.d("City Name", "getCityName: $cityName")
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


    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
    }
}