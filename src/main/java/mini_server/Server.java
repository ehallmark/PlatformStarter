package mini_server;

import j2html.tags.ContainerTag;
import spark.Request;
import spark.Response;

import java.time.*;
import java.util.Timer;
import java.util.concurrent.*;

import static spark.Spark.*;
import static j2html.TagCreator.*;

/**
 * Created by ehallmark on 8/1/17.
 */
public class Server {
    private static final String DEFAULT_URL = "http://35.196.70.117";
    private static final String DEFAULT_ZONE = "us-east1-c";
    private static final String DEFAULT_INSTANCE_NAME = "ai-platform-2";
    private static long lastCheckedTime = 0;
    private static final MonitorTask turnOffTask;
    private static final MonitorTask turnOnTask;
    private static final long MONITOR_PERIOD_MILLIS = 5 * 60 * 1000;
    private static final long TIME_UNTIL_SHUTDOWN_MILLIS = 40 * 60 * 1000;
    private static Future<?> turnOffFuture;
    private static boolean keepOn = false;

    private static ScheduledExecutorService timer;
    static {
        timer = Executors.newSingleThreadScheduledExecutor();
        // monitors
        turnOnTask = new MonitorTask(DEFAULT_URL, DEFAULT_INSTANCE_NAME, DEFAULT_ZONE, true);
        turnOffTask = new MonitorTask(DEFAULT_URL, DEFAULT_INSTANCE_NAME, DEFAULT_ZONE, false);
    }
    public static void main(String[] args) {
        if(args.length>0&&args[0].equals("1")) keepOn=true;
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
        if(now.getHour() < 4+zoneOffset || now.getHour() > 18+zoneOffset) {
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
                if(turnOffFuture != null) {
                    System.out.println("Canceling future...");
                    turnOffFuture.cancel(true);
                    turnOffFuture = null;
                }
            }
            // add more time till shutoff
            if(!keepOn) turnOffFuture = timer.schedule(turnOffTask, TIME_UNTIL_SHUTDOWN_MILLIS, TimeUnit.MILLISECONDS);
            return null;
        });

        post("/", (req,res)->{
            return turnOnAction(req,res);
        });

        get("/alskdhjgoaiseugiauewlkgjadj32la93klva098432jaegasdiga938", (req,res)->{
            return turnOnAction(req,res);
        });

        redirect.get("/secure/home","/");

        get("/", (req,res)->{
            boolean shouldBeOn = shouldBeOn();
            if(!shouldBeOn) {
                return platformNotStarting().render();
            }
            if((System.currentTimeMillis()-lastCheckedTime) < MONITOR_PERIOD_MILLIS) {
                return platformStarting().render();
            }

            return div().with(
                    form().withAction("/").withMethod("POST").with(
                            h5("AI Platform is off."),
                            button("Click to start AI Platform").withType("submit")
                    )
            );
        });
    }

    private static ContainerTag platformStarting() {
        return html().with(
                head().with(
                        title("AI Platform Startup"),
                        meta().attr("http-equiv","refresh").attr("content","10")
                ),body().with(
                        div().with(
                                h4("Platform is starting up now..."),
                                h5("Please check back in a few minutes.")
                        )
                )

        );
    }

    private static Object turnOnAction(Request req, Response res) {
        boolean shouldBeOn = shouldBeOn();
        if(!shouldBeOn) {
            return platformNotStarting().render();
        }

        if((System.currentTimeMillis()-lastCheckedTime) < MONITOR_PERIOD_MILLIS) {
            return platformStarting().render();
        }

        lastCheckedTime = System.currentTimeMillis();
        if(turnOffFuture != null) {
            System.out.println("Canceling future...");
            turnOffFuture.cancel(true);
            turnOffFuture = null;
        }

        timer.schedule(turnOnTask, 0, TimeUnit.MILLISECONDS);
        if(!keepOn) turnOffFuture = timer.schedule(turnOffTask, TIME_UNTIL_SHUTDOWN_MILLIS, TimeUnit.MILLISECONDS);
        return platformStarting().render();
    }

    private static ContainerTag platformNotStarting() {
        return div().with(
                h4("Please check back during business hours.")
        );
    }
}
