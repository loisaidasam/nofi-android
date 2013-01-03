package com.samsandberg.nofi;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener {

	protected final String TAG = "NoFi_MainActivity";
	
	static final int MIN_ACCURACY_REQUIRED_METERS = 500; //20;

	private TextView tvAccuracy, tvLocationUpdates;
	
	private LocationManager locationManager;
	private Location myLocation;
	private RadarView myRadarView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
        setContentView(R.layout.main);
        
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        tvAccuracy = (TextView) findViewById(R.id.tv_accuracy);
        tvAccuracy.setText("Accuracy: N/A");
        
        tvLocationUpdates = (TextView) findViewById(R.id.tv_location_updates);
        tvLocationUpdates.setText("");
        
        List<Hotspot> hotspots = new ArrayList<Hotspot>();
        
        // Home
        hotspots.add(new Hotspot(40.734483, -74.001389));
        
        // 6th ave and west 10th
        hotspots.add(new Hotspot(40.73479, -73.998718));
        
        // Work
        hotspots.add(new Hotspot(40.738795, -73.993921));
        
        myRadarView = new RadarView(this, hotspots);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
		Log.d(TAG, "onResume()");
    	
        setContentView(R.layout.main);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
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
    }

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged()");
		Log.d(TAG, location.toString());
		
		String tvLocationUpdateStr = location.toString() + "\n" + tvLocationUpdates.getText();
		Log.d(TAG, tvLocationUpdateStr);
		tvLocationUpdates.setText(tvLocationUpdateStr);
		
		if (! location.hasAccuracy()) {
			Log.d(TAG, "No accuracy");
			return;
		}
		
		tvAccuracy.setText("Accuracy: " + location.getAccuracy() + "m");
		
		if (location.getAccuracy() > MIN_ACCURACY_REQUIRED_METERS) {
			Log.d(TAG, "Accuracy " + location.getAccuracy() + " > " + MIN_ACCURACY_REQUIRED_METERS);
			return;
		}
		
		if (myLocation == null) {
	        setContentView(myRadarView);
		}
		myLocation = location;
		myRadarView.updateMyLocation(myLocation);
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