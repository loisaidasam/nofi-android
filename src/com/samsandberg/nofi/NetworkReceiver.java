package com.samsandberg.nofi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver {
	/*
	 * General wifi stuff:
	 * http://developer.android.com/reference/android/net/wifi/package-summary.html
	 * 
	 * Network BroadcastReceiver:
	 * http://developer.android.com/training/basics/network-ops/managing.html
	 * 
	 * What's available in WifiInfo object:
	 * http://developer.android.com/reference/android/net/wifi/WifiInfo.html
	 * (note: getBSSID() is MAC address of router, getMacAddress() is MAC address of phone
	 * 
	 * Wifi connected:
	 * http://stackoverflow.com/questions/5888502/android-wifi-how-to-detect-when-wifi-connection-has-been-established
	 * 
	 * Scan wifi's (and turn it on) programatically:
	 * http://stackoverflow.com/questions/5452940/how-can-i-get-android-wifi-scan-results-into-a-list
	 * 
	 * Connect programatically:
	 * http://stackoverflow.com/questions/8818290/how-to-connect-to-a-specific-wifi-network-in-android-programmatically
	 */

	private final String TAG = "NoFi_NetworkReceiver";
	
	private Context context;
	
	private HashMap<String, Hotspot> hotspotMap;
	private List<String> myWifis;
	
	public boolean readyForUpdates;
	public boolean askingToConnect;
	
	public NetworkReceiver(Context context) {
		super();
		this.context = context;
		readyForUpdates = false;
		askingToConnect = false;
		
		hotspotMap = new HashMap<String, Hotspot>();
		myWifis = new ArrayList<String>();
		updateMyWifis();
	}
	
	public void updateMyWifis() {
		myWifis.clear();
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		// No wifi available
		if (wifi == null) {
			return;
		}
		
        List<WifiConfiguration> wifiConfigList = wifi.getConfiguredNetworks();
        for (WifiConfiguration wifiConfig : wifiConfigList) {
        	// Turn "abcd" into abcd (get rid of the wrapping quotes)
        	String ssid = wifiConfig.SSID.substring(1, wifiConfig.SSID.length()-1);
        	myWifis.add(ssid);
        }
	}
	
	public void updateHotspots(List<Hotspot> hotspots) {
		hotspotMap.clear();
		for (Hotspot hotspot : hotspots) {
			hotspotMap.put(hotspot.ssid, hotspot);
		}
	}
	
	public void checkForNewWifiConnection() {
    	WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    	
    	// No wifi
    	if (wifiManager == null) {
    		return;
    	}
    	
    	updateMyWifis();
    	
    	// No connection info
    	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    	if (wifiInfo == null) {
    		return;
    	}
		
		Log.d(TAG, "Connected to WiFi: " + wifiInfo.toString());
		//Log.d(TAG, "BSSID: " + wifiInfo.getBSSID());
		//Log.d(TAG, "SSID: " + wifiInfo.getSSID());
		//Log.d(TAG, "String: " + wifiInfo.toString());
		
		boolean addNetwork = false;
		
		String ssid = wifiInfo.getSSID();
		String macAddress = wifiInfo.getBSSID();
		
		if (! hotspotMap.containsKey(ssid)) {
			addNetwork = true;
		} else {
			Hotspot hotspot = hotspotMap.get(ssid);
			if (! hotspot.macAddress.equals(macAddress)){
    			addNetwork = true;
			}
		}
		
		if (! addNetwork) {
			return;
		}
		// Build out a dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Add/update a hotspot?");
		String message = "You just joined network \"" + ssid + "\"\n\n";
		message += "Would you like to add this to the list of available hotspots so you can find it again in the future?";
		builder.setMessage(message);
		
		Log.d(TAG, "Asking to save...");
		builder.setPositiveButton("Sure!", new WifiAddOnClickListener(context, ssid, macAddress));
		builder.setNegativeButton("Nah", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	dialog.cancel();
            }
		});
		
		AlertDialog alertDialog = builder.create();
		alertDialog.show();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String intentAction = intent.getAction();
		Log.d(TAG, "onReceive() intentAction: " + intentAction);
		
		if (! readyForUpdates || intentAction == null) {
			return;
		}
		
		// Wifi connected!
		if (intentAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			
			// Not a wifi connection
			if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
				return;
			}
			
			// Not connected (maybe dropped?)
			if (! networkInfo.isConnected()) {
				return;
			}
			
			checkForNewWifiConnection();
			
			return;
		}
		
		// WiFi scan complete
		if (intentAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
			if (askingToConnect) {
				return;
			}
			
			WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			
			// No wifi
			if (wifi == null) {
				return;
			}
			
		    List<ScanResult> results = wifi.getScanResults();
		    Log.d(TAG, results.size() + " scan results available!");
		    for (ScanResult result : results) {
		    	//Log.d(TAG, "SSID: " + result.SSID + " | BSSID: " + result.BSSID + " | capabilities: " + result.capabilities);
		    	if (hotspotMap.containsKey(result.SSID) && (! myWifis.contains(result.SSID))) {
					askingToConnect = true;
		    		
		    		// Build out a dialog
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Hotspot detected!");
					String message = "It seems that you're near hotspot \"" + result.SSID + "\"\n\n";
					message += "Would you like to connect?";
					builder.setMessage(message);
		    		Hotspot hotspot = hotspotMap.get(result.SSID);
		    		String security = WifiUtil.getScanResultSecurity(result);
		    		Log.d(TAG, "Found hotspot SSID " + result.SSID + "! Asking to connect...");
					builder.setPositiveButton("Sure!", new WifiConnectOnClickListener(this, context, result.SSID, hotspot.password, security));
					builder.setNegativeButton("Nah", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int which) {
			            	dialog.cancel();
			            	askingToConnect = false;
			            }
					});
					
					AlertDialog alertDialog = builder.create();
					alertDialog.show();
					break;
		    	}
		    }
		    
    		return;
		}
	}
}