/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.models;

import com.graphhopper.jsprit.core.util.Coordinate;
import java.util.Date;

/**
 *
 * @author giggsoff
 */
public class Point {
    public Double x;    
    public Double y;    
    public int type;
    public String id;
    public Date dt = null;
    
    public Point(){
        x = 0.;
        y = 0.;
        type = 0;
        id = "";
    }
    
    public Point(Double _x, Double _y, int _type, String _id){
        x = _x;
        y = _y;
        type = _type;
        id = _id;
    }
    
    public Point(Double _x, Double _y, int _type, String _id, Date _dt){
        x = _x;
        y = _y;
        type = _type;
        id = _id;
        dt = new Date(_dt.getTime());
    }
    
    public Point(Coordinate coord, int _type, String _id){
        x = coord.getX();
        y = coord.getY();
        type = _type;
        id = _id;
    }
    
    public Point getPoint(){
        return this;
    }
    
    @Override
    public String toString() { 
        return x+";"+y;
    } 
}
