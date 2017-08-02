package mini_server;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.TimerTask;

/**
 * Created by ehallmark on 8/1/17.
 */
public class StartUpTask extends TimerTask {
    @Override
    public void run() {
        if(LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY) || LocalDate.now().getDayOfWeek().equals(DayOfWeek.SATURDAY)) {
            System.out.println("Not running on the weekend...");
            return;
        }
        // Weekday
        System.out.println("Server should start soon...");
        Server.keepOn.set(true);
    }
}
