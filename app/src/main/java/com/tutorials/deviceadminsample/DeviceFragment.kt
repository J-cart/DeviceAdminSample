package com.tutorials.deviceadminsample

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tutorials.deviceadminsample.arch.LockViewModel
import com.tutorials.deviceadminsample.databinding.ActionConfirmationDialogBinding
import com.tutorials.deviceadminsample.databinding.AuthConfirmationDialogBinding
import com.tutorials.deviceadminsample.databinding.FragmentDeviceBinding
import com.tutorials.deviceadminsample.service.FirebaseMessagingReceiver.Companion.updateDeviceToken

class DeviceFragment : Fragment() {
    private var _binding: FragmentDeviceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()
    private val fUser = Firebase.auth.currentUser


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
       fUser?.email?.let {
           // TODO: get this to fetch the selected user data
           viewModel.addDeviceSnapshot(it,Build.ID)
        }?: Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show()
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
                        updateDeviceToken(requireContext(),"0")
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
            doneBtn.setOnClickListener {
                newDialog.dismiss()
            }
        }
    }

}