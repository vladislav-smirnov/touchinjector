#!/usr/bin/python

import subprocess
import os


EV_ABS             = 0x0003
EV_SYN             = 0x0000
ABS_MT_POSITION_X  = 0x0035
ABS_MT_POSITION_Y  = 0x0036
ABS_MT_PRESSURE    = 0x003a
ABS_MT_TOUCH_MAJOR = 0x0030
SYN_REPORT         = 0x0000
ABS_MT_TRACKING_ID = 0x0039

touch_event_id = 0

def adbshell(command, serial=None, adbpath='adb'):
    args = [adbpath]
    if serial is not None:
        args.append('-s')
        args.append(serial)
    args.append('shell')
    args.append(command)
    return os.linesep.join(subprocess.check_output(args).split('\r\n')[0:-1])

def adbdevices(adbpath='adb'):
    return [dev.split('\t')[0] for dev in subprocess.check_output([adbpath, 'devices']).splitlines() if dev.endswith('\tdevice')]

def touchscreen_devices(serial=None, adbpath='adb'):
    return [dev.splitlines()[0].split()[-1] for dev in adbshell('getevent -il', serial, adbpath).split('add device ') if dev.find('ABS_MT_POSITION_X') > -1]

def tap(devicename, x, y, serial=None, adbpath='adb'):
    adbshell('S="sendevent {}";$S 3 57 0;$S 3 53 {};$S 3 54 {};$S 3 58 50;$S 3 50 5;$S 0 2 0;$S 0 0 0;'.format(devicename, x, y), serial, adbpath)
    adbshell('S="sendevent {}";$S 3 57 -1;$S 0 2 0;$S 0 0 0;'.format(devicename), serial, adbpath)

def touch(x, y):

    global touch_event_id

    event_string =  "sendevent /dev/input/event1 %d %d %d\n" % (EV_ABS, ABS_MT_TRACKING_ID, touch_event_id)
    event_string += "sendevent /dev/input/event1 %d %d %d\n" % (EV_ABS, ABS_MT_POSITION_X,  21691)
    event_string += "sendevent /dev/input/event1 %d %d %d\n" % (EV_ABS, ABS_MT_POSITION_Y,  23528)
    event_string += "sendevent /dev/input/event1 %d %d %d\n" % (EV_ABS, ABS_MT_PRESSURE,    5)
    event_string += "sendevent /dev/input/event1 %d %d %d\n" % (EV_ABS, ABS_MT_TOUCH_MAJOR, 147)
    event_string += "sendevent /dev/input/event1 %d %d %d\n" % (EV_SYN, SYN_REPORT,         0)

    event_string += "sendevent /dev/input/event1 %d %d %d\n" % (EV_ABS, ABS_MT_TRACKING_ID, -1)
    event_string += "sendevent /dev/input/event1 %d %d %d\n" % (EV_SYN, SYN_REPORT,         0)

    touch_event_id+=1


    os.system('adb shell "%s" &' % event_string)

serial = adbdevices()[0]
touch(500, 500)
#touchdev = touchscreen_devices(serial)[0]
#touchdev  = '/dev/input/event1'
#tap(touchdev, 500, 500, serial)










