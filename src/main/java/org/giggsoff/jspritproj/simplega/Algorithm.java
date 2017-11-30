package org.giggsoff.jspritproj.simplega;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.giggsoff.jspritproj.Main;
import org.giggsoff.jspritproj.jenetics.CostsInterface;
import org.giggsoff.jspritproj.jenetics.SituationInterface;
import org.giggsoff.jspritproj.jenetics.StateObj;
import org.giggsoff.jspritproj.models.Polygon;
import org.giggsoff.jspritproj.models.Truck;

public class Algorithm {

    /* GA parameters */
    private static final double uniformRate = 0.3;
    private static final double mutationRate = 0.2;
    private static final int tournamentSize = 5;
    private static final boolean elitism = true;
    private static CostsInterface cInt;
    private static SituationInterface sInt;
    public static Individual curGen;
    public static Double time;
    public static Integer processed;

    public Algorithm(SituationInterface si, CostsInterface ci) {
        cInt = ci;
        sInt = si;
    }

    public static Double eval(Individual gt) {
        time = 0.;
        curGen = gt;
        if (!curGen.isValid()) {
            return cInt.getMaxRouteTruckCost() * cInt.getMaxRouteTruckCost() * curGen.wrong;
        }
        processed = 0;
        Double sum = 0.;
        List<Boolean> lst = new ArrayList<>();
        for (int i = 0; i < StateObj.MaxBin; i++) {
            lst.add(false);
        }
        Map<Integer, List<Integer>> routes = new HashMap<>();
        for (int i = 0; i < gt.size(); i++) {
            StateObj so = gt.getGene(i);
            if (!routes.containsKey(so.truck)) {
                routes.put(so.truck, new ArrayList<>());
            }
            if (so.obj >= 0) {
                routes.get(so.truck).add(so.obj);
            }
        }
        for (Integer truck : routes.keySet()) {
            if (routes.get(truck).isEmpty()) {
                continue;
            }
            Truck ta = cInt.getTruckAttrs(truck);
            Double vol = (double) ta.volume;
            List<Double> rc = cInt.getFirstRouteCosts(routes.get(truck).get(0), truck);
            sum += rc.get(0) * ta.ppk / 1000 + rc.get(1) * ta.pph / 3600;
            time += rc.get(1);
            for (int i = 0; i < routes.get(truck).size() - 1; i++) {
                /*if(obj>0&&obj<StateObj.MaxBin)
                    sum++;*/
                rc = cInt.getRouteCosts(routes.get(truck).get(i), routes.get(truck).get(i + 1));
                sum += rc.get(0) * ta.ppk / 1000 + rc.get(1) * ta.pph / 3600;
                time += rc.get(1);
            }
            for (int i = 0; i < routes.get(truck).size(); i++) {
                if (routes.get(truck).get(i) >= 0 && routes.get(truck).get(i) < StateObj.MaxBin) {
                    if (cInt.getBinAttrs(routes.get(truck).get(i)).containsKey(ta.type) && ta.max - vol > cInt.getBinAttrs(routes.get(truck).get(i)).get(ta.type) && !lst.get(routes.get(truck).get(i))) {
                        vol += cInt.getBinAttrs(routes.get(truck).get(i)).get(ta.type);
                        lst.set(routes.get(truck).get(i), true);
                        //sum += cInt.getMinRouteTruckCost();
                        processed++;
                        /*}else if (lst.get(routes.get(truck).get(i))) {
                        sum += cInt.getMaxRouteTruckCost()*vol;
                    } else if(!cInt.getBinAttrs(routes.get(truck).get(i)).containsKey(ta.type)){
                        sum += cInt.getMaxRouteTruckCost()*vol;   */
                    } else if (ta.max - vol <= cInt.getBinAttrs(routes.get(truck).get(i)).get(ta.type)) {
                        sum += cInt.getMaxRouteTruckCost();
                    } else {
                        sum += cInt.getMinRouteTruckCost();
                    }
                } else if (routes.get(truck).get(i) >= StateObj.MaxBin) {
                    if (vol > 0.) {
                        //if(cInt.getDumpReprAttrs(routes.get(truck).get(i)).containsKey(ta.type)){
                        sum += vol * cInt.getDumpReprAttrs(routes.get(truck).get(i)).get(ta.type) / cInt.getMaxProfit();
                        vol = 0.;
                        //}
                    } else {
                        sum += cInt.getMaxRouteTruckCost();
                    }
                    sum -= cInt.getMinRouteTruckCost();
                }
            }
            if (vol > 0.) {
                sum += vol * cInt.getMaxRouteTruckCost();
            }
        }

        for (int i = 0; i < StateObj.MaxBin; i++) {
            if (lst.get(i) == false) {
                sum += cInt.getMaxRouteTruckCost() * 10;
            }
        }
        return sum;
    }

    public static List<Polygon> getRoutes() {
        List<Polygon> lp = new ArrayList<>();
        Map<Integer, List<Integer>> routes = new HashMap<>();
        for (int i = 0; i < curGen.size(); i++) {
            StateObj so = curGen.getGene(i);
            if (!routes.containsKey(so.truck)) {
                routes.put(so.truck, new ArrayList<>());
            }
            if (so.obj >= 0 && (routes.get(so.truck).size() < 2 || routes.get(so.truck).get(routes.get(so.truck).size() - 1) != so.obj)) {
                routes.get(so.truck).add(so.obj);
            }
        }
        for (Integer li : routes.keySet()) {
            Polygon pl = new Polygon();
            pl.addPoint(sInt.getPointFirst(li));
            for (Integer pnum : routes.get(li)) {
                pl.addPoint(sInt.getPoint(pnum));
            }
            lp.add(pl);
        }
        return lp;
    }

    /* Public methods */
    // Evolve a population
    public static Population evolvePopulation(Population pop) {
        Population newPopulation = new Population(pop.size(), false);

        // Keep our best individual
        if (elitism) {
            newPopulation.saveIndividual(0, pop.getFittest());
        }

        // Crossover population
        int elitismOffset;
        if (elitism) {
            elitismOffset = 1;
        } else {
            elitismOffset = 0;
        }
        // Loop over the population size and create new individuals with
        // crossover
        for (int i = elitismOffset; i < pop.size(); i++) {
            Individual indiv1 = tournamentSelection(pop);
            Individual indiv2 = tournamentSelection(pop);
            Individual newIndiv = crossover(indiv1, indiv2);
            newPopulation.saveIndividual(i, newIndiv);
        }

        // Mutate population
        if (Math.random() <= mutationRate) {
            for (int i = elitismOffset; i < newPopulation.size(); i++) {
                if (Math.random() <= mutationRate) {
                    newPopulation.getIndividual(i).generateIndividual();
                }
                //mutate(newPopulation.getIndividual(i));
            }
        }

        return newPopulation;
    }

    // Crossover individuals
    private static Individual crossover(Individual indiv1, Individual indiv2) {
        Individual newSol = new Individual();
        for (int i = 0; i < indiv1.size(); i++) {
            newSol.setGene(i, indiv1.getGene(i));
        }
        // Loop through genes
        if (Math.random() <= uniformRate) {
            for (int i = 0; i < indiv1.size() - 1; i++) {
                for (int j = i + 1; j < indiv1.size(); j++) {
                    // Crossover
                    if (Math.random() <= uniformRate) {
                        StateObj st1 = indiv1.getGene(j);
                        StateObj st2 = indiv1.getGene(i);
                        if (st1.truck==st2.truck||(st1.obj >= StateObj.MaxBin && st2.obj >= StateObj.MaxBin)) {
                            Integer tmp = st2.truck;
                            st2.truck = st1.truck;
                            st1.truck = tmp;
                        }
                        newSol.setGene(j, st2);
                        newSol.setGene(i, st1);
                        return newSol;
                    }
                }
            }
        }
        return newSol;
    }

    // Mutate an individual
    private static void mutate(Individual indiv) {
        // Loop through genes
        for (int i = 0; i < indiv.size(); i++) {
            //if(indiv.getGene(i).obj<0||indiv.getGene(i).obj>=StateObj.MaxBin){
            if (Math.random() <= mutationRate) {
                // Create random gene
                StateObj gene = StateObj.Rand();
                indiv.setGene(i, gene);
            }
            //}
        }
    }

    // Select individuals for crossover
    private static Individual tournamentSelection(Population pop) {
        // Create a tournament population
        Population tournament = new Population(tournamentSize, false);
        // For each place in the tournament get a random individual
        for (int i = 0; i < tournamentSize; i++) {
            int randomId = (int) (Math.random() * pop.size());
            tournament.saveIndividual(i, pop.getIndividual(randomId));
        }
        // Get the fittest
        Individual fittest = tournament.getFittest();
        return fittest;
    }
}
