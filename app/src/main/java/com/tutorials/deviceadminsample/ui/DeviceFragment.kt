package com.tutorials.deviceadminsample.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tutorials.deviceadminsample.R
import com.tutorials.deviceadminsample.arch.LockViewModel
import com.tutorials.deviceadminsample.databinding.ActionConfirmationDialogBinding
import com.tutorials.deviceadminsample.databinding.AuthConfirmationDialogBinding
import com.tutorials.deviceadminsample.databinding.FragmentDeviceBinding
import com.tutorials.deviceadminsample.model.DeviceInfo
import com.tutorials.deviceadminsample.model.RemoteCommand
import com.tutorials.deviceadminsample.model.Resource
import com.tutorials.deviceadminsample.model.User
import com.tutorials.deviceadminsample.util.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DeviceFragment : Fragment() {
    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()
    private val fUser = Firebase.auth.currentUser
    private val args by navArgs<DeviceFragmentArgs>()
    private lateinit var user: User
    private lateinit var deviceInfo: DeviceInfo


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileImg.clipToOutline = true
        val deviceId = args.deviceId
        fUser?.email?.let {
            viewModel.addDeviceSnapshot(it, deviceId)
            viewModel.addDeviceUserInfoSnapshot(it)

            observeDeviceCurrentUserInfo()
            observeCurrentSelectedDevice()
            observeAllDevicesCount()
            binding.apply {
                alarmBtn.setOnClickListener {
                    //showConfirmationDialog()
                    tryDate()
                }
                lockText.setOnClickListener {
                    showActionDialog(LOCK)
                }
                profileImg.setOnClickListener {
                    val route = DeviceFragmentDirections.actionUserFragmentToSettingsFragment()
                    findNavController().navigate(route)
                }
            }
        } ?: Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()

    }

    private fun tryDate() {

        val myCalendar = Calendar.getInstance()

        val timePickerOnDataSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hour)
            myCalendar.set(Calendar.MINUTE, minute)

            Log.d("TIMER2", "${myCalendar.time}")
            Log.d("TIMER3", "${myCalendar.timeInMillis}")

            showActionDialog(ALARM, myCalendar)
        }
        TimePickerDialog(
            context,
            timePickerOnDataSetListener,
            myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE),
            false
        ).show()
    }


    private fun showActionDialog(text: String, calendar: Calendar? = null) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.action_confirmation_dialog, binding.root, false)
        val binding = ActionConfirmationDialogBinding.bind(dialogView)
        val newDialog = MaterialAlertDialogBuilder(requireContext()).create()
        if (dialogView.parent != null) {
            (dialogView.parent as ViewGroup).removeView(binding.root)
        }
        newDialog.setView(binding.root)


        newDialog.show()

        binding.apply {
            Glide.with(requireContext()).load(R.raw.action).into(binding.iconImg)
            if (text == ALARM) {
                infoText.text = "You are about to set an alarm on ${deviceInfo.deviceName} at ${
                    SimpleDateFormat(
                        TIME_FORMAT_ONE,
                        Locale.getDefault()
                    ).format(calendar?.time)
                }"
                confirmationText.text = "Yes, set alarm"
                confirmationText.setOnClickListener {
                    val remoteCommand =
                        RemoteCommand(type = ALARM, data = calendar?.timeInMillis.toString())
                    viewModel.sendPushNotifier(user, deviceInfo, remoteCommand)

                    newDialog.dismiss()
                }
            } else {
                infoText.text = "You are about to lock ${deviceInfo.deviceName} "
                confirmationText.text = "Yes, lock device"
                confirmationText.setOnClickListener {
                    val remoteCommand = RemoteCommand(type = LOCK)
                    viewModel.sendPushNotifier(user, deviceInfo, remoteCommand)

                    newDialog.dismiss()
                }
            }

            declineBtn.setOnClickListener {
                newDialog.dismiss()
            }

        }
    }

    private fun showConfirmationDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.auth_confirmation_dialog, binding.root, false)
        val binding = AuthConfirmationDialogBinding.bind(dialogView)
        val newDialog = MaterialAlertDialogBuilder(requireContext()).create()
        if (dialogView.parent != null) {
            (dialogView.parent as ViewGroup).removeView(binding.root)
        }
        newDialog.setView(binding.root)
        newDialog.show()

        binding.apply {
            Glide.with(requireContext()).load(R.raw.authgif).into(binding.iconImg)
            doneBtn.setOnClickListener {
                newDialog.dismiss()
            }
        }
    }

    private fun observeCurrentSelectedDevice() {
        lifecycleScope.launch {
            viewModel.currentSelectedDeviceInfo.collect { resource ->
                binding.progressBar.isVisible = resource is Resource.Loading
                when (resource) {
                    is Resource.Successful -> {
                        //display result

                        resource.data?.let {
                            deviceInfo = it
                            binding.apply {
                                nameText.text = it.deviceName
                                ringText.text = "Some time ago."
                                statusText.text =
                                    if (it.deviceToken.last() == "0") INACTIVE else ACTIVE
                                locationText.text = it.location
                            }

                        }

                    }
                    is Resource.Failure -> {
                        //show error
                        resource.msg?.let {
                            binding.apply {
                                deviceCountText.text = "Total Devices - ~~~"
                            }
                        }
                    }
                    else->Unit
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
                            user = it
                            if (it.userName.isNotEmpty()) {
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


    private fun observeAllDevicesCount() {
        lifecycleScope.launch {
            viewModel.allDevices.collect { resource ->
                if (resource is Resource.Successful) {
                    binding.deviceCountText.text = "Total Devices - ${resource.data?.size ?: "~~"}"
                }
            }


        }
    }
}