package com.tutorials.deviceadminsample

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tutorials.deviceadminsample.databinding.UserViewholderBinding
import java.text.SimpleDateFormat
import java.util.*

class AllUsersAdapter: ListAdapter<User, AllUsersAdapter.ViewHolder>(DiffCallBack) {


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = UserViewholderBinding.bind(view)
        fun bind(data: User) {
           // binding.msg.text = data.toString()

            try {
                val textMe = StringBuilder()
                textMe.append(
                    "Display name: Device $adapterPosition\n" +
                            "Email : ${data.email}\n" +
                            "Account type: ${data.accountType}\n" +
                            "Recent alarm time: ${if (data.alarmTime =="0" ){
                                "awaiting time"
                            }else{
                                SimpleDateFormat(TIME_FORMAT_ONE, Locale.getDefault()).format(data.alarmTime.toLong())}
                            }"
                )
                binding.msg.text = textMe
            }catch (e:Exception){
                Log.d("me_adapter", "adapter time format error --->$e")
            }


            binding.lockBtn.setOnClickListener {
                lockListener?.let { it1 -> it1(data) }
            }
            binding.alarmBtn.setOnClickListener {
                alarmListener?.let { it1 -> it1(data) }
            }
        }
    }


    companion object DiffCallBack : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem.email == newItem.email && oldItem.uid == newItem.uid
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllUsersAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_viewholder, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: AllUsersAdapter.ViewHolder, position: Int) {
        val pos = getItem(position)
        holder.bind(pos)

    }

    private var lockListener:((User)->Unit)? = null

    fun lockClick(listener:(User)->Unit){
        this.lockListener = listener
    }

private var alarmListener:((User)->Unit)? = null

    fun alarmClick(listener:(User)->Unit){
        this.alarmListener = listener
    }


}