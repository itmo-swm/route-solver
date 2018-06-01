/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import static org.giggsoff.jspritproj.Main.ar;
import static org.giggsoff.jspritproj.Main.mongo;
import org.giggsoff.jspritproj.models.Point;
import org.giggsoff.jspritproj.models.Polygon;
import org.giggsoff.jspritproj.models.SGB;
import org.giggsoff.jspritproj.utils.Line;
import org.giggsoff.jspritproj.utils.ODF;

/**
 *
 * @author giggsoff
 */
class CurrentTask extends TimerTask {

    public static List<Date> lastTime = new ArrayList<>();
    public static List<Integer> lastNum = new ArrayList<>();
    public static List<Integer> curNum = new ArrayList<>();
    public static List<Date> lpTime = new ArrayList<>();
    public static Double diff = 0.;

    public void getCurrentPosition() {
        Date cur = new Date();
        cur.setTime(cur.getTime());
        cur.setTime(cur.getTime() + diff.longValue() * 1000);
        for (int i = 0; i < Main.ar.size(); i++) {
            Point p = null;
            if (cur.after(lastTime.get(i)) && curNum.get(i) != 0) {
                if (Main.ar.get(i).get(curNum.get(i)).type > 1 && Main.ar.get(i).get(lastNum.get(i)).type > 1) {
                    DB db = mongo.getDB("orion");
                    boolean saved = false;
                    if (!Main.ar.get(i).get(curNum.get(i)).id.equals(Main.ar.get(i).get(lastNum.get(i)).id)) {
                        DBCollection col = db.getCollection("routes");
                        String usuarioJSON = "{\"from\":\"" + Main.ar.get(i).get(lastNum.get(i)).id + "\"," + "\"to\":\"" + Main.ar.get(i).get(curNum.get(i)).id + "\"," + "\"time\":\"" + cur.getTime() + "\"," + "\"diff\":" + diff + "}";
                        DBObject jsonObject = (DBObject) JSON.parse(usuarioJSON);
                        col.insert(jsonObject);
                        saved = true;
                    } else if (Main.ar.get(i).get(curNum.get(i)).type == 2) {
                        DBCollection col = db.getCollection("volumes");
                        Double proc = (Main.ar.get(i).get(curNum.get(i)).dt.getTime() - Main.ar.get(i).get(lastNum.get(i)).dt.getTime()) / 1000. - 5. + 5 * new Random().nextDouble();
                        SGB sg = SGB.findSGB(Main.sgbList, Main.ar.get(i).get(curNum.get(i)).id);
                        if (sg != null) {
                            String usuarioJSON = "{\"id\":\"" + Main.ar.get(i).get(curNum.get(i)).id + "\"," + "\"process\":" + proc + "," + "\"time\":\"" + cur.getTime() + "\"," + "\"percent\":" + sg.volume / sg.max * 100 + "}";
                            DBObject jsonObject = (DBObject) JSON.parse(usuarioJSON);
                            col.insert(jsonObject);
                            saved = true;
                        }
                    }
                    if (saved) {
                        System.out.println(cur);
                        System.out.println(lastTime.get(i));
                        System.out.println(Main.ar.get(i).get(curNum.get(i)).dt);
                        System.out.println(Main.ar.get(i).get(curNum.get(i)).id);
                    }
                }
                lastTime.get(i).setTime(Main.ar.get(i).get(curNum.get(i)).dt.getTime());
                curNum.set(i, 0);
                lastNum.set(i, 0);
            }
            for (int j = 0; j < Main.ar.get(i).size() - 1; j++) {
                if (Main.ar.get(i).get(j + 1).dt.after(cur) && !Main.ar.get(i).get(j).dt.after(cur)) {
                    lastNum.set(i, j);
                    curNum.set(i, j + 1);
                    Polygon curpart = new Polygon();
                    Date start = Main.ar.get(i).get(j).dt;
                    Date end = Main.ar.get(i).get(j + 1).dt;
                    int n = 0;
                    for (int k = j; k > 0; k--) {
                        if (Main.ar.get(i).get(j).dt.equals(Main.ar.get(i).get(k).dt)) {
                            n = k;
                        } else {
                            break;
                        }
                    }
                    for (int l = n; l <= j; l++) {
                        curpart.addPoint(Main.ar.get(i).get(l));
                    }
                    Double len = curpart.getLength();
                    Double tdiff = new Long((end.getTime() - start.getTime()) / 1000).doubleValue();
                    Random r = new Random();
                    double randomValue = -0.5 + r.nextDouble();
                    diff += randomValue;
                    Double tcurr = new Long((cur.getTime() - start.getTime()) / 1000).doubleValue() + randomValue;
                    Double curlen = len * tcurr / tdiff;
                    Double leni = 0.;
                    for (int l = 0; l < curpart.size() - 1; l++) {
                        leni += curpart.getLength(l);
                        if (leni > curlen) {
                            Line line = new Line(curpart.get(l), curpart.get(l + 1));
                            Double perc = Math.abs((curlen - leni + curpart.getLength(l)) / curpart.getLength(l));
                            p = line.getPercent(perc, cur);
                            //System.out.println(i+": "+p);
                            Main.trposition.set(i, p);
                            break;
                        }
                    }
                    break;
                }
            }
        }
        if(Main.trposition.size()>0){
            String odf = ODF.generateODFLocations(Main.trposition);
            System.out.println("\nODFLoc");
            //System.out.println(odf);
            ODF.sendODF(odf);
            System.out.println("\nODFLoc");
        }
    }

    public CurrentTask(Integer size) {
        lastTime = new ArrayList<>();
        lastNum = new ArrayList<>();
        curNum = new ArrayList<>();
        lpTime = new ArrayList<>();
        Main.trposition = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            lastTime.add(new Date());
            lastNum.add(0);
            curNum.add(0);
            lpTime.add(new Date());
            Main.trposition.add(new Point());
        }
        diff = 0.;
    }

    @Override
    public void run() {
        if (Main.planList.size() > 0 && Main.ar.size() > 0) {
            /*Main.sgbList.get(0).volume = 2.;
                Reader.setUrl(Main.sgbList.get(0).getURLSGB());*/
            getCurrentPosition();
            System.out.println("New position");
        }
    }

}
