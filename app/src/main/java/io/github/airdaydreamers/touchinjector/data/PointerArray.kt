package io.github.airdaydreamers.touchinjector.data

import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties

class PointerArray {
    companion object {
        const val MAX_TOUCHES = 256
    }

    var properties: MutableList<PointerProperties> = arrayListOf()
    var coords: MutableList<PointerCoords> = arrayListOf()

    val size: Int
        get() = properties.size

    fun add(property: PointerProperties, coord: PointerCoords) {
        if (properties.size >= MAX_TOUCHES) {
            //TODO: throw exception
            return
        }
        properties.add(0, property)
        coords.add(0, coord)
    }

    fun remove(index: Int) {
        if (index == -1) {
            return
        }
        properties.removeAt(index)
        coords.removeAt(index)
    }
}