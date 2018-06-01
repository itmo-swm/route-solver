/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.giggsoff.jspritproj.utils;

import java.io.StringWriter;
import org.giggsoff.jspritproj.models.Polygon;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.giggsoff.jspritproj.models.Point;
import org.w3c.dom.DOMException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author giggs
 */
public class ODF {

    private static abstract class CallBack<TRet, TArg1, TArg2> {

        public abstract TRet call(TArg1 val1, TArg2 val2);
    }

    public static Boolean sendODF(String odf) {
        Post.unirestPost("http://192.168.1.138:8081", odf);
        return true;
    }

    public static String generateODF(Iterable routes, CallBack<Element, Document, Object> func) {
        DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder;
        try {
            icBuilder = icFactory.newDocumentBuilder();
            Document doc = icBuilder.newDocument();
            Element mainRootElement = doc.createElementNS("http://www.opengroup.org/xsd/omi/1.0/", "omiEnvelope");
            mainRootElement.setAttribute("version", "1.0");
            mainRootElement.setAttribute("ttl", "0");
            doc.appendChild(mainRootElement);
            Element write = doc.createElement("write");
            write.setAttribute("msgformat", "odf");
            mainRootElement.appendChild(write);
            Element msg = doc.createElement("msg");
            write.appendChild(msg);
            Element objects = doc.createElementNS("http://www.opengroup.org/xsd/odf/1.0/", "Objects");
            msg.appendChild(objects);
            Element SWM = doc.createElement("Object");
            SWM.setAttribute("type", "SWMService");
            SWM.setAttribute("prefix", "schema http://www.schema.org/ mv http://www.schema.mobivoc.org/ swm http://sdn.ifmo.ru/waste-management-system/swm-ontology/");
            objects.appendChild(SWM);
            Element idSWM = doc.createElement("id");
            SWM.appendChild(idSWM);
            //idSWM.appendChild(doc.createTextNode("SWMService"));
            idSWM.appendChild(doc.createTextNode("TEMP"));
            Element WTR = doc.createElement("Object");
            WTR.setAttribute("type", "list");
            SWM.appendChild(WTR);
            Element idWTR = doc.createElement("id");
            WTR.appendChild(idWTR);
            idWTR.appendChild(doc.createTextNode("WasteTruckRoutes"));
            int i = 0;
            for (Object route : routes) {
                i++;
                Element Truck = doc.createElement("Object");
                Truck.setAttribute("type", "swm:WasteTruckRoute");
                WTR.appendChild(Truck);
                Element idTruck = doc.createElement("id");
                idTruck.appendChild(doc.createTextNode("Truck " + i + " Route"));
                Truck.appendChild(idTruck);
                CallBack<Element, Document, Object> cb;
                cb = func;
                Truck.appendChild(cb.call(doc, route));
            }

            // output DOM XML to console 
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("omit-xml-declaration", "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StringWriter xmlOutWriter = new StringWriter();
            transformer.transform(source, new StreamResult(xmlOutWriter));
            System.out.println("\nXML DOM Created Successfully..");
            return xmlOutWriter.toString();

        } catch (IllegalArgumentException | ParserConfigurationException | TransformerException | DOMException e) {
            System.out.println("\nXML DOM Created UnSuccessfully..");
        }
        return null;
    }

    public static String generateODFLocations(List<Point> points) {
        return generateODF(points, new CallBack<Element, Document, Object>() {
            @Override
            public Element call(Document doc, Object point) {
                return getCurrentLocation(doc, (Point)point);
            }
        });
    }

    public static String generateODFRoute(List<Polygon> routes) {
        return generateODF(routes, new CallBack<Element, Document, Object>() {
            @Override
            public Element call(Document doc, Object route) {
                return getRoute(doc, (Polygon)route);
            }
        });
    }
    

    private static Element getCurrentLocation(Document doc, Point point) {
        Element TruckLocation = doc.createElement("Object");
        TruckLocation.setAttribute("type", "schema:GeoCoordinates");
        TruckLocation.setAttribute("name", "CurrentLocation");
        Element idTruckRoute = doc.createElement("id");
        idTruckRoute.appendChild(doc.createTextNode("CurrentLocation"));
        TruckLocation.appendChild(idTruckRoute);
        TruckLocation.appendChild(createInfoItem(doc, "latitude", "xs:double", String.valueOf(point.y)));
        TruckLocation.appendChild(createInfoItem(doc, "longitude", "xs:double", String.valueOf(point.x)));
        
        return TruckLocation;
    }

    private static Element getRoute(Document doc, Polygon route) {
        Element TruckRoute = doc.createElement("Object");
        TruckRoute.setAttribute("type", "swm:Route");
        Element idTruckRoute = doc.createElement("id");
        idTruckRoute.appendChild(doc.createTextNode("Route1733"));
        TruckRoute.appendChild(idTruckRoute);
        int i = 0;
        for (Point point : route.polygon) {
            i++;
            Element Point = doc.createElement("Object");
            TruckRoute.setAttribute("type", "swm:Point");
            Element idPoint = doc.createElement("id");
            idPoint.appendChild(doc.createTextNode("RoutePoint" + i));
            Point.appendChild(idPoint);
            TruckRoute.appendChild(Point);
            Point.appendChild(createInfoItem(doc, "PointType", null, "Point"));
            Point.appendChild(createInfoItem(doc, "status", null, "expected"));
            Point.appendChild(createInfoItem(doc, "time", "xs:long", String.valueOf(point.dt.getTime())));

            Element geo = doc.createElement("Object");
            geo.setAttribute("type", "schema:GeoCoordinates");
            Element idgeo = doc.createElement("id");
            idgeo.appendChild(doc.createTextNode("geo"));
            geo.appendChild(idgeo);
            Point.appendChild(geo);
            geo.appendChild(createInfoItem(doc, "latitude", "xs:double", String.valueOf(point.y)));
            geo.appendChild(createInfoItem(doc, "longitude", "xs:double", String.valueOf(point.x)));
        }
        return TruckRoute;
    }

    private static Node createInfoItem(Document doc, String name, String type, String val) {
        Element InfoItem = doc.createElement("InfoItem");
        InfoItem.setAttribute("name", name);
        Element value = doc.createElement("value");
        if (type != null) {
            InfoItem.setAttribute("type", type);
        }
        InfoItem.appendChild(value);
        value.appendChild(doc.createTextNode(val));
        return InfoItem;
    }
}
