package library.external;

/**
 * Created by uengine on 2020. 4. 18..
 */
public class KioskServiceFallback implements KioskService {
    @Override
    public void selfRental(Kiosk kiosk) {
        //do nothing if you want to forgive it

        System.out.println("Circuit breaker has been opened. Fallback returned instead.");
    }
}
