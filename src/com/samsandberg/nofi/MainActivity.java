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
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener {

	protected final String TAG = "NoFi_MainActivity";
	
	static final int MIN_ACCURACY_REQUIRED_METERS = 20;
	
	private LocationManager locationManager;
	private Location myLocation;
	private RadarView myRadarView;
	private int numUpdates;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
        setContentView(R.layout.main);
        
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        TextView tvAccuracy = (TextView) findViewById(R.id.tv_accuracy);
        tvAccuracy.setText("Accuracy: None");
        
        TextView tvLocationUpdates = (TextView) findViewById(R.id.tv_location_updates);
        tvLocationUpdates.setText("Updates..");
        
        List<Hotspot> hotspots = new ArrayList<Hotspot>();
        hotspots.add(new Hotspot("Home", 40.734483, -74.001389));
        hotspots.add(new Hotspot("6th ave and west 10th", 40.73479, -73.998718));
        hotspots.add(new Hotspot("7th Avenue South and Greenwich Avenue", 40.7366018, -74.0011397));
        hotspots.add(new Hotspot("Work", 40.738795, -73.993921));
        
        myRadarView = new RadarView(this, hotspots);
        
        numUpdates = 0;
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

		LinearLayout layout = (LinearLayout) findViewById(R.id.layout_container);
		if (layout != null) {
			layout.removeViewAt(2);
		}
    }

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged()");
		Log.d(TAG, location.toString());
		
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
			setContentView(R.layout.radar);
			LinearLayout layout = (LinearLayout) findViewById(R.id.layout_container);
			layout.addView(myRadarView, 3, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		}
		myLocation = location;
		myRadarView.updateMyLocation(myLocation);
		
		TextView tvRadarAccuracy = (TextView) findViewById(R.id.tv_radar_accuracy);
		if (tvRadarAccuracy != null) {
			tvRadarAccuracy.setText("Accuracy: " + location.getAccuracy() + "m");
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