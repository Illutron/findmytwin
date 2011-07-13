// Imports

import android.provider.Settings;
import android.view.WindowManager;
import android.os.PowerManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.location.GpsStatus.Listener;
import android.location.GpsStatus.NmeaListener; // not needed yet, but going for nmea data next!
import android.os.Bundle;
import apwidgets.*;

import android.provider.Settings.Secure;
String m4id = "f36e55e699280d12";
String m8id = "8171f474f928ec9a";
String id = "";

float CLOSE_THRESHOLD = 120;
float oldTwinLatitude = -1;

boolean debug = false;

String TWINM8BADGEURL = "http://www.google.com/latitude/apps/badge/api?user=3456359256764633866&type=json";
String TWINM4BADGEURL = "http://www.google.com/latitude/apps/badge/api?user=-3847234630334711426&type=json";
// Johans badge is hardcoded for testing and demo
String twinBadgeUrl = "http://www.google.com/latitude/apps/badge/api?user=-4263365567075812187&type=json";


CompassManager compass;
float compassDirection;
float direction;
float currentBearingTo;

boolean wrongDirectionFlag = false;
boolean rightDirectionFlag = false;

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
float lastDistance = -1;

void setup() {
  frameRate(15);

  PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
  PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "My Tag");
  wl.acquire();

  String android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);

  if (android_id.contentEquals(m4id)) {
    //twinBadgeUrl = TWINM8BADGEURL;
    id = "Illutron.M4 " + android_id;
  } 
  else if (android_id.contentEquals(m8id)) {
    id = "Illutron.M8 " + android_id;
    //twinBadgeUrl = TWINM4BADGEURL;
  } 
  else {
    print("Phone not recognized. You need to configure the phones ANDROID_ID in the source code.");
  }

  Settings.System.putInt(this.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);

  orientation(PORTRAIT);
  size(screenWidth, screenHeight);

  player = new APMediaPlayer(this);
  player.setLooping(false);
  player.setVolume(1.0, 1.0);

  compass = new CompassManager(this);
  accel = new AccelerometerManager(this);

  fontList = PFont.list();
  androidFont = createFont(fontList[5], 35, true);
  textFont(androidFont);

  twinLocation = new Location("latitude badge");
  update_twin_location(twinLocation);
}

void draw() {
  try {
  background(46, 52, 54);

  if (debug) {
    debugDraw();
  } 
  else {

    textAlign(CENTER);
    if (twinLatitude == -1 ) {
      text("I don't know where to go.", width/2, 340);
    } 

    if (currentLatitude == -1) {
      text("I don't know where I am.", width/2, 375);
    }

    if (currentLatitude != -1 && twinLatitude != -1) { 
      // moving pattern
      if (acc_change > 0.5)
      {
        lastMoved = millis();
        if ( state == 0)
        {
          state = 1;
          lastPLayed = millis() + 12000 + (int)random(20000);
          player.setMediaFile("thankyou01.wav");
          player.start();
        }
      }
      if (state == 1 &&  (millis() - lastMoved) > 50000)
      {
        state = 0;
      }
      //idle state
      if (state == 0  )
      {
        if (lastPLayed < millis())
        {
          lastPLayed = millis() + 12000 + (int)random(20000);
          int r = round(random(1, 5)); 
          player.setMediaFile("pleasepickmeup0"+r+".wav");
          player.start();
        }
      }
      // moving state
      if (state == 1)
      {
        if (lastPLayed < millis())
        {
          if (wrongDirectionFlag == true) { 
            player.setMediaFile("wrongdirection01.wav");
            wrongDirectionFlag = false;          
          } 
          else if (rightDirectionFlag == true) {
            int r = round(random(1, 5)); 
            player.setMediaFile("rightdirection0"+r+".wav");
            rightDirectionFlag = false;
          } else {
            int r = round(random(1, 2));  
            player.setMediaFile("helpmefindmytwin0"+r+".wav");
          }
          lastPLayed = millis() + 12000 + (int)random(40000);
          player.start();
        }
      }

      if (distance < CLOSE_THRESHOLD) {
        if (mapImg != null) {
          pushMatrix();
          translate(0, 0);
          image(mapImg, 0, 0, 480, 854);
          popMatrix();
        }
        
        fill(46, 52, 54);
        pushMatrix();
        translate(0, 0);
        rect(0, 0, 480, 340);
        popMatrix();
        
        fill(252, 233, 79); 
        text("We are close!", width/2, 300);
        
        fill(46, 52, 54);
        pushMatrix();
        translate(0, 0);
        rect(0, 654, 480, 240);
        popMatrix();
        
      }

      fill(252, 233, 79);  

      if (distance < CLOSE_THRESHOLD) {
        pushMatrix();   
        translate((width/2), (height/2));
        scale(2);
        ellipse(10, 10, 10, 10);
        popMatrix();
        
        
        fill(252, 233, 79);
        pushMatrix();
        translate(width/2, height/8*5);
        scale(0.5);
        rotate(radians(direction));
        beginShape();
        vertex(0, -50);
        vertex(-20, 60);
        vertex(0, 50);
        vertex(20, 60);
        endShape(CLOSE);
        popMatrix();
        
      }
      
      else {
        
        fill(252, 233, 79);
        pushMatrix();      
        translate(width/2, height/2);
        scale(1.5);
        rotate(radians(direction));
        beginShape();
        vertex(0, -50);
        vertex(-20, 60);
        vertex(0, 50);
        vertex(20, 60);
        endShape(CLOSE);
        popMatrix();
      }
    }
  }
  } catch (Exception e) {
    e.printStackTrace();
  }
}

class MyLocationListener implements LocationListener {

  void onLocationChanged(Location location) {
    // Called when a new location is found by the network location provider.     

    currentLatitude = (float)location.getLatitude();
    currentLongitude = (float)location.getLongitude();
    currentAccuracy = (float)location.getAccuracy();
    currentBearing = (float)location.getBearing();

    // update twin location no more than once every minute
    if (millis() - lastTwinLocUpdate > 60000 || twinLatitude == -1 ) {
        update_twin_location(twinLocation);
  
        if (distance < CLOSE_THRESHOLD && oldTwinLatitude != twinLatitude) {
          try {
            mapImg = loadImage("http://maps.google.com/maps/api/staticmap?center="+twinLatitude+","+twinLongitude+"&zoom=+"+mapZoom+"&size=480x854&sensor=false", "jpg");
            mapCenterLat = currentLatitude;
            mapCenterLon = currentLongitude;
          } catch (Exception e) {
            e.printStackTrace();
          }
  
        oldTwinLatitude = twinLatitude;
      }
    }

    try {
      if (twinLocation != null && location != null) {

        distance = (float)location.distanceTo(twinLocation);
        currentBearingTo = (float)location.bearingTo(twinLocation);

        if (distance < lastDistance) {
          rightDirectionFlag = true;
          wrongDirectionFlag = false;
        } 
        else if (distance > lastDistance) {
          wrongDirectionFlag = true;
          rightDirectionFlag = false;
        }

        lastDistance = distance;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  void onProviderDisabled (String provider) {
  }  
  void onProviderEnabled (String provider) {
  }
  void onStatusChanged (String provider, int status, Bundle extras) {
  }
}

public void directionEvent(float newDirection) {
  compassDirection = degrees(newDirection);
  direction = compassDirection + currentBearingTo;
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

void debugDraw() {
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

void keyPressed() {
  if (key == CODED) {
    if (keyCode == RIGHT) {
      if (debug) { 
        debug = false;
      } 
      else {
        debug = true;
      }
    }
  }
}

