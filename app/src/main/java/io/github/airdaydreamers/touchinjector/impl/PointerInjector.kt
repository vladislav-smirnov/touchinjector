package io.github.airdaydreamers.touchinjector.impl

abstract class PointerInjector @JvmOverloads constructor(protected val deviceName: String? = null) {
    abstract fun addPointer(id: Int, x: Int, y: Int)
    abstract fun updatePointer(id: Int, x: Int, y: Int)
    abstract fun removePointer(id: Int, x: Int, y: Int)
    abstract fun dispose()
}