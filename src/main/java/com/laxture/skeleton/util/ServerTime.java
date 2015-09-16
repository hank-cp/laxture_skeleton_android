package com.laxture.skeleton.util;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

public class ServerTime {

    private static long sLocalTimeOffset;

    public static long currentTimeMillis() {
        return System.currentTimeMillis() + sLocalTimeOffset;
    }

    public static void setServerTime(long serverTime) {
        if (serverTime <= 0) return;
        sLocalTimeOffset = serverTime - System.currentTimeMillis();
    }

    public static DateTime currentDateTime() {
        return new DateTime(currentTimeMillis());
    }

}
