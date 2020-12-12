package io.github.airdaydreamers.touchinjector.democlient

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import io.github.airdaydreamers.touchinjector.PointerInjector
import io.github.airdaydreamers.touchinjector.PointerInjectorFactory
import io.github.airdaydreamers.touchinjector.data.InjectionType
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
        pointerInjector = PointerInjectorFactory.getPointerInjector(InjectionType.Instrumentation)
        //example for one touch.
        val x = 500
        val y = 500
        injection(x, y)
    }

    private suspend fun startNativeInjection() {
        pointerInjector =
            PointerInjectorFactory.getPointerInjector(InjectionType.Native("/dev/input/event2"))
        //example for one touch. And for my device.

        //coords for touch
        val originX = 500
        val originY = 500

        /* adb shellgetevent -pl
           this command will show all devices. find the first touch device and we can get parameters.
        */
        val minX = 0 //ABS_MT_POSITION_X     : value 0, min 0, max 32767
        val minY = 0 //ABS_MT_POSITION_Y     : value 0, min 0, max 32767

        val maxX = 32767 //ABS_MT_POSITION_X     : value 0, min 0, max 32767
        val maxY = 32767 //ABS_MT_POSITION_Y     : value 0, min 0, max 32767

        //resolution
        val size = Point()
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealSize(size) //TODO:

        val displayWidth = size.x // for me = 1920
        val displayHeight = size.y //for me = 1080

        //TODO: I'm not sure that is correct. Need check and think
        val displayX = ((originX * (maxX - minX + 1)) / displayWidth) + minX
        val displayY = ((originY * (maxY - minY + 1)) / displayHeight) + minY

        //old values...
        val x = (500 * (32767 - 0 - 1) / (1920 + 0))
        val y = (500 * 45.5097222 + 0.5).roundToInt()

        Timber.d("x: $x y: $y")
        Timber.d("displayX: $displayX displayY: $displayY")
        injection(displayX, displayY)
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