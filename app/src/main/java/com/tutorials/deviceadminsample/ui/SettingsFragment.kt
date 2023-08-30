package com.tutorials.deviceadminsample.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tutorials.deviceadminsample.R
import com.tutorials.deviceadminsample.ui.arch.LockViewModel
import com.tutorials.deviceadminsample.databinding.FragmentSettingsBinding
import com.tutorials.deviceadminsample.model.RequestState
import com.tutorials.deviceadminsample.model.Resource
import com.tutorials.deviceadminsample.model.User
import com.tutorials.deviceadminsample.service.FirebaseMessagingReceiver
import com.tutorials.deviceadminsample.util.NetworkStatus
import com.tutorials.deviceadminsample.util.showAlert
import com.tutorials.deviceadminsample.util.showToast
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()
    private val fUser = Firebase.auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileImg.clipToOutline = true
        binding.undoBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        lifecycleScope.launch {
            viewModel.connectionState.collect{
                binding.updateBtn.isClickable = it == NetworkStatus.CONNECTED
                binding.logOutText.isClickable = it == NetworkStatus.CONNECTED
                binding.updatePasswordText.isClickable = it == NetworkStatus.CONNECTED
                binding.editImg.isClickable = it == NetworkStatus.CONNECTED
                if (it == NetworkStatus.DISCONNECTED || it == NetworkStatus.IDLE ){
                    binding.constraintLayout.setOnClickListener {
                        requireContext().showToast("No connection detected, make sure to be connected to the internet")
                    }
                    requireContext().showToast("No connection detected, make sure to be connected to the internet")
                }else{
                    performAllOperation()

                }
            }

        }

        observeDeviceCurrentUserInfo()
        observeDeviceCurrentUserDeviceInfo()
        observeUpdateUserInfo()
        observeResetPasswordState()

    }
    private fun performAllOperation(){
        fUser?.email?.let {
            viewModel.addDeviceUserInfoSnapshot(it)

            binding.editImg.setOnClickListener {
                selectImage()
            }
            lifecycleScope.launch {
                viewModel.updateProfImgStatusFlow.collect { state ->
                    binding.imgProgressBar.isVisible = state is RequestState.Loading
                    if (state is RequestState.Failure) {
                        requireContext().showToast(state.msg)
                    }
                }
            }

        } ?: requireContext().showToast("No user logged in")
    }

    private fun observeDeviceCurrentUserInfo() {
        lifecycleScope.launch {
            viewModel.currentUserInfo.collect { state ->
                when (state) {
                    is Resource.Successful -> {
                        state.data?.let { user ->
                            binding.apply {
                                fullNameEdt.setText(user.fullName)
                                userNameEdt.setText(user.userName)
                                emailEdt.setText(user.email)
                                if (user.imageUrl.isEmpty()) {
                                    profileImg.setImageResource(R.drawable.account_avatar)
                                } else {
                                    profileImg.load(user.imageUrl.toUri()) {
                                        crossfade(true)
                                        error(R.drawable.cloud_error_)
                                    }
                                }
                                updatePasswordText.setOnClickListener {
                                    requireContext().showAlert(
                                        "Update password",
                                        "You are about to update your password, are you sure about this action?"
                                    ){
                                        verifyAndResetPassword(user)
                                    }

                                }
                            }
                            updateUserInfo(user)


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

    private fun observeDeviceCurrentUserDeviceInfo() {
        lifecycleScope.launch {
            viewModel.currentUserDeviceInfo.collect { state ->
                when (state) {
                    is Resource.Successful -> {
                        binding.logOutText.setOnClickListener {
                            state.data?.let {
                                FirebaseMessagingReceiver.updateDeviceToken(
                                    requireContext(),
                                    "0",
                                    it
                                )
                                Firebase.auth.signOut().also {
                                    val navigate =
                                        SettingsFragmentDirections.actionSettingsFragmentToLoginFragment()
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

    private fun observeResetPasswordState() {
        lifecycleScope.launch {
            viewModel.resetPasswordStatusFlow.collect { state ->
                binding.progressBar.isVisible = state is RequestState.Loading
                when (state) {
                    is RequestState.Successful -> {
                        requireContext().showToast("Password updated successfully")
                    }
                    is RequestState.Failure -> {
                        requireContext().showToast(state.msg)
                    }
                    else -> Unit
                }

            }
        }

    }


    private fun observeUpdateUserInfo() {
        lifecycleScope.launch {
            viewModel.updateDetailsStatusFlow.collect { state ->
                binding.progressBar.isVisible = state is RequestState.Loading
                when (state) {
                    is RequestState.Successful -> {
                        binding.newPassEdt.text?.clear()
                        binding.confirmPassEdt.text?.clear()
                        requireContext().showToast("Profile update successful")
                    }
                    is RequestState.Failure -> {
                        binding.newPassEdt.text?.clear()
                        binding.confirmPassEdt.text?.clear()
                        requireContext().showToast(state.msg)
                    }
                    else -> Unit
                }

            }
        }

    }


    private fun updateUserInfo(user: User) {

        binding.apply {
            updateBtn.setOnClickListener {
                val fullName = fullNameEdt.text.toString().trim()
                val userName = userNameEdt.text.toString().trim()
                if (fullName != user.fullName || userName != user.userName) {
                    val newUser = user.copy(fullName = fullName, userName = userName)
                    viewModel.updateUserDetails(newUser)
                }
            }
        }

    }

    private val requestAccountImgPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data = it.data?.data
                data?.let { uri ->
                    viewModel.updateProfileImage(fUser?.email!!, uri)
                }
                return@registerForActivityResult
            }
            requireContext().showToast("Unable to update profile image")
            Log.d("checker-dere", "error ${it.resultCode}")

        }

    private fun selectImage() {
        val intent = Intent(
            Intent.ACTION_GET_CONTENT,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        ).apply {
            type = "image/*"
        }
        requestAccountImgPicker.launch(intent)

    }

    private fun verifyAndResetPassword(user: User) {
        val newPassword = binding.newPassEdt.text.toString().trim()
        val confirmPassword = binding.confirmPassEdt.text.toString().trim()

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            requireContext().showToast("Fields cannot be empty")
            return
        }
        if (newPassword != confirmPassword) {
            requireContext().showToast("Passwords do not match")
            return
        }

        viewModel.resetPassword(user = user, newPassword = newPassword)


    }


}