package org.giggsoff.jspritproj.utils;

import java.util.List;
import org.giggsoff.jspritproj.models.Point;
import org.giggsoff.jspritproj.models.Polygon;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author giggsoff
 */
public class GeoJson {

    public static JSONObject getGeoJSON(List<Polygon> list) {
        JSONObject featureCollection = new JSONObject();
        featureCollection.put("type", "FeatureCollection");
        JSONArray featureList = new JSONArray();
        // iterate through your list
        for (Polygon pl : list) {
            JSONObject point = new JSONObject();
            point.put("type", "Feature");
            JSONArray coords = new JSONArray();
            for (Point obj : pl) {
                // {"geometry": {"type": "Point", "coordinates": [-94.149, 36.33]}
                // construct a JSONArray from a string; can also use an array or list
                JSONArray coord = new JSONArray("[" + obj.x + "," + obj.y + "]");
                coords.put(coord);
            }
            point.put("geometry", new JSONObject().put("type", "LineString").put("coordinates", coords));
            point.put("properties", new JSONObject());
            featureList.put(point);
        }
        featureCollection.put("features", featureList);
        return featureCollection;
    }
}
