package com.tutorials.deviceadminsample

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tutorials.deviceadminsample.arch.LockViewModel
import com.tutorials.deviceadminsample.databinding.FragmentLoginBinding
import com.tutorials.deviceadminsample.databinding.FragmentOtpVerificationBinding


class OtpVerificationFragment : Fragment() {
    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

}