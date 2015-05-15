package flipkart.pricing.wolverine.expiry;

public interface ControlledThread extends Runnable {

    void shutdown();

    void restart();

}
