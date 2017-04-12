/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.models;

import com.graphhopper.jsprit.core.util.Coordinate;

/**
 *
 * @author giggsoff
 */
public class Point {
    public Double x;    
    public Double y;    
    
    public Point(Double _x, Double _y){
        x = _x;
        y = _y;
    }
    
    public Point(Coordinate coord){
        x = coord.getX();
        y = coord.getY();
    }
    
    @Override
    public String toString() { 
        return x+";"+y;
    } 
}
