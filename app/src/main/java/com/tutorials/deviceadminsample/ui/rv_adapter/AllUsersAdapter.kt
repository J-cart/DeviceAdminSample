package com.tutorials.deviceadminsample.ui.rv_adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tutorials.deviceadminsample.R
import com.tutorials.deviceadminsample.databinding.DeviceViewholderBinding
import com.tutorials.deviceadminsample.model.User

class AllUsersAdapter : ListAdapter<User, AllUsersAdapter.ViewHolder>(DiffCallBack) {


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = DeviceViewholderBinding.bind(view)
        fun bind(data: User) {
            binding.apply {
                nameText.text = data.email
                statusText.text = data.password
                root.setOnClickListener {
                    listener?.let { it(data) }
                }
            }
        }
    }


    companion object DiffCallBack : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) =
            oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem.email == newItem.email && oldItem.uid == newItem.uid
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_viewholder, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pos = getItem(position)
        holder.bind(pos)

    }

    private var lockListener: ((User) -> Unit)? = null

    fun lockClick(listener: (User) -> Unit) {
        this.lockListener = listener
    }

    private var alarmListener: ((User) -> Unit)? = null

    fun alarmClick(listener: (User) -> Unit) {
        this.alarmListener = listener
    }

    private var listener: ((User) -> Unit)? = null

    fun adapterClick(listener: (User) -> Unit) {
        this.listener = listener
    }


}