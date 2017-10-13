/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.utils;

import com.graphhopper.GraphHopper;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.reader.dem.SRTMProvider;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.AvoidEdgesWeighting;
import com.graphhopper.routing.weighting.PriorityWeighting;
import com.graphhopper.routing.weighting.TurnWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.util.EdgeIteratorState;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.FileUtils;
import org.giggsoff.jspritproj.models.Polygon;

/**
 *
 * @author giggsoff
 */
public class GraphhopperWorker {

    class MyGraphHopper extends GraphHopper {

        private List<Polygon> forbiddenEdges = new ArrayList<>();
        private List<String> edgeIds = new ArrayList<>();

        public void addForbiddenEdges(Polygon p, String id) {
            forbiddenEdges.add(p);
            edgeIds.add(id);
        }

        @Override
        public Weighting createWeighting(HintsMap wMap, FlagEncoder encoder) {
            return new MyFastestWeighting(encoder, wMap, forbiddenEdges);
        }

        private void delForbiddenEdges(String id) {
            int num = edgeIds.indexOf(id);
            while(num>=0){
                forbiddenEdges.remove(num);
                edgeIds.remove(num);
                num = edgeIds.indexOf(id);
            }
        }
    }
    // create one GraphHopper instance

    MyGraphHopper hopper = (MyGraphHopper) new MyGraphHopper().forServer();
    long ghCount = 0;

    public GraphhopperWorker(String osmFile, String graphFolder) throws MalformedURLException, IOException {
        File fl = new File(osmFile);
        if (!fl.exists()) {
            FileUtils.copyURLToFile(new URL("http://download.geofabrik.de/russia/northwestern-fed-district-latest.osm.pbf"), fl);
        }
        hopper.setDataReaderFile(osmFile);
        // where to store graphhopper files?
        hopper.setGraphHopperLocation(graphFolder);
        hopper.setEncodingManager(new EncodingManager("car"));
        hopper.setElevation(true);
        hopper.setElevationProvider(new SRTMProvider());

        // now this can take minutes if it imports or a few seconds for loading
        // of course this is dependent on the area you import
        hopper.importOrLoad();
    }
    
    public void addForbiddenEdges(Polygon p, String id){
        hopper.addForbiddenEdges(p, id);        
    }
    
    public void delForbiddenEdges(String id){
        hopper.delForbiddenEdges(id);        
    }

    public GHResponse getRoute(double latFrom, double lonFrom, double latTo, double lonTo) {
        ghCount++;
// simple configuration of the request object, see the GraphHopperServlet classs for more possibilities.
        GHRequest req = new GHRequest(latFrom, lonFrom, latTo, lonTo).
                setWeighting("fastest").
                setVehicle("car").
                setLocale(Locale.US);
        GHResponse rsp = hopper.route(req);

// first check for errors
        if (rsp.hasErrors()) {
            // handle them!
            // rsp.getErrors()
            return null;
        }
        return rsp;
        /*
// use the best path, see the GHResponse class for more possibilities.
PathWrapper path = rsp.getBest();

// points, distance in meters and time in millis of the full path
PointList pointList = path.getPoints();
double distance = path.getDistance();
long timeInMs = path.getTime();

InstructionList il = path.getInstructions();
// iterate over every turn instruction
for(Instruction instruction : il) {
   instruction.getDistance();
   ...
}

// or get the json
List<Map<String, Object>> iList = il.createJson();

// or get the result as gpx entries:
List<GPXEntry> list = il.createGPXList();*/
    }
}
