package org.cheems.lomgger

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class SusServices(context: Context) {

    var usr: String? = null
    var pass: String? = null
    var authToken: String? = null
    var logginIn: Boolean = false
    var loggingOut: Boolean = false

    init {
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

    suspend fun tryLogin() = withContext(Dispatchers.Default) {
        println("when the")
        val initUrl = URL("http://192.168.0.1/")

        val initConnection = initUrl.openConnection() as HttpURLConnection
        var out: String = ""

        BufferedReader(InputStreamReader(initConnection.getInputStream())).use { inp ->
            while (inp.readLine().also { out += it } != null) {
                println(out)
            }
        }

        initConnection.disconnect();

        val loginUrlRegex = Regex("(?<=.location=\")[^\"]+")
        val token_regex = Regex("(?<=\\?)[^\"]+")

        val loginUrlFind = loginUrlRegex.find(out, 0) ?: return@withContext
        val tokenFind = token_regex.find(out, 0) ?: return@withContext

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
    }
}