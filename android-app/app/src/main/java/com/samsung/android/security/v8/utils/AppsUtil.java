package com.samsung.android.security.v8.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppsUtil {
    
    public static Map<String, Object> getInstalledApps(Context context) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> appsList = new ArrayList<>();
        
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(0);
            
            for (PackageInfo packageInfo : packages) {
                Map<String, Object> appInfo = new HashMap<>();
                
                appInfo.put("packageName", packageInfo.packageName);
                appInfo.put("appName", packageInfo.applicationInfo.loadLabel(pm).toString());
                appInfo.put("versionName", packageInfo.versionName);
                appInfo.put("versionCode", packageInfo.versionCode);
                appInfo.put("installTime", packageInfo.firstInstallTime);
                appInfo.put("updateTime", packageInfo.lastUpdateTime);
                
                int flags = packageInfo.applicationInfo.flags;
                appInfo.put("isSystemApp", (flags & ApplicationInfo.FLAG_SYSTEM) != 0);
                
                appsList.add(appInfo);
            }
            
            result.put("apps", appsList);
            result.put("count", appsList.size());
            result.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
