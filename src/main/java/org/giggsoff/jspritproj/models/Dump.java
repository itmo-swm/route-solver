/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.models;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.giggsoff.jspritproj.utils.Reader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author giggsoff
 */
public class Dump extends Point{
    public String id;
    public Point coord;
    public int state;
    public Double price;
    public Map<String,Double> prices;
    public Dump(JSONObject obj) throws JSONException, ParseException, IOException{
        id = obj.getString("id");
        coord = Reader.readGeoJSONPoint(obj.getString("geometry"));
        //price = obj.getDouble("price_for_waste_disposal");
        //state = Integer.parseInt(obj.getString("state"));
    }
    
    public static List<Dump> fromArray(JSONArray ar) throws JSONException, ParseException, IOException{
        List<Dump> temp = new ArrayList<>();
        for(int i=0;i<ar.length();i++){
            temp.add(new Dump(ar.getJSONObject(i)));
        }
        return temp;
    }
    
    @Override
    public Point getPoint(){
        return coord;
    }
}
