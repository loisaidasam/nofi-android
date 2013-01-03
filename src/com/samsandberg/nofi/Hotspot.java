package com.samsandberg.nofi;

import android.location.Location;

public class Hotspot extends Location {
	
	public Hotspot(double latitude, double longitude) {
		super("foo");
		this.setLatitude(latitude);
		this.setLongitude(longitude);
	}
}