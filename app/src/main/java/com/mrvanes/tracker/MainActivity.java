package com.mrvanes.tracker;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.mrvanes.tracker.LocationTracker.MyBinder;

public class MainActivity extends AppCompatActivity {
    private TextView activityText;
    private CoordinatorLayout backgroundPlane;
    private Intent myIntent;
    private MenuItem aboutMenu;

    public LocationTracker myServiceBinder;
    public mConnection myConnection;
    public BroadcastReceiver myReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Track", "Create.");

        //res = getResources();
        activityText = (TextView) findViewById(R.id.textView1);
        backgroundPlane = (CoordinatorLayout) findViewById(R.id.main);

        // Allways on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        myIntent = new Intent(this, LocationTracker.class);
        myConnection = new mConnection();
        myReceiver = new mReceiver();

        if (!isMyServiceRunning("com.mrvanes.tracker.LocationTracker")) {
            //Toast.makeText(this, "Starting Service", Toast.LENGTH_SHORT).show();
            startService(myIntent);
        } else {
            //Toast.makeText(this, "Service was running", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Track", "Start.");
        // Connect the client.
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Track", "Resume.");


        // Is service running?
        if (isMyServiceRunning("com.mrvanes.tracker.LocationTracker")) {
            Toast.makeText(this, "Service is running", Toast.LENGTH_SHORT).show();
            // Bind LocationTracker
            bindService(myIntent, myConnection, BIND_AUTO_CREATE);
        } else {
            //Toast.makeText(this, "Service is not running", Toast.LENGTH_SHORT).show();
            aboutMenu.setIcon(R.drawable.ic_action_about_red);
            backgroundPlane.setBackgroundColor(0xFF808080);
            activityText.setText("Geen service");

        }

        // Register receiver
        registerReceiver(myReceiver, new IntentFilter(LocationTracker.NOTIFICATION));
    }

    @Override
    protected void onPause() {
        Log.d("Track", "Pause.");
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
            //Toast.makeText(this, "Service is not running", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        Log.d("Track", "Stop.");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (myConnection != null) {
            //unbindService(myConnection);
            //myConnection = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        aboutMenu = menu.findItem(R.id.about);

        Log.d("Track", "optionsMenu Create.");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.about:
                //start about activity
                Intent intent = new Intent(this, About.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class mConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            //myServiceBinder = ((LocationTracker.MyBinder) binder).getService();
            MyBinder binder = (MyBinder) service;
            myServiceBinder = binder.getService();
            Log.d("Track","Service Connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d("Track","Service Disconnected");
            aboutMenu.setIcon(R.drawable.ic_action_about_red);
            backgroundPlane.setBackgroundColor(0xFF808080);

        }
    };


    class mReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Bundle extras = arg1.getExtras();
            String activity = (String) extras.get("activity");
            String result = (String) extras.get("result");
            Integer status = (Integer) extras.get("status");
            setActivity(activity, result, status);
            //String output = " s:" + activity + ", " + result + ", " + status;
            //Log.d("Track", output);
        }
    }

    public void setActivity(View v) {
        String activity = "IDLE";

        findViewById(R.id.button1).setBackgroundColor(0xFFc0c0c0);
        findViewById(R.id.button2).setBackgroundColor(0xFFc0c0c0);
        findViewById(R.id.button3).setBackgroundColor(0xFFc0c0c0);
        findViewById(R.id.button4).setBackgroundColor(0xFFc0c0c0);
        findViewById(R.id.button5).setBackgroundColor(0xFFc0c0c0);

        switch (v.getId()) {
            case R.id.button1:
                activity = "STATUS_A";
                findViewById(R.id.button1).setBackgroundColor(0xFFa0a0a0);
                break;
            case R.id.button2:
                activity = "STATUS_B";
                findViewById(R.id.button2).setBackgroundColor(0xFFa0a0a0);
                break;
            case R.id.button3:
                activity = "STATUS_C";
                findViewById(R.id.button3).setBackgroundColor(0xFFa0a0a0);
                break;
            case R.id.button4:
                activity = "STATUS_D";
                findViewById(R.id.button4).setBackgroundColor(0xFFa0a0a0);
                break;
            case R.id.button5:
                activity = "STATUS_E";
                findViewById(R.id.button5).setBackgroundColor(0xFFa0a0a0);
                break;
            default:
                return;
        }

        backgroundPlane.setBackgroundColor(0xFF808080);
        activityText.setText("Wacht op reactie");

        if (isMyServiceRunning("com.mrvanes.tracker.LocationTracker")) {
            //Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
            myServiceBinder.setServiceActivity(activity);
        } else {
            //Toast.makeText(this, "Service not running", Toast.LENGTH_SHORT).show();
            activityText.setText("Geen service");
        }
    }

    public void setActivity(String activity, String result, Integer status) {
        //Log.d("Track", "s:" + activity + ", " + result + ", " + status);

        if (aboutMenu == null) {
            // aboutMenu not created yet
            return;
        }

        switch (status) {
            case 2:
                // Something wrong with connection
                aboutMenu.setIcon(R.drawable.ic_action_about_red);
                backgroundPlane.setBackgroundColor(0xFF808080);
                activityText.setText("Geen verbinding");
                break;
            case 1:
                // No location
                aboutMenu.setIcon(R.drawable.ic_action_about_orange);
                backgroundPlane.setBackgroundColor(0xFF808080);
                activityText.setText("Wacht op locatie");
                break;
            case 0:
                // Everything OK
                aboutMenu.setIcon(R.drawable.ic_action_about_green);

                //Reset button colors to unpressed
                findViewById(R.id.button1).setBackgroundColor(0xFFa0a0a0);
                findViewById(R.id.button2).setBackgroundColor(0xFFa0a0a0);
                findViewById(R.id.button3).setBackgroundColor(0xFFa0a0a0);
                findViewById(R.id.button4).setBackgroundColor(0xFFa0a0a0);
                findViewById(R.id.button5).setBackgroundColor(0xFFa0a0a0);

                if (activity.equals("STATUS_A")) {
//    			backgroundPlane.setBackgroundColor(0xFF00FF00);
                    findViewById(R.id.button1).setBackgroundColor(0xFF00FF00);
//    			activityText.setText(R.string.status_a);
                } else if (activity.equals("STATUS_B")) {
//				backgroundPlane.setBackgroundColor(0xFFFFBB00);
                    findViewById(R.id.button2).setBackgroundColor(0xFFFFBB00);
//			    activityText.setText(R.string.status_b);
                } else if (activity.equals("STATUS_C")) {
//    			backgroundPlane.setBackgroundColor(0xFFFF0000);
                    findViewById(R.id.button3).setBackgroundColor(0xFFFF0000);
//			    activityText.setText(R.string.status_c);
                } else if (activity.equals("STATUS_D")) {
//    			backgroundPlane.setBackgroundColor(0xFF8080FF);
                    findViewById(R.id.button4).setBackgroundColor(0xFF8080FF);
//			    activityText.setText(R.string.status_d);
                } else if (activity.equals("STATUS_E")) {
//    			backgroundPlane.setBackgroundColor(0xFF00FFFF);
                    findViewById(R.id.button5).setBackgroundColor(0xFF00FFFF);
//    			activityText.setText(R.string.status_e);
                } else {
                    backgroundPlane.setBackgroundColor(0xFF808080);
                    activityText.setText("Geen activiteit");
                }
                activityText.setText("");
                break;
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

    public void usePhone(View v) {
        Intent showPhone = new Intent(Intent.ACTION_DIAL, null);
        startActivity(showPhone);
    }

    public void useMaps(View v) {
        Intent showMap = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"));
        startActivity(showMap);
    }

}
