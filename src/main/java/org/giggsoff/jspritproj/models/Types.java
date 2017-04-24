/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author giggsoff
 */
public class Types {
    private List<String> gtypes;
    
    public Types(){
        gtypes = new ArrayList<>();
    }
    
    public void add(String type){
        if(gtypes.contains(type))
            return;
        gtypes.add(type);
    }
    
    public int get(String type){
        add(type);
        return gtypes.indexOf(type);
    }
    
    public List<String> all(){
        return gtypes;
    }
}
