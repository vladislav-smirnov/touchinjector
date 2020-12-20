Touch Injector
=====
[ ![Download](https://api.bintray.com/packages/vsmirnov/airdaydreamers/io.github.airdaydreamers.touchinjector/images/download.svg?version=0.9.1) ](https://bintray.com/vsmirnov/airdaydreamers/io.github.airdaydreamers.touchinjector/0.9.1/link)

This project demonstrate how to inject touch event on Android OS.

Download
--------

You can download a aar from GitHub's [releases page][4].

Or use Gradle:

```gradle
repositories {
  google()
  jcenter()
}

dependencies {
  implementation 'io.github.airdaydreamers.touchinjector:touchinjector:0.9.1'
}
```

How do I use TouchInjector?
-------------------

### Preparation
You application should has system signature. Because library use:

1) Instrumentation way

```xml
<!-- @SystemApi Allows an application to inject user events (keys, touch, trackball)
         into the event stream and deliver them to ANY window.  Without this
         permission, you can only deliver events to windows in your own process.
         <p>Not for use by third-party applications.
         @hide
    -->
    <permission android:name="android.permission.INJECT_EVENTS"
        android:protectionLevel="signature" />
```
This permissison is need to inject MotionEvent into system. 

2) Native way
For this way we need root access and "su". Actually we also have possibility to configure access to input device.

In the code I use: 

```kotlin
  val p = Runtime.getRuntime().exec(
                arrayOf(
                    "su", "-c",
                    "chmod 777 $deviceName"
                )
            )
```
Libary will read and write into /dev/input/eventX. We need to have possibility to do it.

You can test it on emulator. I created another [repo][5] fot it.

If app will be located into /system/priv-app folder. You need also to put xml with priv-app permissions. If you don't do that -> Android will not started. Look into this [page][6]. Anyway I shared example for it.
#### Steps:
- Download [xml][7] and set your package name.
- Execute commands

```shell
adb root
adb remount
adb push privapp-permissions-airdaydreamers.xml /system/etc/permissions/ 
adb reboot
```
P.S. For some emulators need to disable veryfied boot. So look into [this][8].


For Native method of injection also you may need to do: 
- grant permission for "su" 
```shell
chmod 755 /system/xbin/su
```
and 
- grant permissions for input device. (this is not production way. For production: need setup permission for special user and app)
```shell
chmod 777 /dev/input/eventX 
```
eventX -> where X is number of your input device.

### Usage

#### Init
- Instrumentation way. It's simple way. Don't need root or su.

```kotlin
val pointerInjector = PointerInjectorFactory.getPointerInjector(InjectionType.Instrumentation)
```
or
```kotlin
val pointerInjector = PointerInjectorFactory.getPointerInjector()
```
By default you will get Instrumentation Injector.

- Native way.
```kotlin
val pointerInjector = PointerInjectorFactory.getPointerInjector(InjectionType.Native("/dev/input/eventX"))
```
`/dev/input/eventX` -> this is path to your input device. X -> number. Example: `/dev/input/event2`

To get list of input devices: 
```shell
adb shell getevent -pl 
```
#### Add pointer
```kotlin
pointerInjector.addPointer(id, x, y)
```
#### Move pointer
```kotlin
pointerInjector.updatePointer(id, x, y)
```
#### Remove pointer
```kotlin
pointerInjector.removePointer(id, x, y)
```

-Don't forget dispose Injector.
```kotlin
pointerInjector.dispose()
```
#### Notes
For Native we need to calcute coordinates. Depends on device parameters. 

```kotlin
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

        //TODO: need to check
        val displayX = ((originX * (maxX - minX + 1)) / displayWidth) + minX
        val displayY = ((originY * (maxY - minY + 1)) / displayHeight) + minY
```

Check out the sample app in `demo-client/` to see it in action. But don't forget previous requirements.

TODO
-------------------
I want to implement service with UDP comunitcation. With this service you will have possibility to inject touch from another device. 

- [x] Libary for injection
- [x] Singleuser service
- [ ] Service and broadcast receiver. Don't include if not need. Maybe progard or AndroidManifets manipulation
- [ ] Network communication. Out of the box. Need implement UDP commutioncation. 
- [ ] Demo project for network

Getting Help
------------
To report a specific problem or feature request, [open a new issue on Github][2]. For questions, suggestions, or
anything else, go to [discussion group][3].

Author
------
Vladislav Smirnov - @vladislav-smirnov on GitHub

License
--------

    Copyright 2020 Vladislav Smirnov

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


Disclaimer
---------
This is not stable version.

[1]: https://github.com/vladislav-smirnov/touchinjector
[2]: https://github.com/vladislav-smirnov/touchinjector/issues/new/choose
[3]: https://github.com/vladislav-smirnov/touchinjector/discussions
[4]: https://github.com/vladislav-smirnov/touchinjector/releases
[5]: https://github.com/vladislav-smirnov/automotive-emulator-aosp
[6]: https://source.android.com/devices/tech/config/perms-allowlist
[7]: https://raw.githubusercontent.com/vladislav-smirnov/touchinjector/main/configs/privapp-permissions-airdaydreamers.xml
[8]: https://github.com/vladislav-smirnov/automotive-emulator-aosp#root-and-remount
