/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.jenetics;

import java.util.function.Predicate;
import org.giggsoff.jspritproj.Main;
import org.giggsoff.jspritproj.simplega.Algorithm;
import org.giggsoff.jspritproj.simplega.Individual;
import org.giggsoff.jspritproj.simplega.Population;

/**
 *
 * @author giggsoff
 */
public class Evaluator {  
    
    public static Algorithm EvaluateMy(SituationInterface si, CostsInterface ci) {
        StateObj.MaxBin = si.getSGBs();
        StateObj.MaxTruck = si.getTrucks();
        StateObj.MaxDel = si.getDumpReprs();
        Main.minCost = Double.MAX_VALUE;
        Algorithm alg = new Algorithm(si,ci);
        Individual.setDefaultGeneLength((si.getSGBs()+si.getDumpReprs())*2);
        Population myPop = new Population(si.getTrucks()*5, true);
        
        // Evolve our population until we reach an optimum solution
        int generationCount = 0;
        while (generationCount < (si.getSGBs()+si.getDumpReprs()+si.getTrucks())*10) {
            generationCount++;
            System.out.println("Generation: " + generationCount + " Fittest: " + myPop.getFittest().getFitness());
            myPop = alg.evolvePopulation(myPop);
        }
        for(int i = 0;i<myPop.getFittest().size();i++){
            System.out.print(myPop.getFittest().getGene(i) + " ");
        };     
        return alg;
    }
}
