package com.samsandberg.nofi;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;

public class Hotspot extends Location {

	protected final String TAG = "NoFi_Hotspot";
	
	static final float DEFAULT_RADIUS_LENGTH = 30;
	
	public String ssid, password, macAddress, foursquareId, foursquareName, foursquareType;
	public boolean passwordProtected;
	public List<String> notes;
	
	public float x, y;
	
	protected Paint mPaintGreen;
	
	public Hotspot(double latitude, double longitude) {
		super("foo");
		this.setLatitude(latitude);
		this.setLongitude(longitude);

		this.ssid = "";
		this.macAddress = "";
		this.password = "";
		this.passwordProtected = false;

		this.foursquareId = "";
		this.foursquareName = "";
		this.foursquareType = "";
		
		notes = new ArrayList<String>();
		
        mPaintGreen = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGreen.setColor(Color.GREEN);
	}
	
	public Hotspot setWifiInfo(String ssid, String macAddress) {
		this.ssid = ssid;
		this.macAddress = macAddress;
		return this;
	}
	
	public Hotspot setPassword(String password) {
		passwordProtected = true;
		this.password = password;
		return this;
	}
	
	public Hotspot setNoPassword() {
		password = "";
		passwordProtected = false;
		return this;
	}
	
	public Hotspot setFoursquareInfo(String foursquareId, String foursquareName, String foursquareType) {
		this.foursquareId = foursquareId;
		this.foursquareName = foursquareName;
		this.foursquareType = foursquareType;
		return this;
	}
	
	public Hotspot addNote(String note) {
		notes.add(note);
		return this;
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