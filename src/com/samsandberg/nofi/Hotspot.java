package com.samsandberg.nofi;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.Log;

public class Hotspot extends Location {

	protected final String TAG = "NoFi_Hotspot";
	
	static final float DEFAULT_RADIUS_LENGTH = 30;
	
	public String ssid, password, macAddress;
	public boolean passwordProtected;
	public float x, y;
	
	protected Paint mPaintGreen;
	
	public Hotspot(String ssid, double latitude, double longitude) {
		super("foo");
		this.ssid = ssid;
		this.password = "";
		this.passwordProtected = false;
		
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		
        mPaintGreen = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGreen.setColor(Color.GREEN);
	}
	
	public void setPassword(String password) {
		passwordProtected = true;
		this.password = password;
	}
	
	public void draw(Canvas canvas) {
		canvas.drawCircle(x, y, DEFAULT_RADIUS_LENGTH, mPaintGreen);
	}
	
	public boolean clickInsideCircle(float clickX, float clickY) {
		float dist = (float) Math.sqrt(Math.pow(clickX - x, 2) + Math.pow(clickY - y, 2));
		//Log.d(TAG, "Distance to hotspot " + ssid + ": " + dist);
		return (dist <= DEFAULT_RADIUS_LENGTH);
	}
}