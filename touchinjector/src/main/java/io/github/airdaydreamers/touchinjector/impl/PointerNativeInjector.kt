package io.github.airdaydreamers.touchinjector.impl

import io.github.airdaydreamers.touchinjector.PointerInjector
import timber.log.Timber
import java.io.*

internal class PointerNativeInjector(private val deviceName: String) : PointerInjector {
    companion object {
        const val MAX_TOUCHES = 10

        //region type constants
        const val EV_SYN: Short = 0
        const val EV_KEY: Short = 1
        const val EV_ABS: Short = 3
        //endregion

        //region synchronization constants
        const val SYN_REPORT: Short = 0
        const val SYN_MT_REPORT: Short = 2
        //endregion

        //region absolute axes
        const val ABS_MT_TRACKING_ID: Short = 0x39
        const val ABS_MT_TOUCH_MAJOR: Short = 0x30
        const val ABS_MT_POSITION_X: Short = 0x35
        const val ABS_MT_POSITION_Y: Short = 0x36
        const val ABS_MT_PRESSURE: Short = 0x3a
        const val ABS_MT_SLOT: Short = 0x2f
        //endregion

        //region keys and buttons
        const val KEY_POWER: Short = 0x74
        const val KEY_MENU: Short = 0x8b
        const val KEY_MENU_EM: Short = 0xE5b // ?
        //endregion

        //region states
        const val UP = 0
        const val DOWN = 1
        //endregion
    }

    private var deviceOutputStream: FileOutputStream? = null
    private val eventWriter: EventWriter =
        EventWriter()

    private val pointerItems: MutableList<Int> = MutableList(MAX_TOUCHES) { -1 }

    init {
        initialize()
    }

    //region public method of PointerInjector
    override fun addPointer(id: Int, x: Int, y: Int) {
        downInternal(id, x + 168, y) //TODO: +168 to make dynamic from AOSP display settings
        flush()
    }

    override fun updatePointer(id: Int, x: Int, y: Int) {
        downInternal(id, x + 168, y) //TODO: +168 to make dynamic from AOSP display settings
        flush()
    }

    override fun removePointer(id: Int, x: Int, y: Int) {
        upInternal(id)
        flush()
    }

    override fun dispose() {
        deviceOutputStream?.let {
            try {
                it.close()
            } catch (ioe: IOException) {
                ioe.printStackTrace()
                //DebugLog.error("Can not close device stream...", e);
            }
        }
    }
    //endregion

    private fun initialize() {
        try {
            //Log.i("touch", "Changing device permissions...");
            val p = Runtime.getRuntime().exec(
                arrayOf(
                    "su", "-c",
                    "chmod 777 $deviceName"
                )
            )
            while (true) {
                try {
                    p.waitFor()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                break
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            deviceOutputStream = FileOutputStream(deviceName)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        val file = File(deviceName)
        val size = file.length().toInt()
        val bytes = ByteArray(size)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            buf.read(bytes, 0, bytes.size)
            buf.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        deviceOutputStream?.let(eventWriter::setOutputStream)
    }

    //region not need. for future.
    fun readDev() {
        val file = File(deviceName)
        val bytes = ByteArray(24)
        try {
            val buf = BufferedInputStream(FileInputStream(file))
            while (true) {
                buf.read(bytes, 0, bytes.size)
                if (bytes.isNotEmpty()) {
                    Timber.d("size:%s", bytes.size)
                }
            }
            //buf.close();
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun read(file: String?): ByteArray? {
        var ret: ByteArray? = null
        try {
            val inputStream: InputStream = FileInputStream(file)
            val outputStream = ByteArrayOutputStream()
            var nextByte = inputStream.read()
            while (nextByte != -1) {
                outputStream.write(nextByte)
                nextByte = inputStream.read()
            }
            ret = outputStream.toByteArray()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return ret
    }
    //endregion

    //region internal
    private fun downInternal(id: Int, x: Int, y: Int) {
        val firstUnusedIndex = pointerItems.indexOfFirst { it == -1 }
        val currentPointerIndex = pointerItems.indexOfFirst { it == id }

        when {
            currentPointerIndex != -1 -> {
                eventWriter.writeEvent(
                    EV_ABS,
                    ABS_MT_SLOT,
                    pointerItems.size
                )
                eventWriter.writeEvent(
                    EV_ABS,
                    ABS_MT_POSITION_X,
                    x
                )
                eventWriter.writeEvent(
                    EV_ABS,
                    ABS_MT_POSITION_Y,
                    y
                )
            }
            firstUnusedIndex != -1 -> {
                pointerItems[firstUnusedIndex] = id
                eventWriter.writeEvent(
                    EV_ABS,
                    ABS_MT_SLOT,
                    pointerItems.size
                )
                eventWriter.writeEvent(
                    EV_ABS,
                    ABS_MT_TRACKING_ID,
                    pointerItems[firstUnusedIndex]
                )
                eventWriter.writeEvent(
                    EV_ABS,
                    ABS_MT_POSITION_X,
                    x
                )
                eventWriter.writeEvent(
                    EV_ABS,
                    ABS_MT_POSITION_Y,
                    y
                )
                eventWriter.writeEvent(
                    EV_ABS,
                    ABS_MT_TOUCH_MAJOR,
                    8 //TODO:
                )
                eventWriter.writeEvent(
                    EV_ABS,
                    ABS_MT_PRESSURE,
                    8 //TODO:
                )
            }
            else -> {
                Timber.i("Not enough space in array!")
            }
        }
    }

    private fun upInternal(id: Int) {
        val currentPointerIndex = pointerItems.indexOfFirst { it == id }

        if (currentPointerIndex != -1) {
            pointerItems[currentPointerIndex] = -1
            eventWriter.writeEvent(
                EV_ABS,
                ABS_MT_SLOT,
                pointerItems.size
            )
            eventWriter.writeEvent(
                EV_ABS,
                ABS_MT_TRACKING_ID,
                pointerItems[currentPointerIndex]
            )
        }
    }
    //endregion

    fun sendKey(code: Short, state: Int) {
        eventWriter.writeEvent(
            EV_KEY,
            code,
            state
        )
        flush()
    }

    private fun flush() {
        eventWriter.writeEvent(
            EV_SYN,
            SYN_REPORT,
            0
        )
        try {
            deviceOutputStream?.flush()
        } catch (e: IOException) {
            Timber.w("can not flush%s", e.message)
        }
    }
}