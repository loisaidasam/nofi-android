package com.samsandberg.nofi;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class RadarView extends View implements OnTouchListener {

	protected final String TAG = "NoFi_RadarView";
	
	static final float DEFAULT_REAL_RADIUS_LENGTH = 300;
	
	private Context context;
	private Location myLocation;
	private List<Hotspot> hotspots;
	private Paint mPaintGreen, mPaintRed;
	private float width, height, gridRadius, realRadiusLength;
	private float newBearing = -1;
	

	public RadarView(Context context, List<Hotspot> hotspots) {
		super(context);
		this.context = context;
		this.hotspots = hotspots;

        mPaintGreen = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGreen.setColor(Color.GREEN);
        
        mPaintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintRed.setColor(Color.RED);
        
        setOnTouchListener(this);
	}
	
	private void drawInit() {
        width = getWidth();
        height = getHeight();

        float usableWidth = width * 0.9f;
        float usableHeight = height * 0.9f;
        
        gridRadius = Math.min(usableWidth, usableHeight) / 2;
        
        realRadiusLength = DEFAULT_REAL_RADIUS_LENGTH;
        /*
        for (Hotspot hotspot : hotspots) {
        	float dist = myLocation.distanceTo(hotspot);
        	if (dist > realRadiusLength) {
        		realRadiusLength = dist;
        	}
        }
        */

        if (context instanceof Activity) {
        	Activity activity = (Activity) context;
    		TextView tvRadarGridRadius = (TextView) activity.findViewById(R.id.tv_radar_gridradius);
    		if (tvRadarGridRadius != null) {
    			tvRadarGridRadius.setText("Grid Radius Size: " + realRadiusLength + "m");
    		}
        }

        //Log.d(TAG, "width=" + width + " usableWidth=" + usableWidth);
        //Log.d(TAG, "height=" + height + " usableHeight=" + usableHeight);
        //Log.d(TAG, "gridRadius=" + gridRadius);
        //Log.d(TAG, "realRadiusLength=" + realRadiusLength);
	}
	
	private void drawAxes(Canvas canvas) {
		//Log.d(TAG, "drawAxes()");
        canvas.drawLine(width/2 - gridRadius, height/2, width/2 + gridRadius, height/2, mPaintGreen);
        canvas.drawLine(width/2, height/2 - gridRadius, width/2, height/2 + gridRadius, mPaintGreen);
	}
	
	private void drawMe(Canvas canvas) {
		//Log.d(TAG, "drawMe()");
		canvas.drawCircle(width/2, height/2, Hotspot.DEFAULT_RADIUS_LENGTH, mPaintRed);
	}
	
	private void drawNorth(Canvas canvas) {
		//Log.d(TAG, "drawNorth()");
		/*
		Hotspot northPole = new Hotspot("North Pole", 90, 0);
		float bearing = myLocation.bearingTo(northPole);
		
		float x, y;
		if (bearing <= 90) {
			x = width/2 + (float) Math.sin(bearing) * gridRadius;
			y = height/2 - (float) Math.cos(bearing) * gridRadius;
		} else if (bearing <= 180) {
			bearing -= 90;
			x = width/2 + (float) Math.cos(bearing) * gridRadius;
			y = height/2 + (float) Math.sin(bearing) * gridRadius;
		} else if (bearing <= 270) {
			bearing -= 180;
			x = width/2 - (float) Math.sin(bearing) * gridRadius;
			y = height/2 + (float) Math.cos(bearing) * gridRadius;
		} else {
			bearing -= 270;
			x = width/2 - (float) Math.cos(bearing) * gridRadius;
			y = height/2 - (float) Math.sin(bearing) * gridRadius;
		}
		*/
		float x = width/2;
		float y = height/2 - gridRadius;
		
        canvas.drawLine(width/2, height/2, x, y, mPaintRed);
	}
	
	private void drawHotspots(Canvas canvas) {
		//Log.d(TAG, "drawHotspots()");
		
		for (Hotspot hotspot : hotspots) {
			//Log.d(TAG, hotspot.toString());
			hotspot.prepareRelativeLocation(width, height, myLocation, gridRadius, realRadiusLength);
			hotspot.draw(canvas);
		}
	}
	
	private void drawDirectionTriangle(Canvas canvas) {
		//Log.d(TAG, "drawDirectionTriangle()");
		// TODO: fill in this
	}

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        drawInit();

        drawAxes(canvas);
        //drawMe(canvas);
        drawNorth(canvas);
        drawHotspots(canvas);
        
        if (newBearing != -1) {
        	drawDirectionTriangle(canvas);
        }
    }
    
    public void updateMyLocation(Location location) {
    	if (myLocation != null) {
    		newBearing = myLocation.bearingTo(location);
    	}
    	myLocation = location;
    	this.invalidate();
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//Log.d(TAG, "onTouch()");
		if (event.getAction() != MotionEvent.ACTION_DOWN) {
			return false;
		}
		//Log.d(TAG, "onTouch() action is DOWN!");

		for (Hotspot hotspot: hotspots) {
			if (hotspot.clickInsideCircle(event.getX(), event.getY())) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
				alertDialogBuilder.setTitle(hotspot.ssid);
				String message = "";
				if (hotspot.passwordProtected) {
					message += "Password: " + hotspot.password + "\n";
				} else {
					message += "Open (no password)\n";
				}
				message += "Distance: " + myLocation.distanceTo(hotspot) + "m\n";
				alertDialogBuilder.setMessage(message);
				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
				return true;
			}
		}
		return false;
	}
}