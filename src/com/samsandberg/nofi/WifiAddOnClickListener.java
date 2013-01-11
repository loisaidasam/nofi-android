package com.samsandberg.nofi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;

public class WifiAddOnClickListener implements DialogInterface.OnClickListener {
	
	protected Context context;
	protected String ssid;
	protected String macAddress;
	
	public WifiAddOnClickListener(Context context, String ssid, String macAddress) {
		this.context = context;
		this.ssid = ssid;
		this.macAddress = macAddress;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Intent myIntent = new Intent(context, WifiAddActivity.class);
		myIntent.putExtra("ssid", ssid);
		myIntent.putExtra("macAddress", macAddress);
		context.startActivity(myIntent);
		dialog.dismiss();
	}
}