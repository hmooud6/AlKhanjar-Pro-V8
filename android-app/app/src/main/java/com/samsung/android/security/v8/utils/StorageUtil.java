package com.samsung.android.security.v8.utils;

import android.os.Environment;
import android.os.StatFs;

public class StorageUtil {
    
    public static long getTotalStorage() {
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            return stat.getBlockSizeLong() * stat.getBlockCountLong();
        } catch (Exception e) {
            return 0;
        }
    }
    
    public static long getFreeStorage() {
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            return stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } catch (Exception e) {
            return 0;
        }
    }
}
