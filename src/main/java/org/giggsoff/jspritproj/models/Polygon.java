/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author giggsoff
 */
public class Polygon implements Iterable<Point>{
    public List<Point> polygon; 
    
    public Polygon(){
        polygon = new ArrayList<>();
    }
    
    public void addPoint(Point pt){
        polygon.add(pt);
    }

    public void addPoint(double x, double y) {
        addPoint(new Point(x, y));
    }
    
    public int size(){
        return polygon.size();
    }
    
    public Point get(Integer i){
        return polygon.get(i);
    }

    @Override
    public Iterator iterator() {
        return polygon.iterator();
    }
}
