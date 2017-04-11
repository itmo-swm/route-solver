/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author giggsoff
 */
public class Dump {
    public String id;
    public double lat;
    public double lng;
    public int state;
    public Dump(JSONObject obj) throws JSONException, ParseException{
        id = obj.getString("id");
        lat = obj.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);
        lng = obj.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);
        //state = Integer.parseInt(obj.getString("state"));
    }
    
    public static List<Dump> fromArray(JSONArray ar) throws JSONException, ParseException{
        List<Dump> temp = new ArrayList<>();
        for(int i=0;i<ar.length();i++){
            temp.add(new Dump(ar.getJSONObject(i)));
        }
        return temp;
    }
}
