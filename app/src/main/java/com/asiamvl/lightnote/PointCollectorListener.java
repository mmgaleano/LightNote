package com.asiamvl.lightnote;

import android.graphics.Point;

import java.util.List;

/** Represents a point collector listener interface.
 * @author Michael Mora
 * @version 1.0
 * @since 1.0
 */

public interface PointCollectorListener {

    /** pointCollected Interface.
     * @param points ArrayList of Points.
     */

    public void pointCollected(List<Point> points);
}
