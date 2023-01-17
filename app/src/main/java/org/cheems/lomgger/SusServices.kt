package org.cheems.lomgger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


class SusServices(context: Context): Activity() {

    var usr: String? = null
    var pass: String? = null
    var authToken: String? = null
    var logginIn: Boolean = false
    var loggingOut: Boolean = false
    var cachedContext: Context? = null

    init {
        cachedContext = context
        loadCreds(context)
    }

    fun saveCreds(context: Context, usr: String, pass: String): Boolean {
        this.usr = usr
        this.pass = pass

        val sharedPreference = context.getSharedPreferences("SUS_PREF", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()

        editor.putString("usr", usr)
        editor.putString("pass", pass)
        editor.putString("token", authToken)
        editor.apply()

        return true
    }

    private fun loadCreds(context: Context): Array<String?> {
        val sharedPreference = context.getSharedPreferences("SUS_PREF", Context.MODE_PRIVATE)
        this.usr = sharedPreference.getString("usr", null)
        this.pass = sharedPreference.getString("pass", null)

        return arrayOf<String?>(usr, pass)
    }

    fun getCreds(): Array<String?> {
        return arrayOf<String?>(this.usr, this.pass, this.authToken)
    }

    fun openRepo() {
        if (cachedContext == null) {
            return
        }

        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://github.com/literalEval/logger_chan")
        )
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(cachedContext!!, browserIntent, null)
    }

    fun tryConnectWifi() {
        // startForResult.launch(Intent(cachedContext, ActivityCompat::class.java))
        if (tryToggleWifi() && tryConnectToNetwork()) {
            GlobalScope.launch {
                tryLogin()
            }
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//    }

//    val startForResult = registerForActivityResult(
//        contract = ActivityResultContracts.StartActivityForResult(),
//    ) { result: ActivityResult ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val intent = result.data
//            println(intent)
//        }
//    }

    override fun onResume() {
        super.onResume()
        println("resssssssssss")
    }

    private fun tryToggleWifi(): Boolean {
        // TODO: Implement startActivityForResult properly

        val wifiManager = getSystemService(Context.WIFI_SERVICE) as WifiManager

        if(!wifiManager.isWifiEnabled) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val wifiIntent = Intent(Settings.Panel.ACTION_WIFI)
                startActivity(wifiIntent, null)
                // startActivityForResult(wifiIntent, 1)
            } else {
                val wifiIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(wifiIntent, null)
                // startActivityForResult(wifiIntent, 1)
            }

            return false
        }

        return true
    }

    fun buildWifiConfig(): WifiConfiguration {
        val config = WifiConfiguration()
        config.SSID = "\"IIT(BHU)\""
        config.wepKeys[0] = "\"ravikota@84000\""
        // have to set a very high number in order to ensure that
        // Android doesn't immediately drop this connection and reconnect to
        // the a different AP.
        config.priority = 999999
        return config
    }

    private fun tryConnectToNetwork(): Boolean {

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("sdfsdf")
        println(requestCode)
        println(resultCode)
        println(data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    suspend fun tryLogin() = withContext(Dispatchers.Default) {
        println("when the")
        val initUrl = URL("http://192.168.0.1/")

        val initConnection = initUrl.openConnection() as HttpURLConnection
        var out = ""

        BufferedReader(InputStreamReader(initConnection.getInputStream())).use { inp ->
            while (inp.readLine().also { out += it } != null) {
                println(out)
            }
        }

        initConnection.disconnect();

        val loginUrlRegex = Regex("(?<=.location=\")[^\"]+")
        val tokenRegex = Regex("(?<=\\?)[^\"]+")

        val loginUrlFind = loginUrlRegex.find(out, 0) ?: return@withContext
        val tokenFind = tokenRegex.find(out, 0) ?: return@withContext

        val loginUrlStr = loginUrlFind.value
        val tokenStr = tokenFind.value
        this@SusServices.authToken = tokenStr

        println("login is: $loginUrlStr")
        println("token is: $tokenStr")

        val loginUrl = URL(loginUrlStr)
        val loginPageConnection = loginUrl.openConnection() as HttpURLConnection

        BufferedReader(InputStreamReader(loginPageConnection.getInputStream())).use { inp ->
            while (inp.readLine().also { out += it } != null) {
                // println(out)
            }
        }

        println("Got login page")
        loginPageConnection.disconnect()

        val postData =
            "username=${this@SusServices.usr}&password=${this@SusServices.pass}&magic=$tokenStr&4Tredir=/".toCharArray()
        val loginConnection = loginUrl.openConnection() as HttpURLConnection

        loginConnection.requestMethod = "POST"
        loginConnection.setRequestProperty("charset", "utf-8")
        loginConnection.setRequestProperty("Content-length", postData.size.toString())
        loginConnection.setRequestProperty("Content-Type", "application/json")

        BufferedWriter(OutputStreamWriter(loginConnection.getOutputStream())).use {
            it.write(postData)
            it.flush()
        }

        out = ""
        println(loginConnection.responseCode)
        println(loginConnection.responseMessage)
        println(loginConnection.content)

//    BufferedReader(InputStreamReader(loginConnection.getInputStream())).use { inp ->
//        while (inp.readLine().also { out += it } != null) {
//            println(out)
//        }
//    }
    }

    suspend fun tryLogout() = withContext(Dispatchers.Default) {

        if (loggingOut) {
            return@withContext
        }

        loggingOut = true
        println("Trying logout")
        val initUrl = URL("http://192.168.249.1:1000/logout?${this@SusServices.authToken}")
        val logoutConnection = initUrl.openConnection() as HttpURLConnection
        logoutConnection.addRequestProperty("Connection", "close")

        var out: String = ""

        try {
            BufferedReader(InputStreamReader(logoutConnection.getInputStream())).use { inp ->
                while (inp.readLine().also { out += it } != null) {
                     println("it")
                }
            }

        } catch (e: java.lang.Error) {
            println(e)
        }

        logoutConnection.disconnect()
        loggingOut = false
    }
}