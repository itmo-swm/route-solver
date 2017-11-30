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
import java.util.Date;
import java.util.List;
import org.giggsoff.jspritproj.utils.Reader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author giggsoff
 */
public class SGB extends Point{
    public String id;
    public Point coord;
    public Integer state;
    public Date cleaning_time;
    public Double volume;
    public Double max;
    public String type;
    public Double humidity;
    public Double temperature;
    public SGB(JSONObject obj) throws JSONException, ParseException, IOException{
        id = obj.getString("id");
        coord = Reader.readGeoJSONPoint(obj.getString("geometry"),2,id);
        state = (int)Double.parseDouble(obj.getString("state"));
        volume = Double.parseDouble(obj.getString("volume"));
        max = Double.parseDouble(obj.getString("max"));
        humidity = Double.parseDouble(obj.getString("humidity"));
        temperature = Double.parseDouble(obj.getString("temperature"));
        type = obj.getString("type");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cleaning_time = dateFormat.parse(obj.getString("cleaning_time"));
    }
    
    public static List<SGB> fromArray(JSONArray ar) throws JSONException, ParseException, IOException{
        List<SGB> temp = new ArrayList<>();
        for(int i=0;i<ar.length();i++){
            SGB cursgb = new SGB(ar.getJSONObject(i));
            if(cursgb.state>0&&cursgb.volume>0.5*cursgb.max)
                temp.add(cursgb);
        }
        return temp;
    }
    
    public String getURLSGB(){
        SGB cur = this;
        String st = cur.id;
        st+="/set_sgb?volume=";
        st+=cur.volume;
        st+="&state=";
        st+=cur.state;
        st+="&cleaning_time=";        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy%20HH:mm");
        st+=dateFormat.format(cur.cleaning_time);
        st+="&humidity=";
        st+=cur.humidity;
        st+="&temperature=";
        st+=cur.temperature;
        st+="&CO2=";
        st+=1;
        return st;
    }
    
    public static SGB findSGB(List<SGB> sgbl, String _id){
        for(int i=0;i<sgbl.size();i++){
            if(sgbl.get(i).id.equals(_id)){
                return sgbl.get(i);
            }
        }
        return null;
    }
    
    @Override
    public Point getPoint(){
        return coord;
    }
}
