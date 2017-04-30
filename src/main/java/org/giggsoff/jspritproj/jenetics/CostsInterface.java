/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.jenetics;

import java.util.List;
import java.util.Map;
import org.giggsoff.jspritproj.models.Truck;

/**
 *
 * @author giggsoff
 */
public interface CostsInterface {
    //0 - m, 1 - s
    List<Double> getRouteCosts(Integer obj1, Integer obj2); 
    
    //0 - m, 1 - s
    List<Double> getFirstRouteCosts(Integer obj, Integer tr); 
    
    //type : v 
    Map<String,Double> getBinAttrs(Integer obj);
    
    //type : cpv
    Map<String,Double> getDumpAttrs(Integer obj);
    
    Truck getTruckAttrs(Integer tr);
    
    //Max cost
    Double getMaxRouteTruckCost();
    
    Double updateMaxRouteTruckCost(Double val);
}
