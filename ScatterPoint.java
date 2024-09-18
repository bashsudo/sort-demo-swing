/*
 * CSC 345 PROJECT
 * Class:           ScatterPoint.java
 * Authors:         Angelina A, Eiza S, Ethan W, Hayden R
 * Description:     A representation of a 2D coordinate point with long
 *                  x-values and y-values. The object is immutable
 *                  because the values cannot be changed once they are
 *                  set upon construction. This is intended to be used
 *                  with the ScatterPlotPanel. Although Java offers
 *                  its own Point and Point2D class, they do not support
 *                  long types and have additional overhead and features
 *                  that are not required.
 */

public class ScatterPoint {
    private final long x;
    private final long y;

    /**
     * Initializes the ScatterPoint object as a coordinate point with the given
     * x-value and y-value.
     * 
     * @param x the long x-value of the coordinate point
     * @param y the long y-value of the coordinate point
     */
    public ScatterPoint(long x, long y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the long x-value of the coordinate point
     * 
     * @return the long x-value
     */
    public long getX() {
        return x;
    }

    /**
     * Returns the long y-value of the coordinate point
     * 
     * @return the long y-value
     */
    public long getY() {
        return y;
    }
}