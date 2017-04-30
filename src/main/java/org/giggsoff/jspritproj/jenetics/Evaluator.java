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
import org.giggsoff.jspritproj.models.Truck;
import org.jenetics.Genotype;
import org.jenetics.Mutator;
import org.jenetics.Optimize;
import org.jenetics.RouletteWheelSelector;
import org.jenetics.MultiPointCrossover;
import org.jenetics.TournamentSelector;
import org.jenetics.engine.Engine;
import org.jenetics.engine.EvolutionResult;
import org.jenetics.util.Factory;

/**
 *
 * @author giggsoff
 */
public class Evaluator {  

    public static Mark Evaluate(SituationInterface si, CostsInterface ci) {

        StateObj.MaxBin = si.getSGBs();
        StateObj.MaxTruck = si.getTrucks();
        StateObj.MaxDel = si.getDumps();
        
        Factory<Genotype<CustomGene>> g
                = Genotype.of(CustomChromosome.of(CustomGene.seq((si.getSGBs()+si.getDumps())*2)));

        Mark ev = new Mark(si,ci);
        Engine<CustomGene, Double> engine = Engine
                .builder(ev::eval, g)
                .optimize(Optimize.MINIMUM)
                .populationSize(50)
                .survivorsSelector(new TournamentSelector<>(5))
                .offspringSelector(new RouletteWheelSelector<>())
                .alterers(new Mutator<>(0.15), new MultiPointCrossover<>(0.3,2))
                .build();

        Genotype<CustomGene> result = engine.stream().limit(1000)
                .collect(EvolutionResult.toBestGenotype());

        System.out.println(ev.eval(result));
        System.out.println(result.getChromosome().isValid());
        result.getChromosome().stream().forEach(i -> {
            System.out.print(i.getAllele() + " ");
        });
        System.out.println();
        return ev;
    }
}
