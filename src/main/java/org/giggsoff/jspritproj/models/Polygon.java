/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.models;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.giggsoff.jspritproj.utils.Line;

/**
 *
 * @author giggsoff
 */
public class Polygon implements Iterable<Point>{
    public List<Point> polygon; 
    
    private BoundingBox _boundingBox;
    
    public Polygon(){
        polygon = new ArrayList<>();
        _boundingBox = new BoundingBox();
    }
    
    public void addPoint(Point pt){
        polygon.add(pt);
        updateBoundingBox(pt);
    }

    public void addPoint(double x, double y, int type, String id) {
        Point pt = new Point(x, y, type, id);
        addPoint(pt);
        updateBoundingBox(pt);
    }
    
    public int size(){
        return polygon.size();
    }
    
    public Point get(Integer i){
        return polygon.get(i);
    }
    
    public boolean isIntersect(Point point){        
        if (inBoundingBox(point)) {
            Line ray = createRay(point);
            int intersection = 0;
            for(int i=0;i<polygon.size();i++){
                Line side = new Line(polygon.get(i),polygon.get((i+1)==polygon.size()?0:i+1));
                if (intersect(ray, side)) {
                    // System.out.println("intersection++");
                    intersection++;
                }
            }
            if (intersection % 2 != 0) {
                return true;
            }
        }
        return false;
    }
    
    private boolean inBoundingBox(Point point) {
        if (point.x < _boundingBox.xMin || point.x > _boundingBox.xMax || point.y < _boundingBox.yMin || point.y > _boundingBox.yMax) {
            return false;
        }
        return true;
    }

    private static class BoundingBox {
        public double xMax = Double.NEGATIVE_INFINITY;
        public double xMin = Double.POSITIVE_INFINITY;
        public double yMax = Double.NEGATIVE_INFINITY;
        public double yMin = Double.POSITIVE_INFINITY;
    }
    
        private void updateBoundingBox(Point point) {
                // set bounding box
                if (point.x > _boundingBox.xMax) {
                    _boundingBox.xMax = point.x;
                } else if (point.x < _boundingBox.xMin) {
                    _boundingBox.xMin = point.x;
                }
                if (point.y > _boundingBox.yMax) {
                    _boundingBox.yMax = point.y;
                } else if (point.y < _boundingBox.yMin) {
                    _boundingBox.yMin = point.y;
                }
        }
    
    private Line createRay(Point point) {
        Point outsidePoint = new Point(0., 0., 0, "");

        Line vector = new Line(outsidePoint, point);
        return vector;
    }
    
    private boolean intersect(Line ray, Line side) {
        Point intersectPoint = null;

        // if both vectors aren't from the kind of x=1 lines then go into
        if (!ray.isVertical() && !side.isVertical()) {
            // check if both vectors are parallel. If they are parallel then no intersection point will exist
            if (ray.getA() - side.getA() == 0) {
                return false;
            }

            double x = ((side.getB() - ray.getB()) / (ray.getA() - side.getA())); // x = (b2-b1)/(a1-a2)
            double y = side.getA() * x + side.getB(); // y = a2*x+b2
            intersectPoint = new Point(x, y, 0, "");
        } else if (ray.isVertical() && !side.isVertical()) {
            double x = ray.getStart().x;
            double y = side.getA() * x + side.getB();
            intersectPoint = new Point(x, y, 0, "");
        } else if (!ray.isVertical() && side.isVertical()) {
            double x = side.getStart().x;
            double y = ray.getA() * x + ray.getB();
            intersectPoint = new Point(x, y, 0, "");
        } else {
            return false;
        }

        if (side.isInside(intersectPoint) && ray.isInside(intersectPoint)) {
            return true;
        }

        return false;
    }
    
    @Override
    public Iterator iterator() {
        return polygon.iterator();
    }
}
