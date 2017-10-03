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
public class Truck extends Point{
    public String id;
    public Point coord;
    public int priority;
    public Double pph;
    public Double ppk;
    public Integer volume;
    public Integer max;
    public String type;
    public Truck(JSONObject obj) throws JSONException, IOException{
        id = obj.getString("id");
        coord = Reader.readGeoJSONPoint(obj.getString("geometry"),1,id);
        priority = Integer.parseInt(obj.getString("priority"));
        pph = Double.parseDouble(obj.getString("price_per_hour"));
        ppk = Double.parseDouble(obj.getString("price_per_kilometer"));
        volume = Integer.parseInt(obj.getString("volume"));
        max = Integer.parseInt(obj.getString("max"));
        type = obj.getString("type");
    }
    
    public static List<Truck> fromArray(JSONArray ar) throws JSONException, ParseException, IOException{
        List<Truck> temp = new ArrayList<>();
        for(int i=0;i<ar.length();i++){
            temp.add(new Truck(ar.getJSONObject(i)));
        }
        return temp;
    }
    @Override
    public Point getPoint(){
        return coord;
    }
}
