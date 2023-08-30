package com.tutorials.deviceadminsample.ui.resetpw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.tutorials.deviceadminsample.ui.arch.LockViewModel
import com.tutorials.deviceadminsample.databinding.FragmentForgotPasswordBinding

class ForgotPasswordFragment : Fragment() {
    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.undoBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.sendBtn.setOnClickListener {
            val route =
                ForgotPasswordFragmentDirections.actionForgotPasswordFragmentToOtpVerificationFragment()
            findNavController().navigate(route)
        }
    }

}