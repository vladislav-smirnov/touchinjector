package io.github.airdaydreamers.touchinjector.impl

import android.app.Instrumentation
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties
import io.github.airdaydreamers.touchinjector.PointerInjector
import io.github.airdaydreamers.touchinjector.data.PointerArray
import timber.log.Timber


internal class PointerInstrumentationInjector : PointerInjector {
    private val instrumentation: Instrumentation = Instrumentation()
    private val pointerArray = PointerArray()

    private var downTime: Long = 0 //TODO:

    init {
        instrumentation.start()
        instrumentation.setInTouchMode(true)
    }

    //region internal
    //TODO: workaround remove pointer before inject if it was already existed.
    private fun checkTouches(id: Int) {
        if (pointerArray.size > 0) {
            pointerArray.properties.firstOrNull { it.id == id }?.let {
                val index = pointerArray.properties.indexOf(it)

                upInternal(
                    it.id,
                    pointerArray.coords[index].x.toInt(),
                    pointerArray.coords[index].y.toInt()
                )
            }
        }
    }

    private fun downInternal(id: Int, x: Int, y: Int) {
        val isFirstPointer = pointerArray.size == 0
        val pp = PointerProperties()
        pp.id = id
        val pc = PointerCoords()
        pc.x = x.toFloat()
        pc.y = y.toFloat()
        checkTouches(id) //TODO:
        pointerArray.add(pp, pc)
        val motionEvent: MotionEvent
        if (isFirstPointer) {
            motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x.toFloat(), y.toFloat(), 0
            )

            downTime = SystemClock.uptimeMillis()
        } else {
            motionEvent = MotionEvent.obtain(
                SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), MotionEvent.ACTION_POINTER_DOWN,
                pointerArray.size, pointerArray.properties.toTypedArray(),
                pointerArray.coords.toTypedArray(), 0, 1, 1f, 1f, 0, 0,
                InputDevice.SOURCE_TOUCHSCREEN, 0
            )
            downTime = SystemClock.uptimeMillis()
        }
        instrumentation.sendPointerSync(motionEvent)
    }

    private fun moveInternal(id: Int, x: Int, y: Int) {
        if (pointerArray.size > 0) {
            try {
                val motionEvent = MotionEvent.obtain(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, x.toFloat(), y.toFloat(), 0
                )
                instrumentation.sendPointerSync(motionEvent)
            } catch (e: Exception) { //TODO: don't do like that. use special exception
                Timber.w(e)
            }
        }
    }

    private fun upInternal(id: Int, x: Int, y: Int) {
        if (pointerArray.size > 0) {
            val index = pointerArray.properties.indexOfFirst { it.id == id }
            val motionEvent: MotionEvent
            if (pointerArray.size > 1) {
                motionEvent = MotionEvent.obtain(
                    downTime,
                    SystemClock.uptimeMillis(),
                    (index shl MotionEvent.ACTION_POINTER_INDEX_SHIFT) + MotionEvent.ACTION_POINTER_UP,
                    pointerArray.size,
                    pointerArray.properties.toTypedArray(),
                    pointerArray.coords.toTypedArray(),
                    0,
                    1,
                    1f,
                    1f,
                    0,
                    0,
                    InputDevice.SOURCE_TOUCHSCREEN,
                    0
                )
            } else {
                motionEvent = MotionEvent.obtain(
                    SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
                    x.toFloat(), y.toFloat(), 0
                )
            }
            instrumentation.sendPointerSync(motionEvent)

            //TODO:
            //mEvent.recycle();
            pointerArray.remove(index)
        }
    }
    //endregion

    //region public
    override fun addPointer(id: Int, x: Int, y: Int) {
        downInternal(id, x, y)
    }

    override fun updatePointer(id: Int, x: Int, y: Int) {
        moveInternal(id, x, y)
    }

    override fun removePointer(id: Int, x: Int, y: Int) {
        upInternal(id, x, y)
    }

    override fun dispose() {

    }
    //endregion
}