package com.mrvanes.tracker;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.mrvanes.tracker.LocationTracker.MyBinder;

public class About extends AppCompatActivity {
    private Intent myIntent;
    public mConnection myConnection;
    public LocationTracker myServiceBinder;
    private ToggleButton toggleswitch;
    private TextView activityText;
    private TextView resultText;
    private TextView checkText;
    private TextView statusText;
    public BroadcastReceiver myReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Log.d("Track-About", "Create.");

        // Show the Up button in the action bar.
        toggleswitch = (ToggleButton) findViewById(R.id.toggleButton1);
        activityText = (TextView) findViewById(R.id.textView3);
        resultText = (TextView) findViewById(R.id.textView4);
        checkText = (TextView) findViewById(R.id.textView5);
        statusText = (TextView) findViewById(R.id.textView6);

        TextView versionText = (TextView) findViewById(R.id.textView1);
        TextView idText = (TextView) findViewById(R.id.textView2);
        String uniqueID = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);

        myIntent = new Intent(this, LocationTracker.class);
        myConnection = new mConnection();
        myReceiver = new mReceiver();

        myConnection = new mConnection();

        idText.setText(uniqueID);
        try {
            versionText.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Track-About", "Resume.");
        // Is service running?
        if (isMyServiceRunning("com.mrvanes.tracker.LocationTracker")) {
            Toast.makeText(this, "Service is running", Toast.LENGTH_SHORT).show();
            toggleswitch.setChecked(true);
            // Bind LocationTracker
            bindService(myIntent, myConnection, BIND_AUTO_CREATE);
        } else {
            Toast.makeText(this, "Service is not running", Toast.LENGTH_SHORT).show();

        }

        // Register receiver
        registerReceiver(myReceiver, new IntentFilter(LocationTracker.NOTIFICATION));

    }

    @Override
    protected void onPause() {
        Log.d("Track-About", "Pause.");
        super.onPause();

        // Unregister receiver
        unregisterReceiver(myReceiver);

        // Is service running?
        if (isMyServiceRunning("com.mrvanes.tracker.LocationTracker")) {
        	//Toast.makeText(this, "Service is running, unbinding", Toast.LENGTH_SHORT).show();
            //toggleswitch.setActivated(true);
            // Unbind LocationTracker
            unbindService(myConnection);
        } else {
            Toast.makeText(this, "Service is not running", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStop() {
        Log.d("Track-About", "Stop.");
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                //NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class mConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            //myServiceBinder = ((LocationTracker.MyBinder) binder).getService();
            MyBinder binder = (MyBinder) service;
            myServiceBinder = binder.getService();
            Log.d("Track-About","Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d("Track-About","Service Disconnected");
        }
    };

    class mReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Bundle extras = arg1.getExtras();

            String activity = (String) extras.get("activity");
            String result = (String) extras.get("result");
            String check = (String) extras.get("check");
            Integer status = (Integer) extras.get("status");

            activityText.setText(activity);
            resultText.setText(result);
            checkText.setText(check);
            statusText.setText(status.toString());
        }
    }

    public void toggleService(View v) {
        boolean on = ((ToggleButton) v).isChecked();
        if (on) {
            Log.d("Track-About", "Service On.");
            Toast.makeText(this, "Starting location service", Toast.LENGTH_SHORT).show();
            startService(myIntent);
            bindService(myIntent, myConnection, BIND_AUTO_CREATE);

        } else {
            Log.d("Track-About", "Service Off.");
            Toast.makeText(this, "Stopping location service", Toast.LENGTH_SHORT).show();
            unbindService(myConnection);
            stopService(myIntent);
        }
    }

    private boolean isMyServiceRunning(String serviceName) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
