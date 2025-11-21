package com.example.lado.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NotificationUtils {

    public static boolean isOlderThan24h(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date notifDate = sdf.parse(timestamp);
            long diff = System.currentTimeMillis() - notifDate.getTime();
            return diff > TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
        } catch (Exception e) {
            return false;
        }
    }
}
