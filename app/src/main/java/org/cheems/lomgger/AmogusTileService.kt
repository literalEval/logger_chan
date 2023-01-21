package org.cheems.lomgger

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.*

@RequiresApi(Build.VERSION_CODES.N)
class AmogusTileService : TileService() {

    private lateinit var susServices: SusServices

    override fun onBind(intent: Intent?): IBinder? {
        println("amogus\n\n\namogus tile bind\n\n\namogus tile bind\n\n\namogus tile bind")
        return super.onBind(intent)
    }

    override fun onClick() {
        super.onClick()

        if (qsTile.state == Tile.STATE_ACTIVE) {
            val res = GlobalScope.launch {
                susServices.tryLogout()
            }

            res.invokeOnCompletion {
                qsTile.label = "Login"
                qsTile.state = Tile.STATE_INACTIVE
                qsTile.icon = Icon.createWithResource(applicationContext, R.drawable.wifi_disconnected)
                qsTile.updateTile()
            }
        }

        else if (qsTile.state == Tile.STATE_INACTIVE) {
            val res = GlobalScope.launch {
                susServices.tryLogin()
            }

            res.invokeOnCompletion {
                qsTile.label = "Logout"
                qsTile.state = Tile.STATE_ACTIVE
                qsTile.icon = Icon.createWithResource(applicationContext, R.drawable.wifi_connected)
                qsTile.updateTile()
            }
        }

        println("amogus tile service")
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        // Do something when the user removes the Tile
    }

    override fun onTileAdded() {
        super.onTileAdded()

        // Do something when the user add the Tile
        onStartListening()
    }

    override fun onStartListening() {
        // Called when the Tile becomes
        super.onStartListening()

        susServices = SusServices()
        susServices.loadCreds(applicationContext)

        qsTile.state = Tile.STATE_UNAVAILABLE

        if (!susServices.isSetup) {
            qsTile.label = "Setup"
        } else if (!getWifiStatus()) {
            qsTile.label = "WiFi Off"
        } else {
            qsTile.label = "Checking"
            getLoginStatus()
        }

        qsTile.updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()

        // Called when the tile is no longer visible
    }

    private fun getWifiStatus(): Boolean {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }

    private fun getLoginStatus() {
        val res = GlobalScope.async {
            try {
                return@async susServices.getLoginStatus()
            } catch (e: Error) {
                println(e)
                return@async false
            }
        }

        println(res)
        res.invokeOnCompletion {
            qsTile.label = if (res.isCancelled) "Login" else "Logout"
            qsTile.state = if (res.isCancelled) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
            qsTile.icon = Icon.createWithResource(applicationContext,
                if (res.isCancelled) R.drawable.wifi_disconnected else R.drawable.wifi_connected)
            qsTile.updateTile()
        }
    }

    suspend fun tryLogin() {
        val didLogin = withContext(Dispatchers.Default) {
            tryLogout()
            susServices.tryLogin()
        }

        qsTile.label = if (didLogin) "Logout" else "Error"
        qsTile.state = if (didLogin) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    private suspend fun tryLogout() {
        val didLogout = withContext(Dispatchers.Default) {
            susServices.tryLogout()
        }

        qsTile.label = if (didLogout) "Login" else "Error"
        qsTile.state = if (didLogout) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        qsTile.updateTile()
    }
}
