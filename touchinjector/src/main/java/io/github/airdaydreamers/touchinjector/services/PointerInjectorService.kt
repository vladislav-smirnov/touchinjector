package io.github.airdaydreamers.touchinjector.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import timber.log.Timber

//TODO: for future.
class PointerInjectorService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.v("onStartCommand: id: $startId")
        return START_STICKY
    }

    override fun onDestroy() {
        Timber.v("onDestroy:")
        super.onDestroy()
    }

    override fun onCreate() {
        Timber.v("onCreate:")
        super.onCreate()
    }

    override fun onBind(intent: Intent): IBinder? {
        //Note: onBind is not needed.
        return null
    }
}