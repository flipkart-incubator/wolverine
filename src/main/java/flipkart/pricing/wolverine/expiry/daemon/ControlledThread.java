package flipkart.pricing.wolverine.expiry.daemon;
/*

    The Thread should have all logic what user need to execute once it becomes leader.

 */
public interface ControlledThread extends Runnable {

    void shutdown();

}
