package org.giggsoff.jspritproj.utils;

import java.util.List;
import org.giggsoff.jspritproj.models.Point;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author giggsoff
 */
public class GeoJson {
    public static JSONObject getGeoJSON(List<Point> list){
        JSONObject featureCollection = new JSONObject();  
        featureCollection.put("type", "featureCollection");
        JSONArray featureList = new JSONArray();
        // iterate through your list
        for (Point obj : list) {
            // {"geometry": {"type": "Point", "coordinates": [-94.149, 36.33]}
            JSONObject point = new JSONObject();
            point.put("type", "Point");
            // construct a JSONArray from a string; can also use an array or list
            JSONArray coord = new JSONArray("["+obj.x+","+obj.y+"]");
            point.put("coordinates", coord);
            JSONObject feature = new JSONObject();
            feature.put("geometry", point);
            featureList.put(feature);
            featureCollection.put("features", featureList);
        }
        return featureCollection;
    }
}
