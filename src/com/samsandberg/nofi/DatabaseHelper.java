package com.samsandberg.nofi;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private final String TAG = "NoFi_DatabaseHelper";
	
	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;
 
	private static final String DATABASE_NAME = "nofi";

	public static final int SHARE_LEVEL_PUBLIC = 0;
	public static final int SHARE_LEVEL_FRIENDS = 1; // TODO
	public static final int SHARE_LEVEL_PRIVATE = 2;
	
	private static final String TABLE_HOTSPOTS = "hotspots";
	private static final String KEY_ID = "id";
	private static final String KEY_SSID = "ssid";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_PASSWORD_PROTECTED = "passwordProtected";
	private static final String KEY_MAC_ADDRESS = "macAddress";
	private static final String KEY_LATITUDE = "latitude";
	private static final String KEY_LONGITUDE = "longitude";
	private static final String KEY_FOURSQUARE_ID = "foursquareId";
	private static final String KEY_FOURSQUARE_NAME = "foursquareName";
	private static final String KEY_FOURSQUARE_TYPE = "foursquareType";
	private static final String KEY_LAST_CONNECTED = "lastConnected";
	private static final String KEY_SHARE_LEVEL = "shareLevel";
	private static final String KEY_UPLOADED = "uploaded";

	private SQLiteDatabase readableDB;
	private SQLiteDatabase getReadableDB() {
		if (readableDB == null) {
			readableDB = getReadableDatabase();
		}
		return readableDB;
	}

	private SQLiteDatabase writeableDB;	
	private SQLiteDatabase getWriteableDB() {
		if (writeableDB == null) {
			writeableDB = getWritableDatabase();
		}
		return writeableDB;
	}
	
	public void closeDBs() {
		if (readableDB != null) {
			readableDB.close();
		}
		if (writeableDB != null) {
			writeableDB.close();
		}
	}

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
 
	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_HOTSPOTS_TABLE = "CREATE TABLE " + TABLE_HOTSPOTS + "(" +
			KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			KEY_SSID + " TEXT," +
			KEY_PASSWORD + " TEXT," +
			KEY_PASSWORD_PROTECTED + " INTEGER," +
			KEY_MAC_ADDRESS + " TEXT," +
			KEY_LATITUDE + " REAL," +
			KEY_LONGITUDE + " REAL," +
			KEY_FOURSQUARE_ID + " TEXT," +
			KEY_FOURSQUARE_NAME + " TEXT," +
			KEY_FOURSQUARE_TYPE + " TEXT," +
			KEY_LAST_CONNECTED + " REAL," +
			KEY_SHARE_LEVEL + " INTEGER," +
			KEY_UPLOADED + " REAL" + 
		")";
		db.execSQL(CREATE_HOTSPOTS_TABLE);
	}

	// Upgrading database (do this if we change something)
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		// TODO: figure out why we're resetting... 
		// Wouldn't we want to modify the schema or do something else more clever instead?
		resetDatabase();
	}
	
	public void resetDatabase() {
		SQLiteDatabase db = getWriteableDB();
		
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOTSPOTS);

		// Create tables again
		onCreate(db);
	}
	
	private ContentValues getContentValuesFromData(Hotspot hotspot, long lastConnected, int shareLevel) {
		ContentValues values = new ContentValues();
		values.put(KEY_SSID, hotspot.ssid);
		if (hotspot.passwordProtected) {
			values.put(KEY_PASSWORD, hotspot.password);
			values.put(KEY_PASSWORD_PROTECTED, 1);
		} else {
			values.put(KEY_PASSWORD, "");
			values.put(KEY_PASSWORD_PROTECTED, 0);
		}
		values.put(KEY_MAC_ADDRESS, hotspot.macAddress);
		values.put(KEY_LATITUDE, hotspot.getLatitude());
		values.put(KEY_LONGITUDE, hotspot.getLongitude());
		values.put(KEY_FOURSQUARE_ID, hotspot.foursquareId);
		values.put(KEY_FOURSQUARE_NAME, hotspot.foursquareName);
		values.put(KEY_FOURSQUARE_TYPE, hotspot.foursquareType);
		values.put(KEY_LAST_CONNECTED, lastConnected);
		values.put(KEY_SHARE_LEVEL, shareLevel);
		values.put(KEY_UPLOADED, 0);
		
		return values;
	}
	
	public void updateHotspot(Hotspot hotspot, long lastConnected, int shareLevel) {
		Log.d(TAG, "updateHotspot()");
		
		int hotspotId = getHotspotIdBySSIDAndMacAddress(hotspot.ssid, hotspot.macAddress);
		Log.d(TAG, "hotspotId=" + hotspotId);
		
		SQLiteDatabase db = getWriteableDB();
		ContentValues values = getContentValuesFromData(hotspot, lastConnected, shareLevel);
		
		// Insert
		if (hotspotId == 0) {
			db.insert(TABLE_HOTSPOTS, null, values);
			return;
		}
		
		// Update
		db.update(TABLE_HOTSPOTS, values, KEY_ID + " = ?", new String[] {String.valueOf(hotspotId)});
	}
	
	public void addHotspot(Hotspot hotspot, long lastConnected, int shareLevel) {
		ContentValues values = getContentValuesFromData(hotspot, lastConnected, shareLevel);
		getWriteableDB().insert(TABLE_HOTSPOTS, null, values);
	}
	
	private int getHotspotIdBySSIDAndMacAddress(String ssid, String macAddress) {
		String query = "SELECT " + KEY_ID + " FROM " + TABLE_HOTSPOTS + " " +
			"WHERE " + KEY_SSID + " = ? AND " + KEY_MAC_ADDRESS + " = ?";
		String[] selectionArgs = new String[] {ssid, macAddress};
		Cursor cursor = getReadableDB().rawQuery(query, selectionArgs);
		
		if (cursor == null || ! cursor.moveToFirst()) {
			return 0;
		}
		
		return cursor.getInt(0);
	}
	
	private Hotspot getHotspotFromCursor(Cursor cursor) {
		Hotspot hotspot = new Hotspot(cursor.getString(1), cursor.getString(4), cursor.getDouble(5), cursor.getDouble(6));
		int passwordProtected = cursor.getInt(3);
		if (passwordProtected == 1) {
			hotspot.setPassword(cursor.getString(2));
		}
		return hotspot;
	}
	
	public Hotspot getHotspotBySSIDAndMacAddress(String ssid, String macAddress) {
		String query = "SELECT * FROM " + TABLE_HOTSPOTS + " " +
			"WHERE " + KEY_SSID + " = ? AND " + KEY_MAC_ADDRESS + " = ?";
		String[] selectionArgs = new String[] {ssid, macAddress};
		Cursor cursor = getReadableDB().rawQuery(query, selectionArgs);
		
		if (cursor == null || ! cursor.moveToFirst()) {
			return null;
		}
		
		return getHotspotFromCursor(cursor);
	}
	
	public List<Hotspot> getHotspotsByLocation(Location location, int maxDistance) {
        List<Hotspot> hotspots = new ArrayList<Hotspot>();

		String query = "SELECT * FROM " + TABLE_HOTSPOTS;
		Cursor cursor = getReadableDB().rawQuery(query, null);
		
		if (cursor == null || ! cursor.moveToFirst()) {
			return hotspots;
		}
		
		do {
			Hotspot hotspot = getHotspotFromCursor(cursor);
			
			boolean tooFar = false;
			if (location != null && maxDistance > 0) {
				if (location.distanceTo(hotspot) > maxDistance) {
					tooFar = true;
				}
			}
			
			if (! tooFar) {
				hotspots.add(hotspot);
			}
			
		} while (cursor.moveToNext());
        
		return hotspots;
	}
	
	public List<Hotspot> getAllHotspots() {
		return getHotspotsByLocation(null, 0);
	}
	
	// TODO: implement this stuff
	public List<Hotspot> getAllHotspotsForUpload(int shareLevel) {
        List<Hotspot> hotspots = new ArrayList<Hotspot>();

		String query = "SELECT * FROM " + TABLE_HOTSPOTS +
			" WHERE " + KEY_UPLOADED + " = 0" + 
			" AND " + KEY_SHARE_LEVEL + " = " + shareLevel;
		Cursor cursor = getReadableDB().rawQuery(query, null);
		
		if (cursor == null || ! cursor.moveToFirst()) {
			return hotspots;
		}
		
		do {
			hotspots.add(getHotspotFromCursor(cursor));
		} while (cursor.moveToNext());
		
		return hotspots;
	}
	
	public void insertSampleData() {
		resetDatabase();

        List<Hotspot> hotspots = new ArrayList<Hotspot>();
        hotspots.add(new Hotspot("6th ave and west 10th", "", 40.73479, -73.998718));
        hotspots.add(new Hotspot("7th Avenue South and Greenwich Avenue", "", 40.736602, -74.00114));
        hotspots.add(new Hotspot("7th Avenue South and West 10th", "", 40.73434, -74.002391));
         
        Hotspot zemanta = new Hotspot("Work", "", 40.738795, -73.993921);
        zemanta.setPassword("modernship910");
        hotspots.add(zemanta);
        
        Hotspot home = new Hotspot("Komenskega Ulica", "", 40.734347,-74.001596);
        home.setPassword("tacotuesday");
        hotspots.add(home);
        
        for (Hotspot hotspot : hotspots) {
        	addHotspot(hotspot, 0, SHARE_LEVEL_PUBLIC);
        }
	}
}