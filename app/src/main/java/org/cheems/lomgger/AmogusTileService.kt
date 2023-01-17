package org.cheems.lomgger

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class AmogusTileService : TileService() {

    lateinit var susServices: SusServices

    override fun onBind(intent: Intent?): IBinder? {
        println("amogus")
        return super.onBind(intent)
    }

    override fun onClick() {
        super.onClick()

        val sharedPreference = applicationContext.getSharedPreferences("SUS_PREF", Context.MODE_PRIVATE)
        susServices = SusServices(applicationContext)

        if (qsTile.state == Tile.STATE_ACTIVE) {
            GlobalScope.launch {
                susServices.tryLogout()
            }.also {
                qsTile.label = "Login"
                qsTile.state = Tile.STATE_INACTIVE
                qsTile.updateTile()
            }
        }

        else {
            GlobalScope.launch {
                susServices.tryLogin()
            }.also {
                qsTile.label = "Logout"
                qsTile.state = Tile.STATE_ACTIVE
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
    }

    override fun onStartListening() {
        super.onStartListening()

        // Called when the Tile becomes visible
        qsTile.label = "visible"
        qsTile.updateTile()
        println("amogus tile service")
    }

    override fun onStopListening() {
        super.onStopListening()

        // Called when the tile is no longer visible
    }
}
