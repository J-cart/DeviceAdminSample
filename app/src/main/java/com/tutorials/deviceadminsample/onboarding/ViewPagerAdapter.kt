package com.tutorials.deviceadminsample.onboarding

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.tutorials.deviceadminsample.R

class ViewPagerAdapter(val context: Context) : PagerAdapter() {
    var layoutInflater : LayoutInflater? = null

    val imgArray = arrayOf(
        R.drawable.image_7,
        R.drawable.image_8,
        R.drawable.image_9
    )

    val titleArray = arrayOf(
        "Track Your Phone",
        "Lock Your Phone",
        "Ring Your Device"
    )
     val descArray = arrayOf(
         "Stay Connected, Never Lost: Unlock the Power to Effortlessly Track Your Phone and Always Stay in Control of Your Device's Location!",
         "Secure Your World, Anytime, Anywhere: Empower Yourself to Easily Lock Your Phone and Safeguard Your Personal Data!",
         "Find Your Lost Phone in a Jiffy: Ring Your Phone with Confidence  and Never Let It Wander Far From Your Reach!"
        )

    override fun getCount(): Int {
        return titleArray.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object` as RelativeLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater!!.inflate(R.layout.onboarding_pages_layout,container,false)
        val img = view.findViewById<ImageView>(R.id.onboarding_image)
        val titleTxt = view.findViewById<TextView>(R.id.title_text)
         val desc = view.findViewById<TextView>(R.id.desc_text)
        img.setImageResource(imgArray[position])
        titleTxt.text = titleArray[position]
        desc.text = descArray[position]
        container.addView(view)
        return view

    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)

    }

}