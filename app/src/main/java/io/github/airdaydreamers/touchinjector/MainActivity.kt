package io.github.airdaydreamers.touchinjector

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.app.AppCompatActivity
import io.github.airdaydreamers.touchinjector.impl.PointerInjector
import io.github.airdaydreamers.touchinjector.impl.PointerNativeInjector
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private var pointerInjector: PointerInjector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        native_injection_button.setOnClickListener {
            launch {
                startNativeInjection()
            }
        }

        //region to catch touch
        root_view.setOnTouchListener { _: View?, event: MotionEvent ->
            val X = event.rawX.toInt()
            val Y = event.rawY.toInt()
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> Log.e("touch", "ACTION_DOWN x= $X y = $Y")
                MotionEvent.ACTION_UP -> Log.e("touch", "ACTION_UP")
                MotionEvent.ACTION_POINTER_DOWN -> {
                }
                MotionEvent.ACTION_POINTER_UP -> {
                }
                MotionEvent.ACTION_MOVE -> Log.e("touch", "ACTION_MOVE")
            }
            true
        }
        //endregion
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private suspend fun startNativeInjection() {
        pointerInjector = PointerNativeInjector("/dev/input/event1") //native

        //example for one touch.
        withContext(Dispatchers.IO) {
            SystemClock.sleep(3000)

            //example for my device.
            //TODO: implement method to calculate coordinates.
            var x = (500 * (32767 - 0 - 1) / (1920 + 0))
            var y = (500 * 45.5097222 + 0.5).roundToInt()
            val id = 1

            var i = 0

            while (i < 202) {
                if (i == 0) {
                    Log.e("touch", "add")
                    pointerInjector?.addPointer(id, x, y)
                }
                else if (i in 1..199) {
                    Log.e("touch", "update")
                    x += i
                    y += i
                    pointerInjector?.updatePointer(id, x, y);
                }
                else if (i > 200) {
                    Log.e("touch", "remove")
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