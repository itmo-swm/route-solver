package org.giggsoff.jspritproj.utils;

import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.weighting.AbstractWeighting;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.PMap;
import com.graphhopper.util.Parameters.Routing;
import com.graphhopper.util.PointList;
import java.util.List;
import org.giggsoff.jspritproj.models.Point;
import org.giggsoff.jspritproj.models.Polygon;

public class MyFastestWeighting extends AbstractWeighting {
    /**
     * Converting to seconds is not necessary but makes adding other penalties easier (e.g. turn
     * costs or traffic light costs etc)
     */
    protected final static double SPEED_CONV = 3.6;
    private final double headingPenalty;
    private final long headingPenaltyMillis;
    private final double maxSpeed;
    private List<Polygon> fMap;

    public MyFastestWeighting(FlagEncoder encoder, PMap map, List<Polygon> fmap) {
        super(encoder);
        headingPenalty = map.getDouble(Routing.HEADING_PENALTY, Routing.DEFAULT_HEADING_PENALTY);
        headingPenaltyMillis = Math.round(headingPenalty * 1000);
        maxSpeed = encoder.getMaxSpeed() / SPEED_CONV;
        fMap = fmap;
    }

    @Override
    public double getMinWeight(double distance) {
        return distance / maxSpeed;
    }

    @Override
    public double calcWeight(EdgeIteratorState edge, boolean reverse, int prevOrNextEdgeId) {
        double speed = reverse ? flagEncoder.getReverseSpeed(edge.getFlags()) : flagEncoder.getSpeed(edge.getFlags());
        if (speed == 0)
            return Double.POSITIVE_INFINITY;

        PointList p = edge.fetchWayGeometry(3);
        boolean isOk = true;
        for(int i=0;i<p.size();i++){
            for(Polygon pol:fMap){
                if(pol.isIntersect(new Point(p.getLongitude(i), p.getLatitude(i), 0, ""))){
                    isOk = false;
                    break;
                }
            }
            if(!isOk){
                break;
            }
        }
        
        double time = edge.getDistance() / speed * SPEED_CONV;
        if(!isOk)
            time += headingPenalty;

        // add direction penalties at start/stop/via points
        boolean unfavoredEdge = edge.getBool(EdgeIteratorState.K_UNFAVORED_EDGE, false);
        if (unfavoredEdge)
            time += headingPenalty;

        return time;
    }

    @Override
    public long calcMillis(EdgeIteratorState edgeState, boolean reverse, int prevOrNextEdgeId) {
        // TODO move this to AbstractWeighting?
        long time = 0;
        boolean unfavoredEdge = edgeState.getBool(EdgeIteratorState.K_UNFAVORED_EDGE, false);
        if (unfavoredEdge)
            time += headingPenaltyMillis;

        return time + super.calcMillis(edgeState, reverse, prevOrNextEdgeId);
    }

    @Override
    public String getName() {
        return "fastest";
    }
}