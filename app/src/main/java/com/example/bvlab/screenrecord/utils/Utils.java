package com.example.bvlab.screenrecord.utils;


import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class Utils {
    public static String EXTRA_MSG = "extra_msg";

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return Settings.canDrawOverlays(context);
        }
    }


    public static boolean canWriteSystemSetting(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        } else {
            return Settings.System.canWrite(context);
        }
    }


}
