package processing.android.test.mystbox2;

import processing.core.*; 
import processing.xml.*; 

import android.provider.Settings; 
import android.view.WindowManager; 
import android.os.PowerManager; 
import android.content.Context; 
import android.location.Location; 
import android.location.LocationManager; 
import android.location.LocationListener; 
import android.location.GpsStatus.Listener; 
import android.location.GpsStatus.NmeaListener; 
import android.os.Bundle; 
import apwidgets.*; 
import android.provider.Settings.Secure; 
import java.io.BufferedReader; 
import java.io.InputStreamReader; 
import java.net.URL; 
import java.net.URLConnection; 
import org.json.JSONArray; 
import org.json.JSONObject; 

import apwidgets.*; 

import android.view.MotionEvent; 
import android.view.KeyEvent; 
import android.graphics.Bitmap; 
import java.io.*; 
import java.util.*; 

public class mystbox2 extends PApplet {

// Imports








 // not needed yet, but going for nmea data next!




String m4id = "f36e55e699280d12";
String m8id = "8171f474f928ec9a";
String id = "";

float CLOSE_THRESHOLD = 300;

boolean debug = false;

String TWINM8BADGEURL = "http://www.google.com/latitude/apps/badge/api?user=3456359256764633866&type=json";
String TWINM4BADGEURL = "http://www.google.com/latitude/apps/badge/api?user=-3847234630334711426&type=json";
String twinBadgeUrl = "";

CompassManager compass;
float compassDirection;
float direction;
float currentBearingTo;

// sound
APMediaPlayer player;

// Display
String[] fontList;
PFont androidFont;

ArrayList balls;
AccelerometerManager accel;
float ax, ay, az;
float acc_amount;
float acc_change;

int state = 0; // 0 = nothing, 1 = picked up
long lastPLayed = 0;
long lastMoved = 0;

int mapZoom = 16;
float mapCenterLat = -1;
float mapCenterLon = -1;

PImage mapImg;

LocationManager locationManager;
MyLocationListener locationListener;

// Variables to hold the current GPS data
Location twinLocation;
float twinLatitude = -1;
float twinLongitude = -1;
long lastTwinLocUpdate = 0;

float currentLatitude = -1;
float currentLongitude = -1;
float currentAccuracy = -1;
float currentBearing= -1;

float distance = -1;

public void setup() {
  frameRate(15);
  
  PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
  PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "My Tag");
  wl.acquire();
  
  String android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
  
  if (android_id.contentEquals(m4id)) {
     twinBadgeUrl = TWINM8BADGEURL;
     id = "Illutron.M4 " + android_id;
  } else if (android_id.contentEquals(m8id)) {
     id = "Illutron.M8 " + android_id;
     twinBadgeUrl = TWINM4BADGEURL;
  } else {
    print("Phone not recognized. You need to configure the phones ANDROID_ID in the source code.");
  }
    
  Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
  
  orientation(PORTRAIT);
 
  
  player = new APMediaPlayer(this);
  player.setLooping(false);
  player.setVolume(1.0f, 1.0f);
  
  compass = new CompassManager(this);
  accel = new AccelerometerManager(this);
  
  fontList = PFont.list();
  androidFont = createFont(fontList[5], 35, true);
  textFont(androidFont);
  
  twinLocation = new Location("latitude badge");
}

public void draw() {
  
  // moving pattern
  if (acc_change > 0.5f)
  {
    lastMoved = millis();
    if ( state == 0)
    {
      state = 1;
        lastPLayed = millis() + 5000 + (int)random(20000) ; 
        player.setMediaFile("thankyou01.wav");
        player.start();
    } 
  }
    if (state == 1 &&  (millis() - lastMoved) > 50000)
    {
      state = 0;
    }
  // idle state
  if (state == 0  )
  {
    if (lastPLayed < millis())
    {
      lastPLayed = millis() + 5000 + (int)random(20000);
      player.setMediaFile("pleasepickmeup01.wav");
      player.start();
    }
  }
  // moving state
  if (state == 1)
  {
    if (lastPLayed < millis())
    {
      lastPLayed = millis() + 10000 + (int)random(40000) ; 
      player.setMediaFile("helpmefindmytwin01.wav");
      player.start();
    }
  }
  
  background(210);
  if (debug) {
    debugDraw();
  } else {
    
   textAlign(CENTER);
   if (twinLatitude == -1) {
     text("I don't know where to go.", width/2, 40);
   } 
   
   if (currentLatitude == -1) {
     text("I don't know where I am.", width/2, 75);
   }
    
    fill(192, 0, 0);
    noStroke();
    
    if (distance < CLOSE_THRESHOLD) {
      if (mapImg != null) {
        pushMatrix();
        translate(0, 0);
        image(mapImg, 0, 0, 480, 854);
        popMatrix();
      }
    }
    
    pushMatrix();   
    fill(180,40,40);
    translate(width/5*4, height/6*5);
    scale(1);
    rotate(radians(compassDirection));
    beginShape();
    vertex(0, -50);
    vertex(-20, 60);
    vertex(0, 50);
    vertex(20, 60);
    endShape(CLOSE);
    popMatrix(); 
    
    pushMatrix();
    fill(40,180,40);
    translate(width/5*1, height/6*5);
    scale(1);
    rotate(radians(currentBearing));
    beginShape();
    vertex(0, -50);
    vertex(-20, 60);
    vertex(0, 50);
    vertex(20, 60);
    endShape(CLOSE);
    popMatrix(); 
    
    fill(10,5,5);
    pushMatrix();
    translate(width/2, height/2);
    
    if (distance < 1000) {
      scale(2);
      ellipse(10,10,10,10);
    } else {
      scale(2);
      rotate(radians(direction));
      beginShape();
      vertex(0, -50);
      vertex(-20, 60);
      vertex(0, 50);
      vertex(20, 60);
      endShape(CLOSE);
    }
    
    popMatrix();
    
  }
}

class MyLocationListener implements LocationListener {
   public void onLocationChanged(Location location) {
      // Called when a new location is found by the network location provider.     
      
      currentLatitude = (float)location.getLatitude();
      currentLongitude = (float)location.getLongitude();
      currentAccuracy = (float)location.getAccuracy();
      currentBearing = (float)location.getBearing();
      
      // update twin location no more than once every minute
      if (millis() - lastTwinLocUpdate > 60000) {
          update_twin_location(twinLocation);
          
          mapImg = loadImage("http://maps.google.com/maps/api/staticmap?center="+twinLatitude+","+twinLongitude+"&zoom=+"+mapZoom+"&size=480x854&sensor=false", "jpg");
          mapCenterLat = currentLatitude;
          mapCenterLon = currentLongitude;
      }
      
      try {
        distance = (float)location.distanceTo(twinLocation);
        currentBearingTo = (float)location.bearingTo(twinLocation);    
        direction = currentBearing + currentBearingTo;
        
      } catch (Exception e) {
        e.printStackTrace();
      }
      
      
      
    }
    public void onProviderDisabled (String provider) {}  
    public void onProviderEnabled (String provider) {}
    public void onStatusChanged (String provider, int status, Bundle extras) {}   
}

public void directionEvent(float newDirection) {
  compassDirection = degrees(newDirection);
}

public void accelerationEvent(float x, float y, float z) {
  //println("acceleration: " + x + ", " + y + ", " + z);
  ax = x;
  ay = y;
  az = z;
  
 float tmp = sqrt(ax * ax + az* az + ay*ay);
 acc_change = abs(acc_amount - az);
 acc_amount = tmp;
}


public void resume() {
  super.resume();
  // Build Listener
  locationListener = new MyLocationListener();
  // Acquire a reference to the system Location Manager
  locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
  // Register the listener with the Location Manager to receive location updates
  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); 
  
  if (compass != null) compass.resume();
  if (accel != null) accel.resume();
}

public void pause() {
  super.pause();
  if (compass != null) compass.pause();
  if (accel != null) accel.pause();
}

//The MediaPlayer must be released when the app closes
public void destroy() {
  super.destroy(); //call onDestroy on super class
  if (player!=null) { //must be checked because or else crash when return from landscape mode
    player.release(); //release the player
  }
}

public void debugDraw() {
     //Display current GPS data
     textAlign(LEFT);
     text("lat: " + currentLatitude, 20, 40);
     text("lng: " + currentLongitude, 20, 75);
     text("accu: " + currentAccuracy, 20, 110);  
     text("Twin lat: " + twinLatitude, 20, 145);
     text("Twin lng: " + twinLongitude, 20, 180);     
     text("compdir: " + compassDirection, 20, 215); 
     text("bearingto: " + currentBearingTo, 20, 250);
     text("dir: " + direction, 20, 285);
     text("distto: " + distance, 20, 320);
     text("ax: " + ax, 20, 355);
     text("ay: " + ay, 20, 390);
     text("az: " + az, 20, 425);   
     text("lastmoved: " + lastMoved, 20, 460);
     text("accforce: " + acc_amount, 20, 495);
     text("id: " + id, 20, 530); 
}

public void keyPressed() {
  if (key == CODED) {
    if (keyCode == RIGHT) {
        if (debug) { 
          debug = false; 
        } else {
         debug = true; 
        }
    }
  }
}
// Simple bouncing ball class

class Ball {
  
  float x;
  float y;
  float speed;
  float gravity;
  float w;
  float life = 255;
  
  Ball(float tempX, float tempY, float tempW) {
    x = tempX;
    y = tempY;
    w = tempW;
    speed = 0;
    gravity = 0.1f;
  }
  
    public void move() {
    // Add gravity to speed
    speed = speed + gravity;
    // Add speed to y location
    y = y + speed;
    // If square reaches the bottom
    // Reverse speed
    if (y > height) {
      // Dampening
      speed = speed * -0.8f;
      y = height;
    }
  }
  
  public boolean finished() {
    // Balls fade out
    life--;
    if (life < 0) {
      return true;
    } else {
      return false;
    }
  }
  
  public void display() {
    // Display the circle
    fill(0,255);
    //stroke(0,life);
    ellipse(x,y,w,w);
  }
}  








URLConnection connection;

public void update_twin_location(Location twin) {
  try {
    connection = new URL(twinBadgeUrl).openConnection();
   
    BufferedReader r = new BufferedReader(new InputStreamReader( connection.getInputStream() ));
    StringBuilder total = new StringBuilder();
    String line;
    
    while ((line = r.readLine()) != null) {
        total.append(line);
    }
    
    JSONObject badgeObject = new JSONObject(total.toString());
    JSONArray featureArray = badgeObject.getJSONArray("features");    
    JSONObject geometry = new JSONObject(featureArray.getJSONObject(0).getString("geometry").toString());    
    JSONArray coordinates = geometry.getJSONArray("coordinates");
   
    twinLatitude = PApplet.parseFloat(coordinates.get(1).toString());
    twinLongitude = PApplet.parseFloat(coordinates.get(0).toString());
    
    twin.setLatitude(twinLatitude);
    twin.setLongitude(twinLongitude);
    
    lastTwinLocUpdate = millis();
    print("Location updated succesfully.");
    
      
  } catch (Exception e) {
    print("Error loading new location from latitude. Error was:");
    e.printStackTrace();
  }  
}



  public int sketchWidth() { return screenWidth; }
  public int sketchHeight() { return screenHeight; }
}
