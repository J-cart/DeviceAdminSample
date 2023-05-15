package com.tutorials.deviceadminsample.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.tutorials.deviceadminsample.R
import com.tutorials.deviceadminsample.databinding.FragmentBoardingBinding
import com.tutorials.deviceadminsample.util.FIRST_LAUNCH
import com.tutorials.deviceadminsample.util.SharedPreference


class BoardingFragment : Fragment() {
    private var _binding: FragmentBoardingBinding? = null
    private val binding get() = _binding!!
    lateinit var pagerAdapter: ViewPagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBoardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val first = SharedPreference.getBoolean(FIRST_LAUNCH, true)
        if (!first) {
            findNavController().navigate(R.id.loginFragment)
        }

        pagerAdapter = ViewPagerAdapter(requireContext())
        binding.viewpager.adapter = pagerAdapter
        binding.dotsIndicator.attachTo(binding.viewpager)
        binding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                // TODO("Not yet implemented")
            }

            override fun onPageScrollStateChanged(state: Int) {
                // TODO("Not yet implemented")
            }
        })

        binding.getStartedBtn.setOnClickListener {
            SharedPreference.putFirstLaunch(FIRST_LAUNCH, false)
            val action = BoardingFragmentDirections.actionBoardingFragmentToLoginFragment()
            findNavController().navigate(action)
        }
    }

}