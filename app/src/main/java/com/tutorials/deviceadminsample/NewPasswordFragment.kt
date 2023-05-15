package com.tutorials.deviceadminsample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.tutorials.deviceadminsample.arch.LockViewModel
import com.tutorials.deviceadminsample.databinding.FragmentNewPasswordBinding

class NewPasswordFragment : Fragment() {
    private var _binding: FragmentNewPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNewPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

}