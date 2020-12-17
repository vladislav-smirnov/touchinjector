package io.github.airdaydreamers.touchinjector.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        Timber.v("onReceive: ========= %s", p1?.action)

        //TODO: will start service which will receive commands
//        val intent = Intent(p0, PointerInjectorService::class.java)
//        p0?.startService(intent)
    }
}