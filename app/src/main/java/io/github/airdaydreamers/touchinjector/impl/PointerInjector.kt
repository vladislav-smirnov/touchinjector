package io.github.airdaydreamers.touchinjector.impl

interface PointerInjector {
    fun addPointer(id: Int, x: Int, y: Int)
    fun updatePointer(id: Int, x: Int, y: Int)
    fun removePointer(id: Int, x: Int, y: Int)
    fun dispose()
}