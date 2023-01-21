package org.cheems.lomgger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings

class SusActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tryConnectWifi()
    }

    private fun tryConnectWifi() {
        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager

        if(!wifiManager.isWifiEnabled) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val wifiIntent = Intent(Settings.Panel.ACTION_WIFI)
                 startActivityForResult(wifiIntent, 1)
            } else {
                val wifiIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                 startActivityForResult(wifiIntent, 1)
            }

//            return false
        }

//        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        println("Activity $requestCode resulted $resultCode with data $data")
        finishActivity(0)
    }

    fun tryToggleWifi() {

    }
}