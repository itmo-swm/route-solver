/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.models;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author giggsoff
 */
public class Region {
    public String id;
    public Polygon polygon = new Polygon();
    public Region(JSONObject obj) throws JSONException, ParseException{
        id = obj.getString("id");
        JSONArray ja = obj.getJSONObject("geometry").getJSONArray("coordinates").getJSONArray(0);
        for(int i = 0;i<ja.length();i++){
            polygon.addPoint(new Point(ja.getJSONArray(i).getDouble(0),ja.getJSONArray(i).getDouble(1)));
        }
    }
    
    public static List<Region> fromArray(JSONArray ar) throws JSONException, ParseException{
        List<Region> temp = new ArrayList<>();
        for(int i=0;i<ar.length();i++){
            temp.add(new Region(ar.getJSONObject(i)));
        }
        return temp;
    }
}
