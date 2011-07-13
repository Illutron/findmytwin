package processing.android.test.gps_example;

import processing.core.*; 
import processing.xml.*; 

import android.content.Context; 
import android.location.Location; 
import android.location.LocationManager; 
import android.location.LocationListener; 
import android.location.GpsStatus.Listener; 
import android.location.GpsStatus.NmeaListener; 
import android.os.Bundle; 

import android.view.MotionEvent; 
import android.view.KeyEvent; 
import android.graphics.Bitmap; 
import java.io.*; 
import java.util.*; 

public class gps_example extends PApplet {

// Imports





 // not needed yet, but going for nmea data next!


String[] fontList;
PFont androidFont;

LocationManager locationManager;
MyLocationListener locationListener;

// Variables to hold the current GPS data
float currentLatitude  = 0;
float currentLongitude = 0;
float currentAccuracy  = 0;
String currentProvider = "";

//-----------------------------------------------------------------------------------------

public void setup() {
 
  background(0);
  fontList = PFont.list();
  androidFont = createFont(fontList[5], 35, true);
  textFont(androidFont);
}

//-----------------------------------------------------------------------------------------

public void draw() {
  background(0);
  // Display current GPS data
  text("Latitude: "+currentLatitude, 20, 40);
  text("Longitude: "+currentLongitude, 20, 75);
  text("Accuracy: "+currentAccuracy, 20, 110);
  text("Provider: "+currentProvider, 20, 145);  
}

//-----------------------------------------------------------------------------------------

public void onResume() {
  super.onResume();
  // Build Listener
  locationListener = new MyLocationListener();
  // Acquire a reference to the system Location Manager
  locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
  // Register the listener with the Location Manager to receive location updates
  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
}

public void onPause() {
  super.onPause();
}

//-----------------------------------------------------------------------------------------

// Define a listener that responds to location updates
class MyLocationListener implements LocationListener {
   public void onLocationChanged(Location location) {
      // Called when a new location is found by the network location provider.
      currentLatitude  = (float)location.getLatitude();
      currentLongitude = (float)location.getLongitude();
      currentAccuracy  = (float)location.getAccuracy();
      currentProvider  = location.getProvider();
    }
    public void onProviderDisabled (String provider) { 
      currentProvider = "";
    }

    public void onProviderEnabled (String provider) { 
      currentProvider = provider;
    }

    public void onStatusChanged (String provider, int status, Bundle extras) {
      // Nothing yet...
    }
    
}
public void draw_arrow() {
  background(0);
  // Display current GPS data
  text("arrow here");
}

  public int sketchWidth() { return screenWidth; }
  public int sketchHeight() { return screenHeight; }
}
