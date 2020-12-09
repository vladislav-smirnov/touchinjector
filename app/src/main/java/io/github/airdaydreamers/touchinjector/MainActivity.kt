package io.github.airdaydreamers.touchinjector

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import io.github.airdaydreamers.touchinjector.impl.PointerInjector
import io.github.airdaydreamers.touchinjector.impl.PointerInstrumentationInjector
import io.github.airdaydreamers.touchinjector.impl.PointerNativeInjector
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private var pointerInjector: PointerInjector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.plant(Timber.DebugTree()) //NOTE: should be part of class Application but at this moment don't need.

        native_injection_button.setOnClickListener {
            launch {
                startNativeInjection()
            }
        }

        instr_injection_button.setOnClickListener {
            launch {
                startInstrInjection()
            }
        }

        //region to catch touch
        root_view.setOnTouchListener { _: View?, event: MotionEvent ->
            val X = event.rawX.toInt()
            val Y = event.rawY.toInt()
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> Timber.d("ACTION_DOWN x= $X y = $Y")
                MotionEvent.ACTION_UP -> Timber.d("ACTION_UP")
                MotionEvent.ACTION_POINTER_DOWN -> Timber.d("ACTION_POINTER_DOWN")
                MotionEvent.ACTION_POINTER_UP -> Timber.d("ACTION_POINTER_UP")
                MotionEvent.ACTION_MOVE -> Timber.d("ACTION_MOVE x= $X y = $Y")
            }
            true
        }
        //endregion
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private suspend fun startInstrInjection() {
        pointerInjector = PointerInstrumentationInjector()
        //example for one touch.
        val x = 500
        val y = 500
        injection(x, y)
    }

    private suspend fun startNativeInjection() {
        pointerInjector = PointerNativeInjector("/dev/input/event1") //native
        //example for one touch.
        //example for my device.
        //TODO: implement method to calculate coordinates.
        val x = (500 * (32767 - 0 - 1) / (1920 + 0))
        val y = (500 * 45.5097222 + 0.5).roundToInt()

        injection(x, y)
    }

    private suspend fun injection(_x: Int, _y: Int) {
        var x = _x
        var y = _y
        withContext(Dispatchers.IO) {
            SystemClock.sleep(3000)
            val id = 1

            var i = 0

            while (i < 202) {
                if (i == 0) {
                    Timber.d("inject add")
                    pointerInjector?.addPointer(id, x, y)
                } else if (i in 1..199) {
                    Timber.d("inject update")
                    x += i
                    y += i
                    pointerInjector?.updatePointer(id, x, y);
                } else if (i > 200) {
                    Timber.d("inject remove")
                    x += i
                    y += i
                    pointerInjector?.removePointer(id, x, y)
                    break
                }
                i++
                SystemClock.sleep(5) //need just to slow down. to check. you don't need do it.
            }
        }
    }
}