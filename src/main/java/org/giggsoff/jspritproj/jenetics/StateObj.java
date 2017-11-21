/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.jenetics;

import java.util.Random;
import org.jenetics.util.RandomRegistry;

/**
 *
 * @author giggsoff
 */
public class StateObj {
    public Integer truck;
    public static Integer MaxTruck = 3;
    public Integer obj;
    public static Integer MaxBin = 15;
    public static Integer MaxDel = 5;
    public static StateObj Rand(){        
        final Random random = RandomRegistry.getRandom();
        StateObj so = new StateObj();
        int nv = random.nextInt(10);
        if(nv==1){
            so.obj=-1;
        }else if(nv<5){
            so.obj = random.nextInt(StateObj.MaxBin+StateObj.MaxDel);
        }else{
            so.obj = random.nextInt(StateObj.MaxBin);            
        }
        so.truck = random.nextInt(StateObj.MaxTruck);
        return so;
    }
    public static Integer curObj = 0;
    public static StateObj RandTruck(){
        final Random random = RandomRegistry.getRandom();
        StateObj so = new StateObj();
        so.obj = curObj;
        so.truck = random.nextInt(StateObj.MaxTruck);
        curObj++;
        if(curObj>MaxBin+MaxDel){
            curObj = 0;
        }
        return so;       
    }
    
    @Override
    public String toString(){
        return "<t:"+truck+"|o:"+obj+">";
    }
}
