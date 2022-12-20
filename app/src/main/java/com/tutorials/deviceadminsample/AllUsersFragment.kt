package com.tutorials.deviceadminsample

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TimePicker
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tutorials.deviceadminsample.databinding.FragmentAllUsersBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AllUsersFragment : Fragment(){
    private var _binding: FragmentAllUsersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LockViewModel by activityViewModels()
    private val adapter by lazy { AllUsersAdapter() }
    private lateinit var user: User
    private val fUser = Firebase.auth.currentUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAllUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.addAllUserSnapshot()
        fUser?.email?.let {
            (activity as MainActivity).supportActionBar?.title = it
        }?: "Admin Account"
        lifecycleScope.launch { observeAllUsers() }
        newMenu()
    }

    private suspend fun observeAllUsers() {

        viewModel.getAllUsers()
        viewModel.allUsers.collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    //show Loading
                    errorState(false)
                    loadingState(true)
                }
                is Resource.Successful -> {
                    //display result
                    loadingState(false)
                    binding.recyclerView.adapter = adapter
                    resource.data?.let {
                        if (it.isNotEmpty()){
                            adapter.submitList(it)
                            errorState(false)
                            binding.statusTv.text = "Total Users -- ${it.size}"
                        }else{
                            errorState(true)
                            binding.statusTv.text = "Total Users -- 0"
                            adapter.submitList(emptyList())
                        }
                    } ?: emptyList<User>()
                    adapter.lockClick {
                        val user = it.copy(commandType = LOCK)
                        viewModel.sendPushNotifier(user)
                    }
                    tryDate()
                }
                is Resource.Failure -> {
                    //show error
                    loadingState(false)
                    errorState(true)
                    resource.msg?.let {
                        binding.errorText.text = it
                        adapter.submitList(emptyList())
                        binding.statusTv.text = "Total Users -- 0"
                    }
                }
            }

        }
    }

    private fun errorState(state: Boolean) {
        binding.errorText.isVisible = state
    }

    private fun loadingState(state: Boolean) {
        binding.progressBar.isVisible = state
    }
  private fun tryDate(){

        val myCalendar = Calendar.getInstance()

        val timePickerOnDataSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            myCalendar.set(Calendar.HOUR_OF_DAY, hour)
            myCalendar.set(Calendar.MINUTE, minute)

            Log.d("TIMER2","${myCalendar.time}")
            Log.d("TIMER3","${myCalendar.timeInMillis}")
            viewModel.sendPushNotifier(user.copy(commandType = ALARM, alarmTime = myCalendar.timeInMillis.toString()))
            binding.statusTv.text =SimpleDateFormat(TIME_FORMAT_ONE, Locale.getDefault()).format(myCalendar.time)
        }

        adapter.alarmClick {
            user = it
            TimePickerDialog(
                context,
                timePickerOnDataSetListener,
                myCalendar.get(Calendar.HOUR_OF_DAY),
                myCalendar.get(Calendar.MINUTE),
                false
            ).show()
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
                        FirebaseMessagingReceiver.updateToken(requireContext(), "0")
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

}