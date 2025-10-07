package org.siriusxi.htec.fa.infra.calc.distance;

import org.siriusxi.htec.fa.infra.calc.distance.impl.HaversineAlgorithm;
import org.siriusxi.htec.fa.infra.calc.distance.impl.OrthodromicAlgorithm;

/**
 * A distance Algorithm interface to represent different calculations algorithms type
 * of distances between any two points on earth.
 *
 * The following is the supported distance algorithms:
 *  <ol>
 *      <li>Haversine formula</li>
 *      <li>Orthodromic formula</li>
 *  </ol>
 *
 * @author Venkata Anil Kumar
 * @since v0.4
 */
public interface DistanceAlgorithm {
    
    /**
     * Method to calculate the distance between co-ordinates of two points.
     *
     * @param first point in latitude and longitude.
     * @param second point in latitude and longitude.
     * @param measure the result of the calculation either to be in Mile or KM.
     * @return the final distance.
     */
    double calculate(Point first, Point second, MeasureType measure);
    
    /**
     * A factory method to return the implementation of selected distance  calculator algorithm.
     *
     * @param type of the algorithm.
     * @return The algorithm implementation.
     */
    static DistanceAlgorithm getAlgorithm(AlgorithmType type){
        return switch(type){
            case HAVERSINE -> new HaversineAlgorithm();
            case ORTHODROMIC -> new OrthodromicAlgorithm();
        };
    }

}
