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
import java.util.Date;
import java.util.Random;
import java.util.TimerTask;
import static org.giggsoff.jspritproj.Main.mongo;
import org.giggsoff.jspritproj.models.Point;
import org.giggsoff.jspritproj.models.Polygon;
import org.giggsoff.jspritproj.models.SGB;
import org.giggsoff.jspritproj.utils.Line;

/**
 *
 * @author giggsoff
 */
class CurrentTask extends TimerTask {
    public static Date lastTime = null;
    public static Double diff = 0.;
    public void getCurrentPosition(){
        Date cur = new Date();
        cur.setTime(cur.getTime() + diff.longValue()*1000);
        for(int i=0;i<Main.ar.size();i++)
        {
            Point p = null;
            for(int j=0;j<Main.ar.get(i).size()-1;j++){
                if(Main.ar.get(i).get(j+1).dt.after(cur)&&!Main.ar.get(i).get(j).dt.after(cur)){
                    if(lastTime!=null&&j>0&&!Main.ar.get(i).get(j).dt.before(lastTime)){
                        System.out.println(cur);
                        System.out.println(Main.ar.get(i).get(j).id);                        
                        DB db = mongo.getDB("orion");
                        DBCollection col = db.getCollection("routes");
                        String usuarioJSON = "{\"from\":\""+Main.ar.get(i).get(j).id+"\","+"\"to\":\""+Main.ar.get(i).get(j+1).id+"\","+"\"diff\":"+diff+"}";
			DBObject jsonObject = (DBObject) JSON.parse(usuarioJSON);
                        col.insert(jsonObject);
                        if(Main.ar.get(i).get(j).type==2){
                            col = db.getCollection("volumes");
                            Long proc = (Main.ar.get(i).get(j+1).dt.getTime()-Main.ar.get(i).get(j).dt.getTime())/1000L;
                            SGB sg = SGB.findSGB(Main.sgbList, Main.ar.get(i).get(j).id);
                            if(sg!=null){
                                usuarioJSON = "{\"id\":\""+Main.ar.get(i).get(j).id+"\","+"\"process\":"+proc.intValue()+","+"\"percent\":"+sg.volume/sg.max*100+"}";
                                jsonObject = (DBObject) JSON.parse(usuarioJSON);
                                col.insert(jsonObject);
                            }
                        }
                    }
                    Polygon curpart = new Polygon();
                    Date start = Main.ar.get(i).get(j).dt;
                    Date end = Main.ar.get(i).get(j+1).dt;
                    int n = 0;
                    for(int k=j;k>0;k--){
                        if(Main.ar.get(i).get(j).dt.equals(Main.ar.get(i).get(k).dt)){
                            n = k;
                        }else{
                            break;
                        }
                    }
                    for(int l=n;l<=j;l++){
                        curpart.addPoint(Main.ar.get(i).get(l));
                    }
                    Double len = curpart.getLength();
                    Double tdiff = new Long((end.getTime()-start.getTime())/1000).doubleValue();
                    Random r = new Random();
                    double randomValue = -0.5 + r.nextDouble();
                    diff += randomValue;
                    Double tcurr = new Long((cur.getTime()-start.getTime())/1000).doubleValue()+randomValue;
                    Double curlen = len*tcurr/tdiff;
                    Double leni = 0.;
                    for(int l=0;l<curpart.size()-1;l++){
                        leni+=curpart.getLength(i);
                        if(leni>curlen){
                            Line line = new Line(curpart.get(i), curpart.get(i+1));
                            Double perc = Math.abs((curlen-leni+curpart.getLength(i))/curpart.getLength(i));
                            p = line.getPercent(perc);
                            //System.out.println(i+": "+p);
                            break;
                        }
                    }
                    break;
                }
            }
        }
        lastTime = cur;
    }

    public CurrentTask() {
        lastTime = null;
        diff = 0.;
    }

    @Override
    public void run() {
            if(Main.planList.size()>0&&Main.ar.size()>0){
                /*Main.sgbList.get(0).volume = 2.;
                Reader.setUrl(Main.sgbList.get(0).getURLSGB());*/
                getCurrentPosition();
            }
    }

}
