package com.samsung.android.security.v8;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class SecretCodeActivity extends Activity {
    
    private static final String SECRET_CODE = "2026";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // التحقق من الكود السري
        showCodeDialog();
    }
    
    private void showCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Samsung Security");
        builder.setMessage("أدخل رمز الأمان:");
        
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);
        
        builder.setPositiveButton("تأكيد", (dialog, which) -> {
            String code = input.getText().toString();
            
            if (SECRET_CODE.equals(code)) {
                // الكود صحيح - إظهار الخيارات
                showOptionsDialog();
            } else {
                Toast.makeText(this, "رمز خاطئ", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        
        builder.setNegativeButton("إلغاء", (dialog, which) -> {
            finish();
        });
        
        builder.setCancelable(false);
        builder.show();
    }
    
    private void showOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("خيارات الأمان");
        
        String[] options = {
                "إظهار الأيقونة",
                "إخفاء الأيقونة",
                "معلومات التطبيق",
                "إلغاء التثبيت"
        };
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Show icon
                    showAppIcon();
                    break;
                case 1: // Hide icon
                    hideAppIcon();
                    break;
                case 2: // App info
                    showAppInfo();
                    break;
                case 3: // Uninstall
                    uninstallApp();
                    break;
            }
        });
        
        builder.setNegativeButton("إغلاق", (dialog, which) -> {
            finish();
        });
        
        builder.show();
    }
    
    private void showAppIcon() {
        try {
            ComponentName componentName = new ComponentName(this, MainActivity.class);
            getPackageManager().setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            );
            
            Toast.makeText(this, "تم إظهار الأيقونة", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    
    private void hideAppIcon() {
        try {
            ComponentName componentName = new ComponentName(this, MainActivity.class);
            getPackageManager().setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            );
            
            Toast.makeText(this, "تم إخفاء الأيقونة", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finish();
    }
    
    private void showAppInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("معلومات التطبيق");
        builder.setMessage(
                "الإصدار: 8.0\n" +
                "الحزمة: com.samsung.android.security.v8\n" +
                "الحالة: نشط"
        );
        builder.setPositiveButton("موافق", (dialog, which) -> {
            finish();
        });
        builder.show();
    }
    
    private void uninstallApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("تأكيد الحذف");
        builder.setMessage("هل أنت متأكد من حذف التطبيق؟\n\nسيتم حذف جميع البيانات.");
        
        builder.setPositiveButton("نعم، احذف", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            finish();
        });
        
        builder.setNegativeButton("إلغاء", (dialog, which) -> {
            finish();
        });
        
        builder.show();
    }
}
