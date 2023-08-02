package com.tutorials.deviceadminsample.ui.rv_adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tutorials.deviceadminsample.R
import com.tutorials.deviceadminsample.databinding.DeviceViewholderBinding
import com.tutorials.deviceadminsample.model.DeviceInfo
import com.tutorials.deviceadminsample.util.ACTIVE
import com.tutorials.deviceadminsample.util.INACTIVE

class AllDevicesAdapter : ListAdapter<DeviceInfo, AllDevicesAdapter.ViewHolder>(DiffCallBack) {


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = DeviceViewholderBinding.bind(view)
        fun bind(data: DeviceInfo) {
            binding.apply {
                nameText.text = data.deviceName

                statusText.text = if (data.deviceToken.last() =="0") INACTIVE else ACTIVE
                root.setOnClickListener {
                    listener?.let { it(data) }
                }
            }
        }
    }


    companion object DiffCallBack : DiffUtil.ItemCallback<DeviceInfo>() {
        override fun areItemsTheSame(oldItem: DeviceInfo, newItem: DeviceInfo) =
            oldItem.deviceId == newItem.deviceId

        override fun areContentsTheSame(oldItem: DeviceInfo, newItem: DeviceInfo) =
            oldItem.deviceId == newItem.deviceId && oldItem.deviceName == newItem.deviceName
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

    private var listener: ((DeviceInfo) -> Unit)? = null

    fun adapterClick(listener: (DeviceInfo) -> Unit) {
        this.listener = listener
    }


}