package org.cheems.lomgger

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@SuppressLint("StaticFieldLeak")
class AmogusViewModel : ViewModel() {

    // no it doesn't. stfu.
    private val susServices = SusServices()
    private lateinit var context: Context
    var shouldShowSnackBar: Boolean = false

    fun init(context: Context) {
        this.context = context
        susServices.loadCreds(context)
    }

    private val _isMessageShown = MutableSharedFlow<String>()
    val isMessageShownFlow = _isMessageShown.asSharedFlow()

    private fun showSnackbar(msg: String) {
        viewModelScope.launch {
            _isMessageShown.emit(msg)
        }
    }

    fun getCreds(): Array<String?> {
        return susServices.getCreds()
    }

    fun saveCreds(usr: String, pass: String) {
        susServices.saveCreds(context, usr, pass)
        showSnackbar(SusScaffoldMsg.CREDS_SAVED.msg)
    }

    fun openRepo() {
        susServices.openRepo(context)
    }

    fun getLoginStatus() {
        val res = GlobalScope.async {
            try {
                susServices.getLoginStatus()
                return@async true
            } catch (e: Error) {
                println(e)
                return@async false
            }
        }

        println(res)
        res.invokeOnCompletion {
            showSnackbar(SusScaffoldMsg.ALREADY_LOGGED_IN.msg)
        }
    }

    suspend fun tryLogin() {
        val didLogin = withContext(Dispatchers.Default) {
            tryLogout()
            susServices.tryLogin()
        }

        if (didLogin) {
            showSnackbar(SusScaffoldMsg.LOGGED_IN.msg)
        }
    }

    suspend fun tryLogout() {
        val didLogout = withContext(Dispatchers.Default) {
            susServices.tryLogout()
        }

        if (didLogout) {
            showSnackbar(SusScaffoldMsg.LOGGED_OUT.msg)
        }
    }

    fun tryConnectWifi() {
        if (!this::context.isInitialized) {
            showSnackbar(SusScaffoldMsg.ERROR.msg)
            Log.d(null,"lateinit")
            // return
        }

        susServices.tryConnectWifi(context)
        showSnackbar(SusScaffoldMsg.WIFI_CONNECTED.msg)
    }
}