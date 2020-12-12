package io.github.airdaydreamers.touchinjector.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/*

chmod 755 /system/xbin/su

chmod 777 /dev/input/event1

getevent -pl | awk 'BEGIN { RS="add device "; } /^[0-9]/ { print RS $0; }' | grep -B 100 ABS_MT_POSITION_X | awk '/add device/ {print $NF}'

adb shell sendevent /dev/input/event1 0 0 0
*/

class EventWriter {
    private WeakReference<FileOutputStream> deviceOutputStreamReference;

    private byte eventData[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};   //this only for 64 bit. //TODO: add support for 32 bit.
    //private byte eventData[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    //package-private
    void setOutputStream(FileOutputStream deviceOutputStream) {
        deviceOutputStreamReference = new WeakReference<>(deviceOutputStream);
    }

    // TODO:  Need improve speed
    //package-private
    void writeEvent(short type, short code, int value) {
        try {
            //TODO: add support for 32 bit.
            //this only for 64 bit.
            eventData[16] = (byte) (type >>> 0);
            eventData[17] = (byte) (type >>> 8);

            eventData[18] = (byte) (code >>> 0);
            eventData[19] = (byte) (code >>> 8);

            eventData[20] = (byte) (value >>> 0);
            eventData[21] = (byte) (value >>> 8);
            eventData[22] = (byte) (value >>> 16);
            eventData[23] = (byte) (value >>> 24);

            deviceOutputStreamReference.get().write(eventData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
