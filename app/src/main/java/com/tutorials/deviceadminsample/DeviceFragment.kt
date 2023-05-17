package com.tutorials.deviceadminsample

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tutorials.deviceadminsample.arch.LockViewModel
import com.tutorials.deviceadminsample.databinding.ActionConfirmationDialogBinding
import com.tutorials.deviceadminsample.databinding.AuthConfirmationDialogBinding
import com.tutorials.deviceadminsample.databinding.FragmentDeviceBinding
import com.tutorials.deviceadminsample.model.Resource
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
            // TODO: get this to fetch the selected user data
            viewModel.addDeviceSnapshot(it, deviceId)
            viewModel.addDeviceUserInfoSnapshot(it)
        } ?: Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()

        observeDeviceCurrentUserInfo()
        observeCurrentSelectedDevice()
        binding.apply {
            alarmBtn.setOnClickListener {
                //showConfirmationDialog()
                tryDate()
            }
            lockText.setOnClickListener {
                showActionDialog(LOCK)
            }
        }
    }

    private fun tryDate() {

        val myCalendar = Calendar.getInstance()

        val timePickerOnDataSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hour)
            myCalendar.set(Calendar.MINUTE, minute)

            Log.d("TIMER2", "${myCalendar.time}")
            Log.d("TIMER3", "${myCalendar.timeInMillis}")
            //viewModel.sendPushNotifier(user.copy(commandType = ALARM, alarmTime = myCalendar.timeInMillis.toString()))

            showActionDialog(ALARM, myCalendar)
            binding.deviceCountText.text =
                SimpleDateFormat(TIME_FORMAT_ONE, Locale.getDefault()).format(myCalendar.time)
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
                infoText.text = "You are about to set an alarm on DEVICE at ${SimpleDateFormat(
                TIME_FORMAT_ONE,
                Locale.getDefault()
                ).format(calendar?.time)
            }"
                confirmationText.text = "Yes,set alarm"
            } else {
                infoText.text = "You are about to lock DEVICE "
                confirmationText.text = "Yes,lock device"
            }

            declineBtn.setOnClickListener {
                newDialog.dismiss()
            }
            confirmationText.setOnClickListener {
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
                when (resource) {
                    is Resource.Loading -> {
                        //show Loading
                        binding.apply {

                            deviceCountText.text = "Total Devices - Loading.."
                        }
                    }
                    is Resource.Successful -> {
                        //display result

                        resource.data?.let {
                            binding.apply {
                                deviceCountText.text = "Total Devices - OK.."
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
                                deviceCountText.text = "Total Devices - 0"
                            }
                        }
                    }
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


}