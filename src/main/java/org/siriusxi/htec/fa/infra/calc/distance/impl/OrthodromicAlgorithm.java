package org.siriusxi.htec.fa.infra.calc.distance.impl;

import org.siriusxi.htec.fa.infra.calc.distance.DistanceAlgorithm;
import org.siriusxi.htec.fa.infra.calc.distance.MeasureType;
import org.siriusxi.htec.fa.infra.calc.distance.Point;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * Orthodromic Algorithm, is used to calculate the distance between co-ordinates of point A and
 * point B.
 *
 * The great circle distance or the orthodromic distance is the shortest distance between
 * two points on a sphere (or the surface of Earth).
 *
 * @apiNote The great circle method is chosen over other methods.
 *
 * @author Venkata Anil Kumar
 * @since v0.4
 */
 public class OrthodromicAlgorithm implements DistanceAlgorithm {
    
    /**
     * {@inheritDoc}
     *
     * @param first point in latitude and longitude.
     * @param second point in latitude and longitude.
     * @param measure the result of the calculation either to be in Mile or KM.
     * @return the final distance.
     *
     */
    @Override
    public double calculate(Point first, Point second, MeasureType measure) {
    
        requireNonNull(first, "First co-ordinate can't be null.");
        requireNonNull(second, "Second co-ordinate can't be null.");
        measure = requireNonNullElse(measure, MeasureType.KILOMETER);
    
        /*
           Converts from degrees to radians.
           lat = Latitude / (180/pi) OR
           lat = Latitude / 57.29577951
           
           1. Find the value of the longitude in radians:
         */
        double longitude1 = toRadians(first.longitude());
        double longitude2 = toRadians(second.longitude());
    
        // Find the value of latitude in radians:
        double latitude1 = toRadians(first.latitude());
        double latitude2 = toRadians(second.latitude());
    
        // part of Haversine formula
        double longitude = longitude2 - longitude1;
        double latitude = latitude2 - latitude1;
        
        double a = pow(sin(latitude / 2), 2)
                       + cos(latitude1) * cos(latitude2)
                             * pow(sin(longitude / 2),2);
    
        // Calculate the distance
        double distance = 2 * asin(sqrt(a));
    
        // calculate the result: distance * reduce of earth
        return(distance * measure.getValue());
    }
}
