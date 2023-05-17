package com.tutorials.deviceadminsample

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
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
import com.tutorials.deviceadminsample.onboarding.BoardingFragmentDirections
import com.tutorials.deviceadminsample.service.FirebaseMessagingReceiver
import com.tutorials.deviceadminsample.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingPermission")
class AllDevicesFragment : Fragment() {
    private var _binding: FragmentAllDevicesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()
    private val adapter by lazy { AllUsersAdapter() }
    private val deviceAdapter by lazy { AllDevicesAdapter() }
    private lateinit var user: User
    private val fUser = Firebase.auth.currentUser
    private var cityName = "Not Innit."

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    private val locationRequest =
        LocationRequest.Builder(PRIORITY_BALANCED_POWER_ACCURACY, 1000L).apply {
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setMinUpdateIntervalMillis(120000L)//5mins-300000L

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
            viewModel.addAllDevicesSnapshot(it)
            viewModel.getCurrentUserInfo(it, Build.ID)
        } ?: Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
        /* lifecycleScope.launch { observeAllDevices() }
         newMenu()*/
        lifecycleScope.launch {
            adapter.submitList(emptyList())
            loadingState(true)
            delay(1000L)
            loadingState(false)
            adapter.submitList(getData())
        }
        binding.recyclerView.adapter = adapter
        lifecycleScope.launch {
            viewModel.currentUserDevice.collect { state ->
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
        adapter.adapterClick {
            val action = AllDevicesFragmentDirections.actionAllUsersToUserFragment()
            findNavController().navigate(action)
        }


    }

    private suspend fun observeAllDevices(email: String) {

        viewModel.getAllUserDevice(email)
        viewModel.allDevices.collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    //show Loading
                    errorState(false)
                    loadingState(true)
                }
                is Resource.Successful -> {
                    //display result
                    loadingState(false)
                    binding.recyclerView.adapter = adapter
                    resource.data?.let {
                        if (it.isNotEmpty()) {
                            deviceAdapter.submitList(it)
                            errorState(false)
                            binding.deviceCountText.text = "Total Devices - ${it.size}"
                        } else {
                            errorState(true)
                            binding.deviceCountText.text = "Total Devices - 0"
                            adapter.submitList(emptyList())
                        }
                    } ?: emptyList<User>()
//                    adapter.lockClick {
//                        val user = it.copy(commandType = LOCK)
//                        viewModel.sendPushNotifier(user)
//                    }
                    tryDate()
                }
                is Resource.Failure -> {
                    //show error
                    loadingState(false)
                    errorState(true)
                    resource.msg?.let {
                        binding.errorText.text = it
                        adapter.submitList(emptyList())
                        binding.deviceCountText.text = "Total Devices - 0"
                    }
                }
            }

        }
    }

    private fun errorState(state: Boolean) {
        binding.errorText.isVisible = state
    }

    private fun loadingState(state: Boolean) {
        binding.progressBar.isVisible = state
    }

    private fun tryDate() {

        val myCalendar = Calendar.getInstance()

        val timePickerOnDataSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hour)
            myCalendar.set(Calendar.MINUTE, minute)

            Log.d("TIMER2", "${myCalendar.time}")
            Log.d("TIMER3", "${myCalendar.timeInMillis}")
            //viewModel.sendPushNotifier(user.copy(commandType = ALARM, alarmTime = myCalendar.timeInMillis.toString()))
            binding.deviceCountText.text =
                SimpleDateFormat(TIME_FORMAT_ONE, Locale.getDefault()).format(myCalendar.time)
        }

        adapter.alarmClick {
            user = it
            TimePickerDialog(
                context,
                timePickerOnDataSetListener,
                myCalendar.get(Calendar.HOUR_OF_DAY),
                myCalendar.get(Calendar.MINUTE),
                false
            ).show()
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
        Log.d("City Name Exception", "getCityName: $cityName")
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