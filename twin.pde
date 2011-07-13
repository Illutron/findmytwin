import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

URLConnection connection;

void update_twin_location(Location twin) {
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
   
    twinLatitude = float(coordinates.get(1).toString());
    twinLongitude = float(coordinates.get(0).toString());
    
    twin.setLatitude(twinLatitude);
    twin.setLongitude(twinLongitude);
    
    lastTwinLocUpdate = millis();
    print("Location updated succesfully.");
    
      
  } catch (Exception e) {
    print("Error loading new location from latitude. Error was:");
    e.printStackTrace();
  }  
}


