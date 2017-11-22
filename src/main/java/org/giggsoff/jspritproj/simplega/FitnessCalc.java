package org.giggsoff.jspritproj.simplega;

import java.util.ArrayList;
import java.util.List;
import org.giggsoff.jspritproj.jenetics.StateObj;

public class FitnessCalc {

    static List<StateObj> solution = new ArrayList<>();

    /* Public methods */
    // Set a candidate solution as a byte array
    public static void setSolution(List<StateObj> newSolution) {
        solution = newSolution;
    }
/*
    // To make it easier we can use this method to set our candidate solution 
    // with string of 0s and 1s
    static void setSolution(String newSolution) {
        solution = new byte[newSolution.length()];
        // Loop through each character of our string and save it in our byte 
        // array
        for (int i = 0; i < newSolution.length(); i++) {
            String character = newSolution.substring(i, i + 1);
            if (character.contains("0") || character.contains("1")) {
                solution[i] = Byte.parseByte(character);
            } else {
                solution[i] = 0;
            }
        }
    }
*/
    // Calculate inidividuals fittness by comparing it to our candidate solution
    static Double getFitness(Individual individual) {
        return Algorithm.eval(individual);
    }
}
