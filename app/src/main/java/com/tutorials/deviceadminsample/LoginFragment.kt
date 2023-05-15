package com.tutorials.deviceadminsample

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
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
        /*if (fUser != null) {
            if (!fUser.email!!.contains("admin",true)){
                val navigate = LoginFragmentDirections.actionLoginFragmentToUserFragment()
                findNavController().navigate(navigate)
                return
            }
            findNavController().navigate(R.id.adminLoginFragment)
        }*/
        validateUser()

    }

    private fun validateUser() {

        binding.loginLayout.apply {
            createAccText.setOnClickListener {
                emailEdt.setText("")
                passEdt.setText("")
                this.root.isVisible = false
                binding.signUpLayout.root.isVisible = true
            }

            loginBtn.setOnClickListener {
                val navigate = LoginFragmentDirections.actionLoginFragmentToUserFragment()
                findNavController().navigate(navigate)
/*
                lifecycleScope.launch {
                    if(emailEdt.text.isNullOrEmpty() || passEdt.text.isNullOrEmpty()){
                        emailBox.error = "Required,Empty Field*"
                        return@launch
                    }
                    if (emailEdt.text!!.contains("admin",true)) {
                        emailBox.error = "Required, Invalid (ADMIN) login*"
                        return@launch
                    }
                    viewModel.loginUser(requireContext(),emailEdt.text.toString(), passEdt.text.toString())
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
                                findNavController().navigate(R.id.userFragment)
                            }
                            is RequestState.Failure -> {
                                progressBar.isVisible = false
                                Toast.makeText(
                                    requireContext(),
                                    "Error--> ${it.msg}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is RequestState.NonExistent->{
                                progressBar.isVisible = false
                            }
                        }
                    }

                }
*/


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
                val navigate = LoginFragmentDirections.actionLoginFragmentToUserFragment()
                findNavController().navigate(navigate)
/*
                lifecycleScope.launch {
                    if(emailEdt.text.isNullOrEmpty() || passEdt.text.isNullOrEmpty()){
                        emailBox.error = "Required,Empty Field*"
                        return@launch
                    }

                    if (emailEdt.text!!.contains("admin",true)) {
                        emailBox.error = "Required, field cannot contain an ADMIN login*"
                        return@launch
                    }
                    viewModel.signUpNew(emailEdt.text.toString(), passEdt.text.toString())
                    viewModel.signUpState.collect {
                        when (it) {
                            is RequestState.NonExistent->{
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

                                findNavController().navigate(R.id.userFragment)
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
*/
            }
        }
    }


}