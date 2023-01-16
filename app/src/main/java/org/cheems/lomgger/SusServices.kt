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

fun saveCreds (context: Context, usr: String, pass: String): Boolean {
    val sharedPreference = context.getSharedPreferences("SUS_PREF", Context.MODE_PRIVATE)
    val editor = sharedPreference.edit()

    editor.putString("usr", usr)
    editor.putString("pass", pass)
    editor.apply()

    return true
}

fun getCreds (context: Context): Array<String?> {
    val sharedPreference = context.getSharedPreferences("SUS_PREF", Context.MODE_PRIVATE)
    val usr = sharedPreference.getString("usr", null)
    val pass = sharedPreference.getString("pass", null)

    return arrayOf<String?>(usr, pass)
}

suspend fun tryLogin () = withContext(Dispatchers.Default) {
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

    println("login is: $loginUrlStr")
    println("token is: $tokenStr")

    val loginUrl = URL(loginUrlStr)
    val loginPageConnection = loginUrl.openConnection() as HttpURLConnection

    // Un-necessary space optimization
    // out = out.drop(out.length)

    BufferedReader(InputStreamReader(loginPageConnection.getInputStream())).use { inp ->
        while (inp.readLine().also { out += it } != null) {
            println(out)
        }
    }

    loginPageConnection.disconnect()

    val loginConnection = loginUrl.openConnection() as HttpURLConnection
    loginConnection.requestMethod = "POST"

    BufferedWriter(OutputStreamWriter(loginConnection.getOutputStream())).use {
        it.write(
            JSONObject()
                .put("username", "20165047")
                .put("password", "ravikota@84000")
                .put("magic", tokenStr)
                .toString())

        it.flush()
    }

    out = ""
//    BufferedReader(InputStreamReader(loginConnection.getInputStream())).use { inp ->
//        while (inp.readLine().also { out += it } != null) {
//            println(out)
//        }
//    }
}