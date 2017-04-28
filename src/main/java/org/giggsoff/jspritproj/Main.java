package org.giggsoff.jspritproj;

import com.graphhopper.GHResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.giggsoff.jspritproj.models.Dump;
import org.giggsoff.jspritproj.models.Point;
import org.giggsoff.jspritproj.models.Region;
import org.giggsoff.jspritproj.models.SGB;
import org.giggsoff.jspritproj.models.Truck;
import org.giggsoff.jspritproj.utils.GeoJson;
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
            server.createContext("/get_routes", new RealHandler());
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
            //doWork(null, true, new JSONArray(), 2300.);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void doWork(HttpExchange he, boolean show, JSONArray regs, Double maxTime) {
        try {
            List<Region> rlist = Region.fromArray(Reader.readArray("get_regions"));
            regionList = new ArrayList<>();
            if (regs.length() == 0) {
                regionList.addAll(rlist);
            } else {
                for (Region r : rlist) {
                    regs.forEach(item -> {
                        if (item.equals(r.id) && !regionList.contains(r)) {
                            regionList.add(r);
                        }
                    });
                }
            }
            List<Truck> tlist = Truck.fromArray(Reader.readArray("get_truck?region=" + regionList.get(0).id));
            trList = new ArrayList<>();
            trList.addAll(tlist);
            trList.sort((Truck o1, Truck o2) -> o2.priority - o1.priority);
            List<SGB> list = SGB.fromArray(Reader.readArray("get_sgb?region=" + regionList.get(0).id));
            sgbList = new ArrayList<>();
            sgbList.addAll(list);
            List<Dump> dlist = Dump.fromArray(Reader.readArray("get_waste_dumps?region=" + regionList.get(0).id));
            dumpList = new ArrayList<>();
            dumpList.addAll(dlist);
            Long t = 0l;
            JSONArray ar = new JSONArray();
            int trCount = trList.size();
            do {
                VehicleRoutingProblemSolution solve = Solver.solve(trList.subList(0, trCount), sgbList, dumpList, gw, show);
                if (solve.getUnassignedJobs().size() > 0 && ar.length() > 0) {
                    break;
                }
                ar = new JSONArray();
                List<Long> maxT = new ArrayList<>();
                for (VehicleRoute vr : solve.getRoutes()) {
                    maxT.add(0l);
                    List<Point> vehroute = new ArrayList<>();
                    vehroute.add(new Point(vr.getStart().getLocation().getCoordinate()));
                    for (TourActivity ta : vr.getActivities()) {
                        vehroute.add(new Point(ta.getLocation().getCoordinate()));
                    }
                    vehroute.add(new Point(vr.getEnd().getLocation().getCoordinate()));
                    List<Point> tcoords = new ArrayList<>();
                    for (int i = 0; i < vehroute.size() - 1; i++) {
                        if (vehroute.get(i).toString().equals(vehroute.get(i + 1).toString())) {
                            continue;
                        }
                        GHResponse grp = Solver.getRoute(vehroute.get(i), vehroute.get(i + 1), gw);
                        maxT.set(maxT.size() - 1, maxT.get(maxT.size() - 1) + grp.getBest().getTime());
                        if (grp != null) {
                            for (int j = 0; j < grp.getBest().getPoints().size(); j++) {
                                tcoords.add(new Point(grp.getBest().getPoints().getLon(i), grp.getBest().getPoints().getLat(i)));
                            }
                        }
                    }
                    if (tcoords.size() == 0) {
                        tcoords.add(new Point(vehroute.get(0).x, vehroute.get(0).y));
                    }
                    ar.put(GeoJson.getGeoJSON(tcoords));
                }
                for (Long l : maxT) {
                    if (l > t) {
                        t = l;
                    }
                }
                trCount -= 1;
            } while (t < maxTime * 1000 && trCount > 0);
            if (!show) {
                String response = ar.toString();
                he.getResponseHeaders().set("Content-Type", "application/json");
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        } catch (JSONException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange he) {
            try {
                List<Region> rlist = Region.fromArray(Reader.readArray("get_regions"));
                regionList = new ArrayList<>();
                regionList.addAll(rlist);
                List<Truck> tlist = Truck.fromArray(Reader.readArray("get_truck?region=" + regionList.get(0).id));
                trList = new ArrayList<>();
                trList.addAll(tlist);
                List<SGB> list = SGB.fromArray(Reader.readArray("get_sgb?region=" + regionList.get(0).id));
                sgbList = new ArrayList<>();
                sgbList.addAll(list);
                List<Dump> dlist = Dump.fromArray(Reader.readArray("get_waste_dumps?region=" + regionList.get(0).id));
                dumpList = new ArrayList<>();
                dumpList.addAll(dlist);
                VehicleRoutingProblemSolution solve = Solver.solve(trList, sgbList, dumpList, gw, false);
                JSONArray ar = new JSONArray();
                for (VehicleRoute vr : solve.getRoutes()) {
                    List<Point> vehroute = new ArrayList<>();
                    vehroute.add(new Point(vr.getStart().getLocation().getCoordinate()));
                    for (TourActivity ta : vr.getActivities()) {
                        vehroute.add(new Point(ta.getLocation().getCoordinate()));
                    }
                    vehroute.add(new Point(vr.getEnd().getLocation().getCoordinate()));
                    JSONArray tcoords = new JSONArray();
                    for (int i = 0; i < vehroute.size() - 1; i++) {
                        if (vehroute.get(i).toString().equals(vehroute.get(i + 1).toString())) {
                            continue;
                        }
                        GHResponse grp = Solver.getRoute(vehroute.get(i), vehroute.get(i + 1), gw);
                        if (grp != null) {
                            for (int j = 0; j < grp.getBest().getPoints().size(); j++) {
                                tcoords.put((new JSONArray()).put(grp.getBest().getPoints().getLon(i)).put(grp.getBest().getPoints().getLat(i)));
                            }
                        }
                    }
                    if (tcoords.length() == 0) {
                        tcoords.put((new JSONArray()).put(vehroute.get(0).x).put(vehroute.get(0).y));
                    }
                    ar.put(tcoords);
                }
                String response = ar.toString();
                he.getResponseHeaders().set("Content-Type", "application/json");
                he.sendResponseHeaders(200, response.length());
                OutputStream os = he.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (JSONException | ParseException | IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }

    static class RealHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) {
            JSONArray regs = new JSONArray();
            Double maxTime = -1.;
            if (httpExchange.getRequestURI().getQuery() != null) {
                Map<String, String> params = queryToMap(httpExchange.getRequestURI().getQuery());
                for (String header : params.keySet()) {
                    if (header.equals("regions")) {
                        regs = new JSONArray(java.net.URLDecoder.decode(params.get(header)));
                    } else if (header.equals("max_time_4_garbage_collection")) {
                        maxTime = Double.parseDouble(java.net.URLDecoder.decode(params.get(header)));
                    }
                    System.out.println(header + "->" + params.get(header));
                }
                doWork(httpExchange, false, regs, maxTime);
            } else {
                doWork(httpExchange, false, new JSONArray(), -1.);
            }
        }
    }

}
