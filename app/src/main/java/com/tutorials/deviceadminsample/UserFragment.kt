package com.tutorials.deviceadminsample

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tutorials.deviceadminsample.service.FirebaseMessagingReceiver.Companion.upcomingAlarmTime
import com.tutorials.deviceadminsample.service.FirebaseMessagingReceiver.Companion.updateToken
import com.tutorials.deviceadminsample.arch.LockViewModel
import com.tutorials.deviceadminsample.databinding.ActionConfirmationDialogBinding
import com.tutorials.deviceadminsample.databinding.AuthConfirmationDialogBinding
import com.tutorials.deviceadminsample.databinding.FragmentUserBinding
import com.tutorials.deviceadminsample.util.ACTION_GIF_URL
import com.tutorials.deviceadminsample.util.AUTH_GIF_URL
import com.tutorials.deviceadminsample.util.TIME_FORMAT_ONE
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()
    private val fUser = Firebase.auth.currentUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.profileImg.clipToOutline = true
       fUser?.email?.let {
           viewModel.addUserSnapshot(it)
        }?: "User Account"
        //userStatus()
        //newMenu()
        binding.apply {
            alarmBtn.setOnClickListener {
                showConfirmationDialog()
            }
            lockText.setOnClickListener {
                showActionDialog()
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
                        updateToken(requireContext(),"0")
                        Firebase.auth.signOut().also {
                            findNavController().navigate(R.id.loginFragment)
                            viewModel.updateLogin()
                        }
                        true
                    }
                    else -> false
                }

            }
        }, viewLifecycleOwner, Lifecycle.State.STARTED)
    }

    private fun userStatus(){
        lifecycleScope.launch {
            viewModel.userStatusEvent.collect { linkResponse ->
                when (linkResponse) {
                    is LockViewModel.UserEvents.Successful -> Unit
                    is LockViewModel.UserEvents.Failure -> {
                        Firebase.auth.signOut().also {
                            findNavController().navigate(R.id.loginFragment)
                            viewModel.updateLogin()
                            Log.d("me_logout", "user logged out remotely")
                        }
                    }
                    is LockViewModel.UserEvents.Error -> Unit
                }
            }
        }
    }

    private fun showActionDialog(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.action_confirmation_dialog,binding.root,false)
        val binding = ActionConfirmationDialogBinding.bind(dialogView)
        val newDialog = MaterialAlertDialogBuilder(requireContext()).create()
        if (dialogView.parent != null){
            (dialogView.parent as ViewGroup).removeView(binding.root)
        }
        newDialog.setView(binding.root)
        newDialog.show()

        binding.apply {
            Glide.with(requireContext()).load(R.raw.action).into(binding.iconImg)
//            Glide.with(requireContext()).load(ACTION_GIF_URL).into(binding.iconImg)
            declineBtn.setOnClickListener {
                newDialog.dismiss()
            }
        }
    }
 private fun showConfirmationDialog(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.auth_confirmation_dialog,binding.root,false)
        val binding = AuthConfirmationDialogBinding.bind(dialogView)
        val newDialog = MaterialAlertDialogBuilder(requireContext()).create()
        if (dialogView.parent != null){
            (dialogView.parent as ViewGroup).removeView(binding.root)
        }
        newDialog.setView(binding.root)
        newDialog.show()

        binding.apply {
            Glide.with(requireContext()).load(R.raw.authgif).into(binding.iconImg)
//            Glide.with(requireContext()).load(AUTH_GIF_URL).into(binding.iconImg)
            doneBtn.setOnClickListener {
                newDialog.dismiss()
            }
        }
    }

}