package com.samsung.android.security.v8.utils;

import android.app.ActivityManager;
import android.content.Context;

public class MemoryUtil {
    
    public static long getTotalRam() {
        try {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            return memoryInfo.totalMem;
        } catch (Exception e) {
            return 0;
        }
    }
    
    public static long getAvailableRam(Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            if (activityManager != null) {
                activityManager.getMemoryInfo(memoryInfo);
                return memoryInfo.availMem;
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }
}
