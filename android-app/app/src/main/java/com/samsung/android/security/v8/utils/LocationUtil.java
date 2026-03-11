package com.samsung.android.security.v8.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationUtil {
    
    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onError(String error);
    }
    
    public static void getCurrentLocation(Context context, LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            callback.onError("Location permission not granted");
            return;
        }
        
        // استخدام Google Play Services إذا كان متاحاً
        try {
            FusedLocationProviderClient fusedClient = 
                    LocationServices.getFusedLocationProviderClient(context);
            
            fusedClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onLocationReceived(location);
                        } else {
                            // طلب موقع جديد
                            requestNewLocation(context, fusedClient, callback);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Fallback إلى LocationManager
                        getLocationWithManager(context, callback);
                    });
            
        } catch (Exception e) {
            // Fallback إلى LocationManager
            getLocationWithManager(context, callback);
        }
    }
    
    private static void requestNewLocation(Context context, 
                                          FusedLocationProviderClient fusedClient,
                                          LocationCallback callback) {
        try {
            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000)
                    .setMinUpdateIntervalMillis(500)
                    .setMaxUpdates(1)
                    .build();
            
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED) {
                
                fusedClient.requestLocationUpdates(
                        locationRequest,
                        new com.google.android.gms.location.LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                                    Location location = locationResult.getLastLocation();
                                    callback.onLocationReceived(location);
                                    fusedClient.removeLocationUpdates(this);
                                }
                            }
                        },
                        Looper.getMainLooper()
                );
            }
        } catch (Exception e) {
            getLocationWithManager(context, callback);
        }
    }
    
    private static void getLocationWithManager(Context context, LocationCallback callback) {
        try {
            LocationManager locationManager = 
                    (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            
            if (locationManager == null) {
                callback.onError("LocationManager not available");
                return;
            }
            
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                    != PackageManager.PERMISSION_GRANTED) {
                callback.onError("Location permission not granted");
                return;
            }
            
            // محاولة الحصول على آخر موقع معروف
            Location lastKnown = null;
            
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            
            if (lastKnown == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            
            if (lastKnown != null) {
                callback.onLocationReceived(lastKnown);
            } else {
                // طلب موقع جديد
                String provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) 
                        ? LocationManager.GPS_PROVIDER 
                        : LocationManager.NETWORK_PROVIDER;
                
                locationManager.requestSingleUpdate(
                        provider,
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                callback.onLocationReceived(location);
                            }
                            
                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {}
                            
                            @Override
                            public void onProviderEnabled(String provider) {}
                            
                            @Override
                            public void onProviderDisabled(String provider) {}
                        },
                        Looper.getMainLooper()
                );
            }
            
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }
    
    /**
     * حساب المسافة بين نقطتين
     */
    public static float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }
    
    /**
     * تحويل الإحداثيات إلى عنوان (يحتاج Geocoder)
     */
    public static String getAddressFromLocation(Context context, double latitude, double longitude) {
        try {
            android.location.Geocoder geocoder = new android.location.Geocoder(context);
            java.util.List<android.location.Address> addresses = 
                    geocoder.getFromLocation(latitude, longitude, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                android.location.Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }
                
                return sb.toString();
            }
        } catch (Exception e) {
            // Ignore
        }
        
        return latitude + ", " + longitude;
    }
}
