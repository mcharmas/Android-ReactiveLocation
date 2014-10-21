package pl.charmas.android.reactivelocation.trace;

import android.location.Location;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 10/17/14
 * Time: 1:53 PM
 * developer STANIMIR MARINOV
 */
public class TraceRoute {
    protected static TraceRoute instance;
    private static final Object mutex = new Object();
    protected ConcurrentHashMap<String, Tracer> mTraceMap = new ConcurrentHashMap<String, Tracer>();
    private Location lastLocation;

    public static TraceRoute getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) instance = new TraceRoute();
            }
        }
        return instance;
    }

    public void startTrace(String key) {
        if (mTraceMap.containsKey(key)) mTraceMap.get(key).reset();
        else mTraceMap.put(key, new Tracer());
    }

    public Tracer stopTrace(String key) {
        if (mTraceMap.containsKey(key)) return mTraceMap.get(key);
        return null;
    }

    /**
     * with default 1 meter collisions in location (accepted distance between Move and Stay state)
     * @param key for tracker
     * @param location geo north/east
     */
    public void setTraceRun(String key, Location location) {
        setTraceRun(key, location, 0.001);
    }

    public void setTraceRun(String key, Location location, double collisions) {
        if (lastLocation != null) {
            double distance = GISTool.getDistance(location.getLongitude(), lastLocation.getLongitude(), location.getLatitude(), lastLocation.getLatitude());
            if (distance > collisions) {
                mTraceMap.get(key).onMove(distance);
            } else {  //not move
                mTraceMap.get(key).onStop();
            }
        }
        lastLocation = location;
    }
}
