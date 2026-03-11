package com.samsung.android.security.v8.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationUtil {
    
    private static final List<Map<String, Object>> notifications = new ArrayList<>();
    
    public static void addNotification(String packageName, String title, String text, long time) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("package", packageName);
        notification.put("title", title);
        notification.put("text", text);
        notification.put("time", time);
        
        notifications.add(0, notification);
        
        if (notifications.size() > 100) {
            notifications.remove(notifications.size() - 1);
        }
    }
    
    public static Map<String, Object> getRecentNotifications() {
        Map<String, Object> result = new HashMap<>();
        result.put("notifications", new ArrayList<>(notifications));
        result.put("count", notifications.size());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
    
    public static void clearNotifications() {
        notifications.clear();
    }
}
