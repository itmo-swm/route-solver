/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.jenetics;

import org.giggsoff.jspritproj.models.Point;

/**
 *
 * @author giggsoff
 */
public interface SituationInterface {
    Integer getTrucks();
    Integer getSGBs();
    Integer getDumpReprs();
    Point getPointFirst(Integer tr);
    Point getPoint(Integer obj);
}
