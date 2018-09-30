/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.utils;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.reader.dem.SRTMProvider;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.Graph;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.giggsoff.jspritproj.Main;
import org.giggsoff.jspritproj.models.Polygon;

/**
 *
 * @author giggsoff
 */
public class GraphhopperWorker {

    class MyGraphHopper extends GraphHopperOSM {

        private List<Polygon> forbiddenEdges = new ArrayList<>();
        private List<String> edgeIds = new ArrayList<>();

        public void addForbiddenEdges(Polygon p, String id) {
            forbiddenEdges.add(p);
            edgeIds.add(id);
        }

        @Override
        public Weighting createWeighting(HintsMap wMap, FlagEncoder encoder, Graph graph) {
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
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Loading file...");
            FileUtils.copyURLToFile(new URL("http://download.geofabrik.de/russia/northwestern-fed-district-latest.osm.pbf"), fl);
        }
        hopper.setDataReaderFile(fl.getPath());
        hopper.setGraphHopperLocation(graphFolder);
        hopper.setEncodingManager(new EncodingManager("car"));
        hopper.setElevation(true);
        hopper.setElevationProvider(new SRTMProvider());
        hopper.importOrLoad();
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "File loaded");
    }
    
    public void addForbiddenEdges(Polygon p, String id){
        hopper.addForbiddenEdges(p, id);        
    }
    
    public void delForbiddenEdges(String id){
        hopper.delForbiddenEdges(id);        
    }

    public GHResponse getRoute(double latFrom, double lonFrom, double latTo, double lonTo) {
        ghCount++;
        GHRequest req = new GHRequest(latFrom, lonFrom, latTo, lonTo).
                setWeighting("fastest").
                setVehicle("car").
                setLocale(Locale.US);
        GHResponse rsp = hopper.route(req);
        if (rsp.hasErrors()) {
            return null;
        }
        return rsp;
    }
}
