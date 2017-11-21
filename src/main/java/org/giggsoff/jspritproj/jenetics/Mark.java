/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.jenetics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.giggsoff.jspritproj.models.Polygon;
import org.giggsoff.jspritproj.models.Truck;
import org.jenetics.Genotype;

/**
 *
 * @author giggsoff
 */
public class Mark{
    private CostsInterface cInt;
    private SituationInterface sInt;  
    public Genotype<CustomGene> curGen;
    public Double time;
    public Integer processed;

    Mark(SituationInterface si, CostsInterface ci) {        
        cInt = ci;
        sInt = si;
    }
    
    public Double eval(Genotype<CustomGene> gt) {
        time = 0.;
        curGen = gt;
        processed = 0;
        Double sum = 0.;
        List<Boolean> lst = new ArrayList<>();
        for (int i = 0; i < StateObj.MaxBin; i++) {
            lst.add(false);
        }
        Map<Integer, List<Integer>> routes = new HashMap<>();
        for (CustomGene so : gt.getChromosome().toSeq()) {
            if (!routes.containsKey(so.getAllele().truck)) {
                routes.put(so.getAllele().truck, new ArrayList<>());
            }
            if (so.getAllele().obj >= 0) {
                routes.get(so.getAllele().truck).add(so.getAllele().obj);
            }
        }
        for (Integer truck : routes.keySet()) {
            if(routes.get(truck).isEmpty()){
                continue;
            }
            Truck ta =  cInt.getTruckAttrs(truck);
            Double vol = (double)ta.volume;
            List<Double> rc = cInt.getFirstRouteCosts(routes.get(truck).get(0),truck);
            sum += cInt.updateMaxRouteTruckCost(rc.get(0)*ta.ppk/1000+rc.get(1)*ta.pph/3600);
            time += rc.get(1);
            for (int i = 0; i < routes.get(truck).size() - 1; i++) {
                /*if(obj>0&&obj<StateObj.MaxBin)
                    sum++;*/
                rc = cInt.getRouteCosts(routes.get(truck).get(i),routes.get(truck).get(i+1));
                sum += cInt.updateMaxRouteTruckCost(rc.get(0)*ta.ppk/1000+rc.get(1)*ta.pph/3600);                
                time += rc.get(1);
            }
            for (int i = 0; i < routes.get(truck).size() - 1; i++) {
                if (routes.get(truck).get(i) > 0 && routes.get(truck).get(i) < StateObj.MaxBin) {
                    if (cInt.getBinAttrs(routes.get(truck).get(i)).containsKey(ta.type) && ta.max - vol > cInt.getBinAttrs(routes.get(truck).get(i)).get(ta.type) && lst.get(routes.get(truck).get(i)) == false) {
                        vol+=cInt.getBinAttrs(routes.get(truck).get(i)).get(ta.type);
                        lst.set(routes.get(truck).get(i), Boolean.TRUE);
                        processed++;
                    } else if (lst.get(routes.get(truck).get(i))) {
                        sum += cInt.getMaxRouteTruckCost() * 2;
                    } else if(!cInt.getBinAttrs(routes.get(truck).get(i)).containsKey(ta.type)){
                        sum += cInt.getMaxRouteTruckCost() * 2;                         
                    } else if(ta.max - vol <= cInt.getBinAttrs(routes.get(truck).get(i)).get(ta.type)){
                        sum += cInt.getMaxRouteTruckCost() * 2;                        
                    }
                } else if (routes.get(truck).get(i) >= StateObj.MaxBin) {
                    if(cInt.getDumpReprAttrs(routes.get(truck).get(i)).containsKey(ta.type)){
                        sum += vol*cInt.getDumpReprAttrs(routes.get(truck).get(i)).get(ta.type);
                        vol = 0.;
                    }
                }
            }
            if (vol > 0) {
                sum += Double.MAX_VALUE*0.05;
            }
        }

        for (int i = 0; i < StateObj.MaxBin; i++) {
            if (lst.get(i) == false) {
                sum += cInt.getMaxRouteTruckCost()*10;
            }
        }

        return sum;
    }
    
    public List<Polygon> getRoutes(){
        List<Polygon> lp = new ArrayList<>();        
        Map<Integer, List<Integer>> routes = new HashMap<>();
        for (CustomGene so : curGen.getChromosome().toSeq()) {
            if (!routes.containsKey(so.getAllele().truck)) {
                routes.put(so.getAllele().truck, new ArrayList<>());
            }
            if (so.getAllele().obj >= 0 && (routes.get(so.getAllele().truck).size()<2||routes.get(so.getAllele().truck).get(routes.get(so.getAllele().truck).size()-1)!=so.getAllele().obj)) {
                routes.get(so.getAllele().truck).add(so.getAllele().obj);
            }
        }
        for(Integer li:routes.keySet()){
            Polygon pl = new Polygon();
            pl.addPoint(sInt.getPointFirst(li));
            for(Integer pnum: routes.get(li)){
                pl.addPoint(sInt.getPoint(pnum));
            }            
            lp.add(pl);
        }
        return lp;
    }
}
