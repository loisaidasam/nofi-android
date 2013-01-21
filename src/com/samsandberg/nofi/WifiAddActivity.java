package com.samsandberg.nofi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class WifiAddActivity extends Activity implements OnClickListener, OnItemSelectedListener, LocationListener  {

	private final String TAG = "NoFi_WifiAddActivity";
	public static final int MIN_SAVE_ACCURACY_REQUIRED_METERS = 50;
	
	private EditText ssid;
	private EditText macAddress;
	private Spinner passwordProtected;
	private TextView passwordTV;
	private EditText password;
	private Button button;
	
	private LocationManager locationManager;
	private Location myLocation;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		
		setContentView(R.layout.wifiadd);
		
		Intent intent = getIntent();
		
		String intentSsid = intent.getStringExtra("ssid");
		if (intentSsid != null) {
			ssid = (EditText) findViewById(R.id.wifiadd_ssid);
			ssid.setText(intentSsid);
		}
		
		String intentMacAddress = intent.getStringExtra("macAddress");
		if (intentMacAddress != null) {
			macAddress = (EditText) findViewById(R.id.wifiadd_mac_address);
			macAddress.setText(intentMacAddress);
		}
		
		// via http://developer.android.com/guide/topics/ui/controls/spinner.html
		passwordProtected = (Spinner) findViewById(R.id.wifiadd_password_protected);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
			this,
	        R.array.wifiadd_password_protected_choices, 
	        android.R.layout.simple_spinner_item
        );
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		passwordProtected.setAdapter(adapter);
		passwordProtected.setOnItemSelectedListener(this);
		
		DatabaseHelper databaseHelper = new DatabaseHelper(this);
		Hotspot hotspotToBe = databaseHelper.getHotspotBySSIDAndMacAddress(intentSsid, intentMacAddress);
		
		if (hotspotToBe != null && hotspotToBe.passwordProtected) {
			passwordProtected.setSelection(1);
			password = (EditText) findViewById(R.id.wifiadd_password);
			password.setText(hotspotToBe.password);
		}

		button = (Button) findViewById(R.id.wifiadd_button);
		button.setOnClickListener(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
		Log.d(TAG, "onResume()");
		
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
	public void onClick(View view) {
		Log.d(TAG, "onClick()");
		
		ssid = (EditText) findViewById(R.id.wifiadd_ssid);
		String ssidStr = ssid.getText().toString();
		
		macAddress = (EditText) findViewById(R.id.wifiadd_mac_address);
		String macAddressStr = macAddress.getText().toString();
		
		Hotspot hotspot = new Hotspot(myLocation.getLatitude(), myLocation.getLongitude());
		hotspot.setWifiInfo(ssidStr, macAddressStr);
		
		passwordProtected = (Spinner) findViewById(R.id.wifiadd_password_protected);
		if (passwordProtected.getSelectedItem().toString().equals("Yes")) {
			password = (EditText) findViewById(R.id.wifiadd_password);
			hotspot.setPassword(password.getText().toString());
		} else {
			hotspot.setNoPassword();
		}
		
		long lastConnected = System.currentTimeMillis();
		
		// TODO: share level
		int shareLevel = DatabaseHelper.SHARE_LEVEL_PUBLIC;
		
		Log.i(TAG, "Saving hotspot: " + hotspot.toString() + " lastConnected=" + lastConnected + " shareLevel=" + shareLevel);
		
		DatabaseHelper databaseHelper = new DatabaseHelper(this);
		databaseHelper.updateHotspot(hotspot, lastConnected, shareLevel);
		
		finish();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		String result = parent.getItemAtPosition(pos).toString();
		passwordTV = (TextView) findViewById(R.id.wifiadd_password_tv);
		password = (EditText) findViewById(R.id.wifiadd_password);
		if (result.equals("Yes")) {
			passwordTV.setVisibility(View.VISIBLE);
			password.setVisibility(View.VISIBLE);
		} else {
			passwordTV.setVisibility(View.GONE);
			password.setVisibility(View.GONE);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "onLocationChanged()");

		Button button = (Button) findViewById(R.id.wifiadd_button);
		TextView buttonTV = (TextView) findViewById(R.id.wifiadd_button_tv);
		
		// Keep adding dots for every location found
		buttonTV.setText(buttonTV.getText() + ".");
		
		if (! location.hasAccuracy()) {
			Log.d(TAG, "No accuracy");
			return;
		}
		
		if (location.getAccuracy() > MIN_SAVE_ACCURACY_REQUIRED_METERS) {
			Log.d(TAG, "Accuracy " + location.getAccuracy() + " > " + MIN_SAVE_ACCURACY_REQUIRED_METERS);
			return;
		}
		
		myLocation = location;
		buttonTV.setVisibility(View.GONE);
		button.setClickable(true);
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