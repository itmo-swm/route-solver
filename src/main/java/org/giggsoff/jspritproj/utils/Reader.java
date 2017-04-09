/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author giggsoff
 */
public class Reader {
    
    public static JSONObject readObject(String what) throws IOException{
        URL url = new URL("http://sdn.naulinux.ru:8128/Plone/swm_scripts/"+what);
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                yc.getInputStream()));
        String inputLine;
        String ret = "";

        while ((inputLine = in.readLine()) != null) 
            ret+=inputLine;
        JSONObject obj = new JSONObject(ret);
        in.close();
        return obj;
    }
    
    public static JSONArray readArray(String what) throws IOException{
        URL url = new URL("http://sdn.naulinux.ru:8128/Plone/swm_scripts/"+what);
        URLConnection yc = url.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                yc.getInputStream()));
        String inputLine;
        String ret = "";

        while ((inputLine = in.readLine()) != null) 
            ret+=inputLine;
        JSONArray obj = new JSONArray(ret);
        in.close();
        return obj;
    }
    
}
