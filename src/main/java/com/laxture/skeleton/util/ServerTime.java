package com.laxture.skeleton.util;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

public class ServerTime {

    private static long sLocalTimeOffset;

    public static long currentTimeMillis() {
        return System.currentTimeMillis() + sLocalTimeOffset;
    }

    public static long currentTime() {
        return (System.currentTimeMillis() + sLocalTimeOffset)/1000;
    }

    public static void setServerTime(long serverTime) {
        if (serverTime <= 0) return;
        sLocalTimeOffset = serverTime*1000 - System.currentTimeMillis();
    }

    public static DateTime currentDateTime() {
        return new DateTime(currentTimeMillis());
    }

    public static DateMidnight currentDateMidnight() {
        return new DateMidnight(currentTimeMillis());
    }

}
