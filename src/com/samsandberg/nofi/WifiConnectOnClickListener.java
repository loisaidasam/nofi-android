package com.samsandberg.nofi;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

/*
 * Customized listener created so that we can connect to specified wifi on click
 */
public class WifiConnectOnClickListener implements DialogInterface.OnClickListener {
	
	protected NetworkReceiver networkReceiver;
	protected Context context;
	protected String ssid, password, security;
	
	public WifiConnectOnClickListener(NetworkReceiver networkReceiver, Context context, String ssid, String password, String security) {
		this.networkReceiver = networkReceiver;
		this.context = context;
		this.ssid = ssid;
		this.password = password;
		this.security = security;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Toast.makeText(context, "Attempting to connect to hotspot \"" + ssid + "\" ...", Toast.LENGTH_LONG).show();
		WifiUtil.connectToWifiNetwork(context, ssid, password, security);
		networkReceiver.updateMyWifis();
		networkReceiver.askingToConnect = false;
	}
	
}