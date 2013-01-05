package com.samsandberg.nofi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	 */

	protected final String TAG = "NoFi_NetworkReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive()");
		
	    ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = conn.getActiveNetworkInfo();
	    if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
	    	WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	    	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	    	if (wifiInfo != null) {
	    		Log.d(TAG, "BSSID: " + wifiInfo.getBSSID());
	    		Log.d(TAG, "SSID: " + wifiInfo.getSSID());
	    		Log.d(TAG, "String: " + wifiInfo.toString());
	    	}
	    }
	}
}