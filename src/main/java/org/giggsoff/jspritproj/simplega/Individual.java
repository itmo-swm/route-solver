package org.giggsoff.jspritproj.simplega;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.giggsoff.jspritproj.Main;
import org.giggsoff.jspritproj.jenetics.StateObj;

public class Individual {

    static int defaultGeneLength = 64;
    private List<StateObj> genes = new ArrayList<>();
    // Cache
    private Double fitness = Double.MAX_VALUE;
    public static Double wrong = 1.;
    
    public Individual(){
        for (int i = 0; i < defaultGeneLength; i++) {
            genes.add(new StateObj());
        }        
    }
    
    public Individual(int size){
        for (int i = 0; i < size; i++) {
            genes.add(new StateObj());
        }        
        setDefaultGeneLength(size);
    }

    // Create a random individual
    public void generateIndividual() {
        List<Integer> li = new ArrayList<>();        
        for (int i = 0; i < StateObj.MaxBin; i++) {
            li.add(i);
        }     
        Collections.shuffle(li);
        Integer curObj = 0;
        Random random = new Random();
        for (int i = 0; i < size(); i++) {
            StateObj so = new StateObj();
            so.obj = curObj;
            if(curObj>=0)
                curObj++;
            if(curObj>StateObj.MaxBin){
                curObj = -1;
            }
            if(curObj<0){
                if(Math.random() <= 0.4){
                    so.obj = random.nextInt(StateObj.MaxDel)+StateObj.MaxBin;
                }                
            }
            if(so.obj>=0 && so.obj<StateObj.MaxBin){
                so.obj=li.get(so.obj);
                so.truck = Main.typesTrucks.get(Main.sgbList.get(so.obj).type).get(random.nextInt(Main.typesTrucks.get(Main.sgbList.get(so.obj).type).size()));
            }else
                so.truck = random.nextInt(StateObj.MaxTruck);
            genes.set(i, so);
        }
    }

    /* Getters and setters */
    // Use this if you want to create individuals with different gene lengths
    public static void setDefaultGeneLength(int length) {
        defaultGeneLength = length;
    }
    
    public StateObj getGene(int index) {
        StateObj so = new StateObj();
        so.obj = genes.get(index).obj;
        so.truck = genes.get(index).truck;        
        return so;
    }

    public void setGene(int index, StateObj value) {
        genes.set(index, value);
        fitness = 0.;
    }

    /* Public methods */
    public int size() {
        return genes.size();
    }

    public Double getFitness() {
        //if (fitness == 0.) {
            fitness = FitnessCalc.getFitness(this);
        //}
        return fitness;
    }
    public boolean isValid() {
        boolean result = true;
        wrong = 1.;
        List<Boolean> lst = new ArrayList<>();
        for (int i = 0; i < StateObj.MaxBin; i++) {
            lst.add(false);
        }
        for (int i = 0; i < defaultGeneLength; i++) {
            if (genes.get(i).obj >= 0 && genes.get(i).obj < StateObj.MaxBin) {
                if(lst.get(genes.get(i).obj)==true){
                    result= false;
                    wrong = wrong * 2;
                }
                lst.set(genes.get(i).obj, true);
                if(!Main.trList.get(genes.get(i).truck).type.equals(Main.sgbList.get(genes.get(i).obj).type)){
                    result= false;
                    wrong = wrong * 2;
                }
            }
        }
        for (int i = 0; i < StateObj.MaxBin; i++) {
            if (lst.get(i) == false) {
                result= false;
                wrong = wrong * 2;
            }
        }
        /*for (int i = 0; i < length-1; i++) {
            for (int j=i+1;j<length;j++){
                if(iSeq.get(i).getAllele().obj<StateObj.MaxBin&&iSeq.get(i).getAllele().obj>0&&Objects.equals(iSeq.get(i).getAllele().obj, iSeq.get(j).getAllele().obj))
                    return false;
            }
        }*/
        if(result){
            return true;
        }else
            return false;
    }

    @Override
    public String toString() {
        String geneString = "";
        for (int i = 0; i < size(); i++) {
            geneString += getGene(i);
        }
        return geneString;
    }
}