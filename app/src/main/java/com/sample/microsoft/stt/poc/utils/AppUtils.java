package com.sample.microsoft.stt.poc.utils;

import android.content.Context;
import android.provider.Settings;

/**
 * Created by sgarimella on 05/10/17.
 */

public class AppUtils {

    public static String getDeviceUniqueId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
