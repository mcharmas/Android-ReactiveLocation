package pl.charmas.android.reactivelocation.trace;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 10/17/14
 * Time: 1:54 PM
 * developer STANIMIR MARINOV
 */
public class Tracer {
    private long startTime;
    private long previousStayTime;
    private long stayTime;
    private double km;

    public Tracer() {
        set();
    }

    public void set() {
        startTime = new Date().getTime();
        stayTime = 0L;
        previousStayTime = 0L;
        km = 0.0;
    }

    public void reset() {
        set();
    }

    public void onMove(double km) {
        this.km += km;
    }

    public void onStop() {
        long now = new Date().getTime();
        if (previousStayTime > 0L) stayTime += now - previousStayTime;
        previousStayTime = now;
    }

    /**
     * @return delta time in ms
     */
    public long calcTime() {
        return new Date().getTime() - startTime;
    }

    /**
     * @return stay time in ms
     */
    public long getStayTime() {
        return stayTime;
    }

    /**
     * @return run in km
     */
    public double getRun() {
        return this.km;
    }

    /**
     * @return average speed in km/h
     */
    public double getAverageSpeed() {
        double timeHours = ((double) (calcTime() - getStayTime()) / 1000) / 60 / 60;
        return this.km / timeHours;
    }
}
