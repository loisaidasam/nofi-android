package com.samsandberg.nofi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiUtil {
	
	private static final String TAG = "NoFi_WifiUtil";
	
	/*
	 * Helpful:
	 * http://stackoverflow.com/questions/5452940/how-can-i-get-android-wifi-scan-results-into-a-list
	 */
	public static void triggerWifiScan(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifi.startScan();
	}
	
	/*
	 * Helpful:
	 * http://stackoverflow.com/questions/8818290/how-to-connect-to-a-specific-wifi-network-in-android-programmatically
	 */
	public static boolean connectToWifiNetwork(Context context, String networkSSID, String networkPass, String security) {
		Log.d(TAG, "Connecting to SSID \"" + networkSSID + "\" with password \"" + networkPass + "\" and with security \"" + security + "\" ...");
		
		// You need to create WifiConfiguration instance like this:
		WifiConfiguration conf = new WifiConfiguration();

		// Please note the quotes. String should contain ssid in quotes
		conf.SSID = "\"" + networkSSID + "\"";
		
		if (security.equals("OPEN")) {
			conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		}
		
		// Then, for WEP network you need to do this:
		// TODO: if your password is in hex, you do not need to surround it with quotes
		else if (security.equals("WEP")) {
			conf.wepKeys[0] = "\"" + networkPass + "\""; 
			conf.wepTxKeyIndex = 0;
			conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		}
		
		// For WPA network you need to add passphrase like this:
		else {
			conf.preSharedKey = "\""+ networkPass +"\"";
		}
		
		// Then, you need to add it to Android wifi manager settings:
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
		int networkId = wifiManager.addNetwork(conf);
		Log.d(TAG, "Network ID: " + networkId);

		//wifiManager.disconnect();
		boolean result = wifiManager.enableNetwork(networkId, true);
		//wifiManager.reconnect();
		wifiManager.saveConfiguration();
		
		return result;
	}
	
	/*
	 * Helpful:
	 * http://stackoverflow.com/questions/6517314/android-wifi-connection-programmatically
	 */
	public static String getScanResultSecurity(ScanResult result) {
	    final String capabilities = result.capabilities;
	    final String[] securityModes = { "WEP", "PSK", "EAP" };

	    for (String securityMode : securityModes) {
	        if (capabilities.contains(securityMode)) {
	            return securityMode;
	        }
	    }

	    return "OPEN";
	}
}
