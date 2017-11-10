/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.utils;

import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.giggsoff.jspritproj.alg.VehicleRoutingAlgorithmBuilder;
import org.giggsoff.jspritproj.jenetics.CostsInterface;
import org.giggsoff.jspritproj.jenetics.Evaluator;
import org.giggsoff.jspritproj.jenetics.Mark;
import org.giggsoff.jspritproj.jenetics.SituationInterface;
import org.giggsoff.jspritproj.models.Dump;
import org.giggsoff.jspritproj.models.DumpRepr;
import org.giggsoff.jspritproj.models.Point;
import org.giggsoff.jspritproj.models.Polygon;
import org.giggsoff.jspritproj.models.Processing;
import org.giggsoff.jspritproj.models.SGB;
import org.giggsoff.jspritproj.models.Truck;
import org.giggsoff.jspritproj.models.Types;

/**
 *
 * @author giggsoff
 */
public class Solver {

    public static Map<String, Map<String, PathWrapper>> hashMap = new HashMap<>();
    
    public static Map<String, List<Point>> pMap = new HashMap<>();
    
    public static Types types = new Types();
    
    public static Double curMax = Double.MIN_VALUE;
    
    public static PathWrapper getRoute(Point p1, Point p2, GraphhopperWorker gw){
        
            if(!pMap.containsKey(p1.toString()))
                pMap.put(p1.toString(), new ArrayList());
            if(!pMap.get(p1.toString()).contains(p1)){
                pMap.get(p1.toString()).add(p1);
            }
            
            if(!pMap.containsKey(p2.toString()))
                pMap.put(p2.toString(), new ArrayList());
            if(!pMap.get(p2.toString()).contains(p2)){
                pMap.get(p2.toString()).add(p1);
            }
        if (hashMap.containsKey(p1.toString())) {
                        if (hashMap.get(p1.toString()).containsKey(p2.toString())) {
                            return hashMap.get(p1.toString()).get(p2.toString());
                        } else {
                            GHResponse resp = gw.getRoute(p1.y, p1.x, p2.y, p2.x);
                            if (resp != null) {
                                hashMap.get(p1.toString()).put(p2.toString(), resp.getBest());
                                return resp.getBest();
                            }
                        }
        } else {
                        GHResponse resp = gw.getRoute(p1.y, p1.x, p2.y, p2.x);
                        if (resp != null) {
                            hashMap.put(p1.toString(), new HashMap<>());
                            hashMap.get(p1.toString()).put(p2.toString(), resp.getBest());
                            return resp.getBest();
                        }
        }
        return null;
    }
    
    public static List<Point> getPoints(double lat,double lon){
        return pMap.get(lat+";"+lon);
    }
    
    public static Mark solve(List<Truck> trList, List<SGB> sgbList, List<DumpRepr> dumpList, GraphhopperWorker gw){      
        List<Point> pts = new ArrayList<>();
        pts.addAll(sgbList);
        pts.addAll(dumpList);
        /*List<String> types = new ArrayList<>();
        for(SGB sgb:sgbList){
            if(!types.contains(sgb.type)){
                types.add(sgb.type);
            }
        }
        for(Dump dump:dumpList){
            dump.prices = new HashMap<>();
            for(String type:types){
                dump.prices.put(type, 100.);
            }
        }*/
        curMax = Double.MIN_VALUE;
        return Evaluator.Evaluate(new SituationInterface(){
            @Override
            public Integer getTrucks() {
                return trList.size();
            }

            @Override
            public Integer getSGBs() {
                return sgbList.size();
            }

            @Override
            public Integer getDumpReprs() {
                return dumpList.size();
            }

            @Override
            public Point getPointFirst(Integer tr) {
                return trList.get(tr).getPoint();
            }

            @Override
            public Point getPoint(Integer obj) {
                return pts.get(obj).getPoint();
            }
            
        }, new CostsInterface() {
            @Override
            public List<Double> getRouteCosts(Integer obj1, Integer obj2) {
                List<Double> ld = new ArrayList<>();
                PathWrapper gr = getRoute(pts.get(obj1).getPoint(), pts.get(obj2).getPoint(), gw);
                ld.add(gr.getDistance());
                ld.add((double)gr.getTime()/1000);
                return ld;
            }

            @Override
            public Truck getTruckAttrs(Integer tr) {
                return trList.get(tr);
            }

            @Override
            public Double getMaxRouteTruckCost() {
                return curMax;
            }

            @Override
            public List<Double> getFirstRouteCosts(Integer obj, Integer tr) {
                List<Double> ld = new ArrayList<>();
                PathWrapper gr = getRoute(pts.get(obj).getPoint(), trList.get(tr).getPoint(), gw);
                ld.add(gr.getDistance());
                ld.add((double)gr.getTime()/1000);
                return ld;
            }

            @Override
            public Map<String, Double> getBinAttrs(Integer obj) {
                Map<String, Double> ld = new HashMap<>();         
                ld.put(sgbList.get(obj).type, (double)sgbList.get(obj).volume);
                return ld;
            }

            @Override
            public HashMap<String, Double> getDumpReprAttrs(Integer obj) {
                return dumpList.get(obj-sgbList.size()).getPrices();
            }

            @Override
            public Double updateMaxRouteTruckCost(Double val) {
                if(curMax<val){
                    curMax = val;
                }
                return val;
            }
        });
    }

    public static VehicleRoutingProblemSolution solve(List<Truck> trList, List<SGB> sgbList, List<DumpRepr> dumpList, GraphhopperWorker gw, boolean showPlot) {

        System.out.println("INITIAL COUNT: " + gw.ghCount);
        /*
         * get a vehicle type-builder and build a type with the typeId "vehicleType" and one capacity dimension, i.e. weight, and capacity dimension value of 2
         */
        final int WEIGHT_INDEX = 0;
        final double max_speed_mps = 3.6*50;
        

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        /*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
         */
        for (Truck tr : trList) {
            VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(types.get(tr.type), tr.max).setMaxVelocity(max_speed_mps).setCostPerServiceTime(10).setCostPerTransportTime(tr.pph).setCostPerDistance(tr.ppk);
            VehicleType vehicleType = vehicleTypeBuilder.build();
            VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance(tr.id);
            vehicleBuilder.setStartLocation(Location.newInstance(tr.coord.x, tr.coord.y));
            vehicleBuilder.setType(vehicleType);
            vehicleBuilder.setReturnToDepot(false);
            
            VehicleImpl vehicle = vehicleBuilder.build();
            vrpBuilder.addVehicle(vehicle);
        }

        /*
         * build services at the required locations, each with a capacity-demand of 1.
         */
        int i = 0;
        for (SGB sgb : sgbList) {
            Pickup service = Pickup.Builder.newInstance(Integer.toString(++i)).setServiceTime(100).addSizeDimension(types.get(sgb.type), (int)sgb.volume.doubleValue()).setLocation(Location.newInstance(sgb.coord.x, sgb.coord.y)).build();
            vrpBuilder.addJob(service);
        }
        
        for (DumpRepr dump : dumpList) {
            Service.Builder<Delivery> service = Delivery.Builder.newInstance(Integer.toString(++i)).setServiceTime(100).setLocation(Location.newInstance(dump.coord.x, dump.coord.y));
            for(int j=0;j<types.all().size();j++){
                service.addSizeDimension(j, 900);
            }            
            vrpBuilder.addJob(service.build());
        }

        SolutionCostCalculator costCalculator = (VehicleRoutingProblemSolution solution) -> {
            double time = 0.;
            double km = 0.;
            List<VehicleRoute> routes = (List<VehicleRoute>) solution.getRoutes();
            for (VehicleRoute route : routes) {
                List<Location> lc = new ArrayList<>();
                lc.add(route.getStart().getLocation());
                for (TourActivity ta : route.getActivities()) {
                    lc.add(ta.getLocation());
                }
                lc.add(route.getEnd().getLocation());
                for (int i1 = 0; i1 < lc.size() - 1; i1++) {
                    PathWrapper grp = getRoute(new Point(lc.get(i1).getCoordinate(),0,""), new Point(lc.get(i1+1).getCoordinate(),0,""), gw);
                    if(grp!=null){
                        km += grp.getDistance()*route.getVehicle().getType().getVehicleCostParams().perDistanceUnit/1000;
                        time += grp.getTime()*route.getVehicle().getType().getVehicleCostParams().perTransportTimeUnit/60/60/1000;
                    }
                }
                //costs += route.getVehicle().getType().getVehicleCostParams().fix;
                /*costs+=stateManager.getRouteState(route, InternalStates.COSTS, Double.class);
                for (RewardAndPenaltiesThroughSoftConstraints contrib : contribs) {
                costs+=contrib.getCosts(route);
                }*/
            }
            return km+time;
        };
        
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

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

        if (showPlot) {

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

        }

        System.out.println("LAST COUNT: " + gw.ghCount);
        
        return bestSolution;
    }
}
