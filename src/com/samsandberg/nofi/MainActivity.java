package com.samsandberg.nofi;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener {

	protected final String TAG = "NoFi_MainActivity";

	static final int MIN_ACCURACY_REQUIRED_METERS = 20;
	static final int MIN_DISTANCE_CHANGE_METERS = 0;
	static final int MIN_DISTANCE_SCAN = 50;
	static final int MAX_SCAN_FREQUENCY_SECONDS = 30;
	
	private DatabaseHelper databaseHelper;
	private List<Hotspot> hotspots;
	
	private LocationManager locationManager;
	private Location myLocation;
	
    private NetworkReceiver networkReceiver;
    private long lastWifiScan;
	
	private RadarView myRadarView;
	private int numUpdates;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        databaseHelper = new DatabaseHelper(this);
        
        // TODO: this is temporary - start building a real DB soon!
        databaseHelper.insertSampleData();
        
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        // And to track scan results updates
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        networkReceiver = new NetworkReceiver(this);
        registerReceiver(networkReceiver, filter);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
		Log.d(TAG, "onResume()");

        // Loader screen for while we wait for a close location
        setContentView(R.layout.main);
        
        TextView tvAccuracy = (TextView) findViewById(R.id.tv_accuracy);
        tvAccuracy.setText("Accuracy: None");
        
        TextView tvLocationUpdates = (TextView) findViewById(R.id.tv_location_updates);
        tvLocationUpdates.setText("Updates...");

        // Sanity check that wifi is enabled, otherwise what's the point?
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (! wifi.isWifiEnabled()) {
            Toast.makeText(this, "Your WiFi is disabled! Enabling it now...", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        networkReceiver.updateMyWifis();

        numUpdates = 0;
        lastWifiScan = 0;
        
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, MIN_DISTANCE_CHANGE_METERS, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, MIN_DISTANCE_CHANGE_METERS, this);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
		Log.d(TAG, "onPause()");

    	// Cancel these items if the person dips
    	if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || 
    		locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) 
    	{
        	locationManager.removeUpdates(this);
    	}
    	myLocation = null;

		LinearLayout layout = (LinearLayout) findViewById(R.id.layout_container);
		if (layout != null) {
			layout.removeViewAt(2);
		}

		networkReceiver.readyForUpdates = false;
		networkReceiver.askingToConnect = false;
    }
    
    @Override 
    public void onDestroy() {
        super.onDestroy();
        // Unregister BroadcastReceiver when app is destroyed.
        if (networkReceiver != null) {
            this.unregisterReceiver(networkReceiver);
        }
    }

	@Override
	public void onLocationChanged(Location location) {
		//Log.d(TAG, "onLocationChanged()");
		//Log.d(TAG, location.toString());
		
		numUpdates++;
		
		TextView tvLocationUpdates = (TextView) findViewById(R.id.tv_location_updates);
		if (tvLocationUpdates != null) {
			String locString = "#" + numUpdates + " " + location.getLatitude() + ", " + location.getLongitude() + " (" + location.getAccuracy() + "m)";
			String tvLocationUpdateStr = locString + "\n" + tvLocationUpdates.getText();
			tvLocationUpdates.setText(tvLocationUpdateStr);
		}
		
		if (! location.hasAccuracy()) {
			Log.d(TAG, "No accuracy");
			return;
		}
		
		TextView tvAccuracy = (TextView) findViewById(R.id.tv_accuracy);
		if (tvAccuracy != null) {
			String accuracyString = "Accuracy: " + location.getAccuracy() + "m";
			if (location.getAccuracy() > MIN_ACCURACY_REQUIRED_METERS) {
				accuracyString += " (" + MIN_ACCURACY_REQUIRED_METERS + "m required)";
			}
			tvAccuracy.setText(accuracyString);
		}
		
		if (location.getAccuracy() > MIN_ACCURACY_REQUIRED_METERS) {
			Log.d(TAG, "Accuracy " + location.getAccuracy() + " > " + MIN_ACCURACY_REQUIRED_METERS);
			return;
		}
		
		if (myLocation == null) {
	        // TODO: make a loader screen for while hotspots load
	        //setContentView(R.layout.?);
	        
			hotspots = databaseHelper.getAllHotspots();
	        networkReceiver.updateHotspots(hotspots);
	        myRadarView = new RadarView(this, hotspots);
	        
			setContentView(R.layout.radar);
			LinearLayout layout = (LinearLayout) findViewById(R.id.layout_container);
			layout.addView(myRadarView, 3, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			
			networkReceiver.readyForUpdates = true;
		}
		myLocation = location;
		myRadarView.updateMyLocation(myLocation);
		
		TextView tvRadarAccuracy = (TextView) findViewById(R.id.tv_radar_accuracy);
		if (tvRadarAccuracy != null) {
			tvRadarAccuracy.setText("Accuracy: " + location.getAccuracy() + "m");
		}
		
		if (lastWifiScan + (MAX_SCAN_FREQUENCY_SECONDS * 1000) < System.currentTimeMillis()) {
			// Set this first (because location could change in between now and 
			// the amount of time it takes to iterate through all the hotspots)
			lastWifiScan = System.currentTimeMillis();
			for (Hotspot hotspot : hotspots) {
				if (myLocation.distanceTo(hotspot) < MIN_DISTANCE_SCAN) {
					Log.d(TAG, "Distance to hotspot \"" + hotspot.ssid + "\" < " + MIN_DISTANCE_SCAN + "m - triggering WiFi scan...");
					WifiUtil.triggerWifiScan(this);
					break;
				}
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG, "onProviderDisabled()");
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "onProviderEnabled()");
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG, "onStatusChanged()");
		// TODO Auto-generated method stub
	}
}