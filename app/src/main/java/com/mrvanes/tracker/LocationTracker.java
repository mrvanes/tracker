package com.mrvanes.tracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LocationTracker extends Service {
    private final IBinder mBinder = new MyBinder();
    public static final String NOTIFICATION = "com.mrvanes.track";
    private static final int MY_NOTIFICATION_ID = 53241;
    private static final String ACTIVITY = "activity";
    private static final String RESULT = "result";
    private static final String CHECK = "check";
    private static final String STATUS = "status";
    private String PostURL;
    private String salt;
    private Notification.Builder myNotification;
    private NotificationManager myNotifyManager;
    private Timer myTimer = new Timer();
    private MyLocationListener myListener;
    private LocationManager myManager;
    private String uniqueID;
    private String activity;
    private String result;
    private String check;
    private Integer status;
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        Log.d("Track_LT", "Created.");
        uniqueID = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
        activity = "IDLE";
        result = "Service created";
        check = "FAIL";
        status = 2;

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, MY_NOTIFICATION_ID, intent, 0);

        myNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        myNotification = new Notification.Builder(this);
        myNotification
                .setContentTitle("Track Service")
                .setContentText("Track Locationtracker service")
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_service);
        startForeground(MY_NOTIFICATION_ID, myNotification.getNotification());

        myListener = new MyLocationListener();
        myManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            Log.d("Track_LT", "No location persmissions.");
            return;
        }
        myManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, myListener);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        Log.d("Track_LT", "Started.");

        context = getApplicationContext();
        PostURL = context.getString(R.string.post_url);
        salt = context.getString(R.string.post_salt);

        myTimer.schedule(new RemindTask() , 0L, 10000L);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        Log.d("Track_LT", "Destroyed.");
        myTimer.cancel();
        myManager.removeUpdates(myListener);
        myManager = null;

        result = "Service Stopped";
        status = 2;
        sendResult();
        myNotifyManager.cancelAll();
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Log.d("Track_LT", "Bound.");
        result = "Service Started";
        status = 2;
        sendResult();
        return mBinder;
    }

    public class MyBinder extends Binder {
        LocationTracker getService() {
            return LocationTracker.this;
        }
    }

    private class RemindTask extends TimerTask {
        public void run() {
            syncPostData();
            sendResult();
        }
    }

    private void sendResult() {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(ACTIVITY, activity);
        intent.putExtra(RESULT, result);
        intent.putExtra(CHECK, check);
        intent.putExtra(STATUS, status);
        sendBroadcast(intent);
        switch (status) {
            case 2:
                // Network problems
                myNotification.setContentText("Geen netwerk");
                break;
            case 1:
                // Location problems
                myNotification.setContentText("Geen locatie");
                break;
            case 0:
                // No problems
                myNotification.setContentText("Verbonden");
                break;
            default:
                myNotification.setContentText("Onbekende toestand");

        }

        myNotifyManager.notify(MY_NOTIFICATION_ID, myNotification.getNotification());
    }

    /** method for clients */
    public void setServiceActivity(String newActivity) {
        activity = newActivity;
    }

    private class MyLocationListener implements LocationListener {
        public Location mCurrentLocation;

        @Override
        public void onLocationChanged(Location location) {
            mCurrentLocation = location;
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

    private void syncPostData() {
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(PostURL);

        if (myListener.mCurrentLocation == null) {
            result = "NO_LOCATION";
            status = 1;
            return;
        }

        double lat = myListener.mCurrentLocation.getLatitude();
        double lon = myListener.mCurrentLocation.getLongitude();
        float hdop = myListener.mCurrentLocation.getAccuracy();
        float speed = myListener.mCurrentLocation.getSpeed();

//        report = String.format(Locale.US, "%s, speed %2.2f, acc %3.0f", activity, speed, hdop);

        try {
            // Create list of 6 NameValuePairs
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);

            // Add your data
            nameValuePairs.add(new BasicNameValuePair("id", uniqueID));
            nameValuePairs.add(new BasicNameValuePair("activity", activity));
            nameValuePairs.add(new BasicNameValuePair("lat", Double.toString(lat)));
            nameValuePairs.add(new BasicNameValuePair("lon", Double.toString(lon)));
            nameValuePairs.add(new BasicNameValuePair("hdop", Float.toString(hdop)));
            nameValuePairs.add(new BasicNameValuePair("speed", Float.toString(speed)));
            try {
                nameValuePairs.add(new BasicNameValuePair("version", getPackageManager().getPackageInfo(getPackageName(), 0).versionName.toString()));
            } catch (NameNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            String hash = getSecretHash(nameValuePairs);
            nameValuePairs.add(new BasicNameValuePair("hash", hash));

            // Create form from list
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            // Parse return value as JSON object
            String retval = inputStreamToString(response.getEntity().getContent());
            try {
                JSONObject retobj = new JSONObject(retval);
                activity = retobj.getString("activity");
                result = retobj.getString("result");
                check = retobj.getString("check");
//	        	Log.d("Track_LT", "check: " + retobj.getString("check"));
                status = 0;
            } catch (JSONException e) {
                result = "JSON_ERROR";
                status = 2;
            }

        } catch (ClientProtocolException e) {
            result = "UPLOAD_ERROR";
            status = 2;
        } catch (IOException e) {
            result = "CONNECTION_ERROR";
            status = 2;
        }

    }

    private String inputStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        // Read response until the end
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            total.append("No response");
        }

        // Return full string
        return total.toString();
    }

    private String getSecretHash(List<NameValuePair> nvps) {
        String all = salt;
        for (NameValuePair nvp : nvps) {
            all += nvp.toString();
        }
        return md5(all);
    }

    private String md5(String md5) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }
}
