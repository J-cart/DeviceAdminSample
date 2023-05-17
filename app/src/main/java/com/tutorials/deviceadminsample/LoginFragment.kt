package com.tutorials.deviceadminsample

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tutorials.deviceadminsample.arch.LockViewModel
import com.tutorials.deviceadminsample.databinding.FragmentLoginBinding
import com.tutorials.deviceadminsample.model.RequestState
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()
    private val fUser = Firebase.auth.currentUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (fUser != null) {
            val navigate = LoginFragmentDirections.actionLoginFragmentToAllUsers()
            findNavController().navigate(navigate)
            return
        }
        validateUser()

    }

    private fun validateUser() {
        val navigate = LoginFragmentDirections.actionLoginFragmentToAllUsers()
        val deviceName = Build.BRAND + " " + Build.MODEL
        binding.loginLayout.apply {
            createAccText.setOnClickListener {
                emailEdt.setText("")
                passEdt.setText("")
                this.root.isVisible = false
                binding.signUpLayout.root.isVisible = true
            }

            loginBtn.setOnClickListener {

                lifecycleScope.launch {
                    if (emailEdt.text.isNullOrEmpty() || passEdt.text.isNullOrEmpty()) {
                        emailBox.error = "Required,Empty Field*"
                        return@launch
                    }

                    viewModel.loginUser(
                        context = requireContext(),
                        email = emailEdt.text.toString(),
                        password = passEdt.text.toString(),
                        deviceId = Build.ID,
                        deviceName = deviceName,
                        location = ""
                    )
                    viewModel.loginState.collect {
                        when (it) {
                            is RequestState.Loading -> {
                                progressBar.isVisible = true
                            }
                            is RequestState.Successful -> {
                                progressBar.isVisible = false
                                Toast.makeText(
                                    requireContext(),
                                    "Login Successful--> ${it.data}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                if (findNavController().currentDestination?.id == R.id.loginFragment) {
                                    findNavController().navigate(navigate)
                                }
                            }
                            is RequestState.Failure -> {
                                progressBar.isVisible = false
                                Toast.makeText(
                                    requireContext(),
                                    "Error--> ${it.msg}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is RequestState.NonExistent -> {
                                progressBar.isVisible = false
                            }
                        }
                    }

                }


            }

        }

        ////////////////////////SIGN UP////////////////////////////////


        binding.signUpLayout.apply {
            signInText.setOnClickListener {
                emailEdt.setText("")
                passEdt.setText("")
                this.root.isVisible = false
                binding.loginLayout.root.isVisible = true
            }

            createAccBtn.setOnClickListener {
                lifecycleScope.launch {
                    if (emailEdt.text.isNullOrEmpty()) {
                        emailBox.error = "Required,Empty Field*"
                        return@launch
                    }

                    if (passEdt.text.isNullOrEmpty()) {
                        passBox.error = "Required,Empty Field*"
                        return@launch
                    }

                    if (fullNameEdt.text.isNullOrEmpty()) {
                        fullNameBox.error = "Required,Empty Field*"
                        return@launch
                    }


                    viewModel.signUpNew(
                        email = emailEdt.text?.trim().toString(),
                        password = passEdt.text?.trim().toString(),
                        deviceId = Build.ID,
                        deviceName = deviceName,
                        location = "",
                        userName = fullNameEdt.text?.trim().toString()
                    )
                    viewModel.signUpState.collect {
                        when (it) {
                            is RequestState.NonExistent -> {
                                progressBar.isVisible = false
                            }
                            is RequestState.Loading -> {
                                progressBar.isVisible = true
                            }
                            is RequestState.Successful -> {
                                progressBar.isVisible = false
                                Toast.makeText(
                                    requireContext(),
                                    "SignUp Successful--> ${it.data}",
                                    Toast.LENGTH_SHORT
                                ).show()


                                if (findNavController().currentDestination?.id == R.id.loginFragment) {
                                    findNavController().navigate(navigate)
                                }
                                //findNavController().navigate(R.id.userFragment)
                            }
                            is RequestState.Failure -> {
                                progressBar.isVisible = false
                                Toast.makeText(
                                    requireContext(),
                                    "Error--> ${it.msg}",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }


}