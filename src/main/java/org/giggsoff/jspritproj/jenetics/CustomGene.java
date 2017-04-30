/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.jenetics;

import org.jenetics.Gene;
import org.jenetics.util.ISeq;
import org.jenetics.util.MSeq;

/**
 *
 * @author giggsoff
 */
public class CustomGene implements Gene<StateObj, CustomGene> {
    
    private StateObj obj;
    
    private CustomGene(StateObj _obj){
        obj=_obj;
    }

    public static CustomGene of(StateObj _obj) {
        return new CustomGene(_obj);
    }
    

    public static ISeq<CustomGene> seq(int length) {
        return MSeq.<CustomGene>ofLength(length).fill(() ->
                new CustomGene(StateObj.RandTruck())
        ).toISeq();
    }


    @Override
    public StateObj getAllele() {
        return obj;
    }

    @Override
    public CustomGene newInstance() {
        return new CustomGene(StateObj.Rand());
    }

    @Override
    public CustomGene newInstance(StateObj value) {
        return new CustomGene(value);
    }

    @Override
    public boolean isValid() {
        return true;
    }
    
}
