/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.models;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.giggsoff.jspritproj.utils.Reader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author giggsoff
 */
public class Region {
    public String id;
    public Polygon polygon;
    public Region(JSONObject obj) throws JSONException, ParseException, IOException{
        id = obj.getString("id");
        polygon = Reader.readGeoJSONPolygon(obj.getString("geometry"));
    }
    
    public static List<Region> fromArray(JSONArray ar) throws JSONException, ParseException, IOException{
        List<Region> temp = new ArrayList<>();
        for(int i=0;i<ar.length();i++){
            temp.add(new Region(ar.getJSONObject(i)));
        }
        return temp;
    }
}
