package org.cheems.lomgger

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


class SusServices {

    private var usr: String? = null
    private var pass: String? = null
    private var authToken: String? = null
    private var logginIn: Boolean = false
    private var loggingOut: Boolean = false
    var isSetup: Boolean = false

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

    fun loadCreds(context: Context): Array<String?> {
        val sharedPreference = context.getSharedPreferences("SUS_PREF", Context.MODE_PRIVATE)
        this.usr = sharedPreference.getString("usr", null)
        this.pass = sharedPreference.getString("pass", null)

        isSetup = usr != null && usr!!.isNotEmpty() && pass != null && pass!!.isNotEmpty()
        println("Is Setup ? $isSetup")
        return arrayOf(usr, pass)
    }

    fun getCreds(): Array<String?> {
        return arrayOf(this.usr, this.pass, this.authToken)
    }

    fun openRepo(context: Context) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("http://github.com/literalEval/logger_chan")
        )
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(context, browserIntent, null)
    }

    fun tryConnectWifi(context: Context) {
//        tryToggleWifi(context)
//        return

        if (tryToggleWifi(context) && tryConnectToNetwork()) {
            GlobalScope.launch {
                tryLogout()
                tryLogin()
            }
        }
    }

    fun getLoginStatus(): Boolean  {

        val keepaliveReqClient = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .protocols(listOf(Protocol.HTTP_1_1))
            .readTimeout(40, java.util.concurrent.TimeUnit.SECONDS)
            .connectTimeout(40, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val keepAliveRequest = Request.Builder()
            .url("http://192.168.249.1:1000/keepalive?sussussussusbaka")
            .header("Connection", "close")
            .build()

        keepaliveReqClient.newCall(keepAliveRequest).execute().use {
            if (!it.isSuccessful) {
                println("cant get keeplivesadlkfja;sdlkfj\n\n\n\n\nasdlkfjsaldfj\n\n\n\nldfkjsdf\n\n\n")
                throw Exception("BTC exception")
            }

            for ((name, value) in it.headers) {
                println("$name: $value")
            }

            println(it.body!!.string())
            return true
        }
    }

    private fun tryToggleWifi(context: Context): Boolean {
        // TODO: Implement startActivityForResult properly

//        val intent = Intent(context, SusActivity::class.java)
//        startActivity(context, intent, null)
//        return true

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if(!wifiManager.isWifiEnabled) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val wifiIntent = Intent(Settings.Panel.ACTION_WIFI)
                startActivity(context, wifiIntent, null)
                // startActivityForResult(wifiIntent, 1)
            } else {
                val wifiIntent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(context, wifiIntent, null)
                // startActivityForResult(wifiIntent, 1)
            }

            return false
        }

        return true
    }

    private fun tryConnectToNetwork(): Boolean {
        return true
    }

    suspend fun tryLogin(): Boolean = withContext(Dispatchers.IO) {
        val initUrl = URL("http://192.168.0.1/")
        val initConnection = initUrl.openConnection() as HttpURLConnection
        var out = ""

        BufferedReader(InputStreamReader(initConnection.inputStream)).use { inp ->
            while (inp.readLine().also { out += it } != null) {
                println(out)
            }
        }

        initConnection.disconnect();

        val loginUrlRegex = Regex("(?<=.location=\")[^\"]+")
        val tokenRegex = Regex("(?<=\\?)[^\"]+")

        val loginUrlFind = loginUrlRegex.find(out, 0) ?: return@withContext false
        val tokenFind = tokenRegex.find(out, 0) ?: return@withContext false

        val loginUrlStr = loginUrlFind.value
        val tokenStr = tokenFind.value
        this@SusServices.authToken = tokenStr

        println("login is: $loginUrlStr")
        println("token is: $tokenStr")

        val loginUrl = URL(loginUrlStr)
        val loginPageConnection = loginUrl.openConnection() as HttpURLConnection

        BufferedReader(InputStreamReader(loginPageConnection.inputStream)).use { inp ->
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

        BufferedWriter(OutputStreamWriter(loginConnection.outputStream)).use {
            it.write(postData)
            it.flush()
        }

        out = ""
        println(loginConnection.responseCode)
        println(loginConnection.responseMessage)
        println(loginConnection.content)

        return@withContext true

//    BufferedReader(InputStreamReader(loginConnection.getInputStream())).use { inp ->
//        while (inp.readLine().also { out += it } != null) {
//            println(out)
//        }
//    }
    }

    suspend fun tryLogout(): Boolean = withContext(Dispatchers.IO) {

        if (loggingOut) {
            return@withContext false
        }

        loggingOut = true
        println("Trying logout")
        val initUrl = URL("http://192.168.249.1:1000/logout?${this@SusServices.authToken}")
        val logoutConnection = initUrl.openConnection() as HttpURLConnection
        logoutConnection.addRequestProperty("Connection", "close")

        var out: String = ""

        try {
            BufferedReader(InputStreamReader(logoutConnection.inputStream)).use { inp ->
                while (inp.readLine().also { out += it } != null) {
                     println("it")
                }
            }

        } catch (e: java.lang.Error) {
            println(e)
            return@withContext false
        }

        logoutConnection.disconnect()
        loggingOut = false
        println("logged out")
        return@withContext true
    }
}