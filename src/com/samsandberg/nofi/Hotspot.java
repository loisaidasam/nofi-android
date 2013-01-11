package com.samsandberg.nofi;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.Log;

public class Hotspot extends Location {

	protected final String TAG = "NoFi_Hotspot";
	
	static final float DEFAULT_RADIUS_LENGTH = 30;
	
	public String ssid, password, macAddress, foursquareId, foursquareName, foursquareType;
	public boolean passwordProtected;
	public float x, y;
	
	protected Paint mPaintGreen;
	
	public Hotspot(String ssid, String macAddress, double latitude, double longitude) {
		super("foo");
		this.ssid = ssid;
		this.macAddress = macAddress;
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		
		this.password = "";
		this.passwordProtected = false;

		this.foursquareId = "";
		this.foursquareName = "";
		this.foursquareType = "";
		
        mPaintGreen = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGreen.setColor(Color.GREEN);
	}
	
	public void setPassword(String password) {
		passwordProtected = true;
		this.password = password;
	}
	
	public void setFoursquareInfo(String foursquareId, String foursquareName, String foursquareType) {
		this.foursquareId = foursquareId;
		this.foursquareName = foursquareName;
		this.foursquareType = foursquareType;
	}
	
	public void draw(Canvas canvas) {
		canvas.drawCircle(x, y, DEFAULT_RADIUS_LENGTH, mPaintGreen);
	}
	
	public boolean clickInsideCircle(float clickX, float clickY) {
		float dist = (float) Math.sqrt(Math.pow(clickX - x, 2) + Math.pow(clickY - y, 2));
		//Log.d(TAG, "Distance to hotspot " + ssid + ": " + dist);
		return (dist <= DEFAULT_RADIUS_LENGTH);
	}
	
	public String toString() {
		String result = "ssid=" + ssid + 
			" macAddress=" + macAddress + 
			" passwordProtected=" + String.valueOf(passwordProtected) +
			" password=" + password +
			" lat=" + getLatitude() +
			" lon=" + getLongitude();
		return result;
	}
}