package com.asiamvl.lightnote;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/** Represents a point collector.
 * @author Michael Mora
 * @version 1.0
 * @since 1.0
 */

public class PointCollector implements View.OnTouchListener {

    private ArrayList<Point> points = new ArrayList<Point>();
    private PointCollectorListener listener;
    public final static int NUM_POINTS = 4;


    /** Sets the Point Collector listener.
     * @param listener A PointCollectorListener.
     */

    public void setListener(PointCollectorListener listener) {
        this.listener = listener;
    }


    /** Overrides onTouch method and executes the pointCollected interface.
     * @param view View being touched.
     * @param motionEvent Event reference.
     * @return A boolean false for the next event
     */

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        int x = Math.round(motionEvent.getX());
        int y = Math.round(motionEvent.getY());

        points.add(new Point(x,y));

        if(points.size() == NUM_POINTS){
            if(listener != null){
                listener.pointCollected(points); //Executes the interface
            }

        }

        return false;
    }

    /** Clears the ArrayList<Point> object in PointCollector.
     */

    public void clear(){
        points.clear();
    }
}
