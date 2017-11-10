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
import java.util.HashMap;
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
public class Processing extends DumpRepr{
    public Processing(JSONObject obj) throws JSONException, ParseException, IOException{
        prices = new HashMap<>();
        id = obj.getString("id");
        coord = Reader.readGeoJSONPoint(obj.getString("geometry"),4,id);
        if(obj.has("prices")){
            JSONObject jo = obj.getJSONObject("prices");
            if(jo.has("glass")){
                prices.put("glass", -Double.parseDouble(jo.getString("glass")));
            }
            if(jo.has("organic")){
                prices.put("organic", -Double.parseDouble(jo.getString("organic")));
            }
            if(jo.has("dangerous")){
                prices.put("dangerous", -Double.parseDouble(jo.getString("dangerous")));
            }
        }
        //price = obj.getDouble("price_for_waste_disposal");
        //state = Integer.parseInt(obj.getString("state"));
    }
    
    public static List<Processing> fromArray(JSONArray ar) throws JSONException, ParseException, IOException{
        List<Processing> temp = new ArrayList<>();
        for(int i=0;i<ar.length();i++){
            temp.add(new Processing(ar.getJSONObject(i)));
        }
        return temp;
    }
    
    @Override
    public Point getPoint(){
        return coord;
    }
}
