package mini_server;

import j2html.tags.ContainerTag;

import java.time.*;
import java.util.Timer;
import static spark.Spark.*;
import static j2html.TagCreator.*;

/**
 * Created by ehallmark on 8/1/17.
 */
public class Server {
    private static final String DEFAULT_URL = "http://35.196.70.117";
    private static final String DEFAULT_ZONE = "us-east1-c";
    private static final String DEFAULT_INSTANCE_NAME = "instance-5";
    private static long lastCheckedTime = 0;
    private static final MonitorTask turnOffTask;
    private static final MonitorTask turnOnTask;
    private static final long MONITOR_PERIOD_MILLIS = 10 * 60 * 1000;
    private static final long TIME_UNTIL_SHUTDOWN_MILLIS = 50 * 60 * 1000;

    private static Timer timer;
    static {
        timer = new Timer();
        // monitors
        turnOnTask = new MonitorTask(DEFAULT_URL, DEFAULT_INSTANCE_NAME, DEFAULT_ZONE, true);
        turnOffTask = new MonitorTask(DEFAULT_URL, DEFAULT_INSTANCE_NAME, DEFAULT_ZONE, false);
    }
    public static void main(String[] args) {
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
        if(now.getHour() < 7+zoneOffset || now.getHour() > 17+zoneOffset) {
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

        get("/ping", (req,res)->{
            System.out.println("Got a ping from the AI Platform!");
            if(shouldBeOn()) {
                timer.cancel();
                timer = new Timer();
            }
            // add more time till shutoff
            timer.schedule(turnOffTask, TIME_UNTIL_SHUTDOWN_MILLIS, MONITOR_PERIOD_MILLIS);
            return null;
        });

        get("/", (req,res)->{
            boolean shouldBeOn = shouldBeOn();
            if(!shouldBeOn) {
                return platformNotStarting().render();
            }

            if((System.currentTimeMillis()-lastCheckedTime) < MONITOR_PERIOD_MILLIS) {
                return platformStarting().render();
            }

            lastCheckedTime = System.currentTimeMillis();
            timer.cancel();
            timer = new Timer();

            timer.schedule(turnOnTask, 0);
            timer.schedule(turnOffTask, TIME_UNTIL_SHUTDOWN_MILLIS, MONITOR_PERIOD_MILLIS);
            return platformStarting().render();
        });
    }

    private static ContainerTag platformStarting() {
        return div().with(
                h4("Platform is starting up now..."),
                h5("Please check back in a few minutes.")
        );
    }

    private static ContainerTag platformNotStarting() {
        return div().with(
                h4("Please check back during business hours.")
        );
    }
}
