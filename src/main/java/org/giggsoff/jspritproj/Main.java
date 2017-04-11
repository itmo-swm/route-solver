package org.giggsoff.jspritproj;

import org.giggsoff.jspritproj.utils.Reader;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.giggsoff.jspritproj.models.Dump;
import org.giggsoff.jspritproj.models.Region;
import org.giggsoff.jspritproj.models.SGB;
import org.giggsoff.jspritproj.models.Truck;
import org.giggsoff.jspritproj.utils.GraphhopperWorker;
import org.giggsoff.jspritproj.utils.Solver;
import org.json.JSONArray;
import org.json.JSONException;

public class Main {
    
    public static List<Truck> trList = new ArrayList<>();
    public static List<SGB> sgbList = new ArrayList<>();
    public static List<Dump> dumpList = new ArrayList<>();
    public static List<Region> regionList = new ArrayList<>();
    public static GraphhopperWorker gw = null;
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/test", new MyHandler());
            server.start();
        } catch (IOException | JSONException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
         * some preparation - create output folder
         */
        File dir = new File("output");
        // if the directory does not exist, create it
        if (!dir.exists()) {
            System.out.println("creating directory ./output");
            boolean result = dir.mkdir();
            if (result) {
                System.out.println("./output created");
            }
        }
        try {
            gw = new GraphhopperWorker("map.pbf", "output");
            System.out.println("Ready");
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) throws IOException {
            try {
                List<Region> rlist = Region.fromArray(Reader.readArray("get_regions"));
                regionList = new ArrayList<>();
                regionList.addAll(rlist);
                List<Truck> tlist = Truck.fromArray(Reader.readArray("get_truck?region="+regionList.get(0).id));
                trList = new ArrayList<>();
                trList.addAll(tlist);
                List<SGB> list = SGB.fromArray(Reader.readArray("get_sgb?region="+regionList.get(0).id));
                sgbList = new ArrayList<>();
                sgbList.addAll(list);
                List<Dump> dlist = Dump.fromArray(Reader.readArray("get_waste_dumps?region="+regionList.get(0).id));
                dumpList = new ArrayList<>();
                dumpList.addAll(dlist);
                VehicleRoutingProblemSolution solve = Solver.solve(trList, sgbList, dumpList, gw, false);
                JSONArray ar = new JSONArray();
                for(VehicleRoute vr:solve.getRoutes()){
                    JSONArray vehroute = new JSONArray();
                    for(TourActivity ta:vr.getActivities()){
                        vehroute.put(new JSONArray().put(ta.getLocation().getCoordinate().getX()).put(ta.getLocation().getCoordinate().getY()));
                    }
                    ar.put(vehroute);
                }
                String response = ar.toString();
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (JSONException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
