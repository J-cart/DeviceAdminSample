package com.tutorials.deviceadminsample

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        // Use the current time as the default values for the picker
        val calendar: Calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)

        /** Create a new instance of TimePickerDialog and return it
         * && implement OnTimeSetListener on Activity.**/
        return TimePickerDialog(
            requireActivity(),
            requireParentFragment() as TimePickerDialog.OnTimeSetListener,
            hour,
            minute,
            true)
    }

}
