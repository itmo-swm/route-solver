/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.models;

import java.util.Map;

/**
 *
 * @author giggsoff
 */
public class DumpRepr extends Point{
    public String id;
    public Point coord;
    public int state;
    public Double price;
    public Map<String,Double> prices;    
}
