package com.samsandberg.nofi;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.view.View;

public class RadarView extends View {

	protected final String TAG = "NoFi_RadarView";
	
	private Context context;
	private Location myLocation;
	private List<Hotspot> hotspots;
	private Paint mPaintGreen, mPaintRed;
	private float width, height, gridRadius;
	private double xMin, xMax, yMin, yMax, range;

	public RadarView(Context context, List<Hotspot> hotspots) {
		super(context);
		this.context = context;
		this.hotspots = hotspots;

        mPaintGreen = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintGreen.setColor(Color.GREEN);
        
        mPaintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintRed.setColor(Color.RED);
	}
	
	private void prepMinMaxForLocation(Location location) {
    	double lat = location.getLatitude();
    	if (lat < xMin) {
    		xMin = lat;
    	}
    	if (lat > xMax) {
    		xMax = lat;
    	}

    	double lon = location.getLongitude();
    	if (lon < yMin) {
    		yMin = lon;
    	}
    	if (lon > yMax) {
    		yMax = lon;
    	}
		
	}
	
	private void drawInit() {
        width = getWidth();
        height = getHeight();

        float usableWidth = width * 0.9f;
        float usableHeight = height * 0.9f;
        
        gridRadius = Math.min(usableWidth, usableHeight) / 2;

        //Log.d(TAG, "width=" + width + " usableWidth=" + usableWidth);
        //Log.d(TAG, "height=" + height + " usableHeight=" + usableHeight);
        //Log.d(TAG, "gridRadius=" + gridRadius);

        xMin = Double.MAX_VALUE;
        xMax = -1 * Double.MAX_VALUE;
        yMin = Double.MAX_VALUE;
        yMax = -1 * Double.MAX_VALUE;
        
        if (myLocation != null) {
            prepMinMaxForLocation(myLocation);
        }
        for (Hotspot hotspot : hotspots) {
        	prepMinMaxForLocation(hotspot);
        }

        //Log.d(TAG, "xMin=" + xMin + " xMax=" + xMax);
        //Log.d(TAG, "yMin=" + yMin + " yMax=" + yMax);
        
        double xRange = Math.abs(xMax - xMin);
        double yRange = Math.abs(yMax - yMin);
        if (xRange > yRange) {
        	range = xRange;
        	double mid = yMin + yRange / 2;
        	yMin = mid - (range/2);
        	yMax = mid + (range/2);
        } else {
        	range = yRange;
        	double mid = xMin + xRange / 2;
        	xMin = mid - (range/2);
        	xMax = mid + (range/2);
        }

        //Log.d(TAG, "xMin=" + xMin + " xMax=" + xMax);
        //Log.d(TAG, "yMin=" + yMin + " yMax=" + yMax);
	}
	
	private void drawAxes(Canvas canvas) {
		//Log.d(TAG, "drawAxes()");
        canvas.drawLine(width/2 - gridRadius, height/2, width/2 + gridRadius, height/2, mPaintGreen);
        canvas.drawLine(width/2, height/2 - gridRadius, width/2, height/2 + gridRadius, mPaintGreen);
	}
	
	private FloatPoint convertLocationToFloatPoint(Location location) {
		double lat = location.getLatitude();
		double xDist = Math.abs(lat - xMin) / range;
		float x = (width/2) - gridRadius + (float)(xDist * gridRadius);
		
		double lon = location.getLongitude();
		double yDist = Math.abs(lon - yMin) / range;
		float y = (height/2) - gridRadius + (float)(yDist * gridRadius);
		
		return new FloatPoint(x, y);
	}
	
	private void drawHotspots(Canvas canvas) {
		//Log.d(TAG, "drawHotspots()");

		// Draw me...
		if (myLocation != null) {
			//Log.d(TAG, myLocation.toString());
			FloatPoint floatPoint = convertLocationToFloatPoint(myLocation);
			//Log.d(TAG, floatPoint.toString());
			canvas.drawCircle(floatPoint.x, floatPoint.y, 10, mPaintRed);
		}
		
		// Draw hotspots
		for (Hotspot hotspot : hotspots) {
			//Log.d(TAG, hotspot.toString());
			FloatPoint floatPoint = convertLocationToFloatPoint(hotspot);
			//Log.d(TAG, floatPoint.toString());
			canvas.drawCircle(floatPoint.x, floatPoint.y, 10, mPaintGreen);
		}
	}

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawInit();
        drawAxes(canvas);
        drawHotspots(canvas);
    }
    
    public void updateMyLocation(Location location) {
    	myLocation = location;
    	this.invalidate();
    }
}