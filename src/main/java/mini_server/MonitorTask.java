package mini_server;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.TimerTask;

/**
 * Created by ehallmark on 8/1/17.
 */
public class MonitorTask extends TimerTask {
    private String url;
    private String instanceName;
    private String zone;
    private boolean turnOn;
    public MonitorTask(String url, String instanceName, String zone, boolean turnOn) {
        this.url = url;
        this.instanceName=instanceName;
        this.zone=zone;
        this.turnOn = turnOn;
    }

    @Override
    public void run() {
        System.out.println("Monitoring...");
        if (turnOn) {
            // check if already on
            System.out.println("Checking if already on...");
            if (!ping()) {
                // turn on
                System.out.println("Not on.");
                try {
                    turnOn();
                } catch(Exception e) {
                    System.out.println("Error turning on: "+e.getMessage());
                }
            } else {
                System.out.println("Yes.");
            }
        } else {
            // check if already off
            System.out.println("Checking if already off...");
            if (ping()) {
                // turn off
                System.out.println("Not off.");
                try {
                    turnOff();
                } catch(Exception e) {
                    System.out.println("Error turning off: "+e.getMessage());
                }
            } else {
                System.out.println("Yes.");
            }
        }
    }

    private void turnOn() throws Exception {
        ProcessBuilder ps = new ProcessBuilder("/bin/bash", "-c", "gcloud compute instances start "+instanceName+" --zone="+zone);
        ps.start();
        System.out.println("Started...");
    }

    private void turnOff() throws Exception {
        ProcessBuilder ps = new ProcessBuilder("/bin/bash", "-c","gcloud compute instances stop "+instanceName+" --zone="+zone);
        ps.start();
        System.out.println("Stopped...");
    }

    public boolean ping() {
        ProcessBuilder ps = new ProcessBuilder("/bin/bash", "-c", "gcloud compute instances describe "+instanceName+" --zone="+zone+" | grep status");
        try {
            Process process = ps.start();
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader processOutput = new BufferedReader(isr);

            String output;
            while ((output = processOutput.readLine()) != null) {
                System.out.println("Result of gcloud compute instances describe: " + output);
                if (output.contains("RUNNING") || output.contains("STAGING") || output.contains("PROVISIONING")) {
                    processOutput.close();
                    return true;
                }
            }

            processOutput.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
