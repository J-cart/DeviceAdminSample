package com.tutorials.deviceadminsample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tutorials.deviceadminsample.databinding.FragmentAdminLoginBinding
import kotlinx.coroutines.launch

class AdminLoginFragment : Fragment() {
    private var _binding: FragmentAdminLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()
    private val fUser = Firebase.auth.currentUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAdminLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (fUser != null) {
            if (fUser.email!!.contains("admin",true)){
                val navigate = AdminLoginFragmentDirections.actionAdminLoginFragmentToAllUsers2()
                findNavController().navigate(navigate)
                return
            }

        }
        validateAdmin()

    }

    private fun validateAdmin() {

        binding.loginLayout.apply {
            loginText.text = "ADMIN LOGIN"
            signUpTxt.setOnClickListener {
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


                    if (!emailEdt.text!!.contains("admin",true)) {
                        emailBox.error = "Required, this is not an ADMIN login*"
                        return@launch
                    }

                    viewModel.loginAdmin(
                        requireContext(),
                        emailEdt.text.toString(),
                        passEdt.text.toString()
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
                                findNavController().navigate(R.id.allUsers)
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


        ////SIGN-UP-ADMIN/////////////////////////////////////
        binding.signUpLayout.apply {
            signUpText.text = "ADMIN SIGN UP"
            loginTxt.setOnClickListener {
                emailEdt.setText("")
                passEdt.setText("")
                this.root.isVisible = false
                binding.loginLayout.root.isVisible = true
            }

            signUpBtn.setOnClickListener {
                lifecycleScope.launch {
                    if (emailEdt.text.isNullOrEmpty() || passEdt.text.isNullOrEmpty()) {
                        emailBox.error = "Required,Empty Field*"
                        return@launch
                    }

                    if (!emailEdt.text!!.contains("admin",true)) {
                        emailBox.error = "Required, add an ADMIN suffix to you detail*"
                        return@launch
                    }

                    viewModel.signUpAdmin(emailEdt.text.toString(), passEdt.text.toString())
                    viewModel.signUpState.collect { requestState ->
                        when (requestState) {
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
                                    "SignUp Successful--> ${requestState.data}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val navigate =
                                    AdminLoginFragmentDirections.actionAdminLoginFragmentToAllUsers2()
                                findNavController().navigate(navigate)
                            }
                            is RequestState.Failure -> {
                                progressBar.isVisible = false
                                Toast.makeText(
                                    requireContext(),
                                    "Error--> ${requestState.msg}",
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

