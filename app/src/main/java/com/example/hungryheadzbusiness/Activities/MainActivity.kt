package com.example.hungryheadzbusiness.Activities

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import com.example.hungryheadzbusiness.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val logo=findViewById<ImageView>(R.id.logo)

        //logo.animate().translationX(1400F).setDuration(1000).startDelay = 2000;

        val objectAnimator= ObjectAnimator.ofPropertyValuesHolder(
            logo,
            PropertyValuesHolder.ofFloat("scaleX",1.2F),
            PropertyValuesHolder.ofFloat("scaleY",1.2F)
        )
        objectAnimator.duration=1000
        objectAnimator.repeatCount= ValueAnimator.INFINITE
        objectAnimator.repeatMode= ValueAnimator.REVERSE
        objectAnimator.start()

        if (!isConnectedToInternet(this)) {
            showNoInternetDialogue()
        } else {
            TimerThread().start()
        }
    }

    inner class TimerThread():Thread(){
        override fun run() {
            try {
                synchronized(this) { sleep(2750) }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                val intent = Intent(this@MainActivity, MainActivity2::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun showNoInternetDialogue() {
        val dialog= Dialog(this)
        dialog.setContentView(R.layout.dialogue_box_no_internet)
        dialog.window!!.setBackgroundDrawable(getDrawable(R.drawable.dialogue_bg))
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)
        dialog.show()

        val btn_ok=dialog.findViewById<RelativeLayout>(R.id.btn_ok)
        btn_ok.setOnClickListener{
            dialog.dismiss()
            finish()
        }
    }

    private fun isConnectedToInternet(mainActivity: MainActivity): Boolean {
        val connectivityManager =
            mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wificon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        val mobilecon = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        return wificon != null && wificon.isConnected || mobilecon != null && mobilecon.isConnected
    }
}