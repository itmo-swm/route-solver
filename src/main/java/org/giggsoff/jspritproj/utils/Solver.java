/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.utils;

import com.graphhopper.GHResponse;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.giggsoff.jspritproj.alg.VehicleRoutingAlgorithmBuilder;
import org.giggsoff.jspritproj.models.SGB;
import org.giggsoff.jspritproj.models.Truck;

/**
 *
 * @author giggsoff
 */
public class Solver {

    public static void solve(List<Truck> trList, List<SGB> sgbList, GraphhopperWorker gw) {
        
        System.out.println("INITIAL COUNT: "+gw.ghCount);
        /*
         * get a vehicle type-builder and build a type with the typeId "vehicleType" and one capacity dimension, i.e. weight, and capacity dimension value of 2
         */
        final int WEIGHT_INDEX = 0;
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(WEIGHT_INDEX, 2);
        VehicleType vehicleType = vehicleTypeBuilder.build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
         */
        for (Truck tr : trList) {
            VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
            vehicleBuilder.setStartLocation(Location.newInstance(tr.lat, tr.lng));
            vehicleBuilder.setType(vehicleType);
            VehicleImpl vehicle = vehicleBuilder.build();
            vrpBuilder.addVehicle(vehicle);
        }

        /*
         * build services at the required locations, each with a capacity-demand of 1.
         */
        int i = 0;
        for (SGB sgb : sgbList) {
            Service service = Service.Builder.newInstance(Integer.toString(++i)).addSizeDimension(WEIGHT_INDEX, 1).setLocation(Location.newInstance(sgb.lat, sgb.lng)).build();
            vrpBuilder.addJob(service);
        }

        
        SolutionCostCalculator costCalculator = new SolutionCostCalculator() {
            @Override
            public double getCosts(VehicleRoutingProblemSolution solution) {
                double costs = 0.;
                List<VehicleRoute> routes = (List<VehicleRoute>) solution.getRoutes();                
                for (VehicleRoute route : routes) {
                    List<Coordinate> lc = new ArrayList<>();
                    lc.add(route.getStart().getLocation().getCoordinate());
                    for(TourActivity ta : route.getActivities()){
                        lc.add(ta.getLocation().getCoordinate());
                    }
                    lc.add(route.getEnd().getLocation().getCoordinate());
                    for(int i=0;i<lc.size()-1;i++){
                        GHResponse resp = gw.getRoute(lc.get(i).getY(), lc.get(i).getX(), lc.get(i+1).getY(), lc.get(i+1).getX());
                        if(resp!=null)
                            costs += resp.getBest().getDistance();
                    }
                    
                    //costs += route.getVehicle().getType().getVehicleCostParams().fix;
                    /*costs+=stateManager.getRouteState(route, InternalStates.COSTS, Double.class);
                    for (RewardAndPenaltiesThroughSoftConstraints contrib : contribs) {
                        costs+=contrib.getCosts(route);
                    }*/
                }
                return costs;
            }

        };

        VehicleRoutingProblem problem = vrpBuilder.build();

        /*
         * get the algorithm out-of-the-box.
         */
        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(problem, "algorithmConfig.xml");
        vraBuilder.addDefaultCostCalculators();
        vraBuilder.setObjectiveFunction(costCalculator);
        VehicleRoutingAlgorithm algorithm = vraBuilder.build();

        /*
         * and search a solution
         */
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        /*
         * get the best
         */
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        new VrpXMLWriter(problem, solutions).write("output/problem-with-solution.xml");

        SolutionPrinter.print(problem, bestSolution, SolutionPrinter.Print.VERBOSE);

        /*
         * plot
         */
        new Plotter(problem, bestSolution).plot("output/plot.png", "simple example");

        /*
        render problem and solution with GraphStream
         */
        new GraphStreamViewer(problem, bestSolution).labelWith(GraphStreamViewer.Label.ID).setRenderDelay(200).display();
        
        System.out.println("LAST COUNT: "+gw.ghCount);
    }
}
