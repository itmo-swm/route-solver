/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author giggsoff
 */
public class Truck {
    public String id;
    public double lat;
    public double lng;
    public int priority;
    public Truck(JSONObject obj) throws JSONException{
        id = obj.getString("id");
        lat = obj.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0);
        lng = obj.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1);
        priority = Integer.parseInt(obj.getString("priority"));
    }
}
