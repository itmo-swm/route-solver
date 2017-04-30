/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.jenetics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.jenetics.Chromosome;
import org.jenetics.util.ISeq;

/**
 *
 * @author giggsoff
 */
public class CustomChromosome implements Chromosome<CustomGene> {

    private ISeq<CustomGene> iSeq;
    private final int length;

    private CustomChromosome(ISeq<CustomGene> genes) {
        this.iSeq = genes;
        this.length = iSeq.length();
    }

    public static CustomChromosome of(ISeq<CustomGene> genes) {
        return new CustomChromosome(genes);
    }

    @Override
    public Chromosome<CustomGene> newInstance(ISeq<CustomGene> genes) {
        return new CustomChromosome(genes);
    }

    @Override
    public CustomGene getGene(int index) {
        return iSeq.get(index);
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public ISeq<CustomGene> toSeq() {
        return iSeq;
    }

    @Override
    public boolean isValid() {
        List<Boolean> lst = new ArrayList<>();
        for (int i = 0; i < StateObj.MaxBin; i++) {
            lst.add(false);
        }
        for (int i = 0; i < length; i++) {
            if (iSeq.get(i).getAllele().obj >= 0 && iSeq.get(i).getAllele().obj < StateObj.MaxBin) {
                lst.set(iSeq.get(i).getAllele().obj, true);
            }
        }
        for (int i = 0; i < StateObj.MaxBin; i++) {
            if (lst.get(i) == false) {
                return false;
            }
        }
        /*for (int i = 0; i < length-1; i++) {
            for (int j=i+1;j<length;j++){
                if(iSeq.get(i).getAllele().obj<StateObj.MaxBin&&iSeq.get(i).getAllele().obj>0&&Objects.equals(iSeq.get(i).getAllele().obj, iSeq.get(j).getAllele().obj))
                    return false;
            }
        }*/
        return true;
    }

    @Override
    public Iterator<CustomGene> iterator() {
        return iSeq.iterator();
    }

    @Override
    public Chromosome<CustomGene> newInstance() {
        ISeq<CustomGene> genes = ISeq.empty();
        for (int i = 0; i < length; i++) {
            genes = genes.append(CustomGene.of(StateObj.Rand()));
        }
        return new CustomChromosome(genes);
    }

}
