package mini_server;

import java.time.*;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import static spark.Spark.*;

/**
 * Created by ehallmark on 8/1/17.
 */
public class Server {
    private static final String DEFAULT_URL = "http://35.196.70.117";
    private static final String DEFAULT_ZONE = "us-east1-c";
    private static final String DEFAULT_INSTANCE_NAME = "instance-5";

    private static final MonitorTask monitorTask;
    private static final Timer timer;
    static {
        timer = new Timer();
        // monitor
        monitorTask = new MonitorTask(DEFAULT_URL, DEFAULT_INSTANCE_NAME, DEFAULT_ZONE);
    }
    public static void main(String[] args) {
        final long monitorPeriod = 10 * 60 * 1000; // a few minutes
        //timer.schedule(monitorTask,0,monitorPeriod);

        server();

    }


    public static synchronized boolean shouldBeOn() {
        int zoneOffset = +0;
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of( "America/Los_Angeles" ));
        System.out.println("Current time: "+now.toString());
        System.out.println("    Hour: "+now.getHour());
        if(now.getDayOfWeek().equals(DayOfWeek.SATURDAY)||now.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            // weekend
            System.out.println("  Don't run on weekends...");
            return false;
        }
        if(now.getHour() < 9+zoneOffset || now.getHour() > 16+zoneOffset) {
            // non business hours
            System.out.println("  Outside of business hours...");
            return false;
        }

        System.out.println("  Turn me on!");
        // should be on!
        return true;
    }

    private static void server() {
        port(8080);

        get("/", (req,res)->{
           return "Hello World!";
        });
    }
}
