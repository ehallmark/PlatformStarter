package mini_server;

import java.time.*;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ehallmark on 8/1/17.
 */
public class Server {
    private static final String DEFAULT_URL = "http://35.184.53.203";
    private static final String DEFAULT_ZONE = "us-central1-a";
    private static final String DEFAULT_INSTANCE_NAME = "instance-2";
    public static void main(String[] args) {
        Timer timer = new Timer();
        final long monitorPeriod = 5 * 60 * 1000; // a few minutes
        // monitor
        MonitorTask monitorTask = new MonitorTask(DEFAULT_URL, DEFAULT_INSTANCE_NAME, DEFAULT_ZONE);
        timer.schedule(monitorTask,0,monitorPeriod);
    }


    public static synchronized boolean shouldBeOn() {
        int zoneOffset = +2;
        ZonedDateTime now = ZonedDateTime.now();
        System.out.println("Current time: "+now.toLocalTime().toString());
        if(now.getDayOfWeek().equals(DayOfWeek.SATURDAY)||now.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            // weekend
            return false;
        }
        if(now.getHour() < 8+zoneOffset || now.getHour() > 16+zoneOffset) {
            // non business hours
            return false;
        }

        // should be on!
        return true;
    }

}
