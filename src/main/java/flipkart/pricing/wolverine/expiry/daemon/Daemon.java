package flipkart.pricing.wolverine.expiry.daemon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class Daemon implements ControlledThread {
    private static final Logger logger = LoggerFactory.getLogger(Daemon.class);
    public static final int MINUTE = 60 * 1000;
    public static final int MAX_SLEEP_TIME = 30 * 60 * 1000;
    private final int minSleepTime;
    private final int maxSleepTime;
    private final int maxSleepTimeAtError;

    public Daemon(int minSleepTime){
        this(minSleepTime, MAX_SLEEP_TIME);
    }

    public Daemon(int minSleepTime, int maxSleepTime){
        this(minSleepTime, maxSleepTime, MINUTE);
    }

    public Daemon(int minSleepTime, int maxSleepTime, int maxSleepTimeAtError){
        this.minSleepTime = minSleepTime;
        this.maxSleepTime = maxSleepTime;
        this.maxSleepTimeAtError = maxSleepTimeAtError;
    }

    private volatile boolean alive = true;

    public void shutdown(){
        alive = false;
    }


    @Override
    public void run() {
        alive = true;
        long sleepTime = minSleepTime;
        logger.info(" Expiry Daemon started.");
        while(alive){
            try {
                boolean success = work();
                if (!success) {
                    sleepTime *= 2;
                    sleepTime = Math.min(sleepTime, maxSleepTime);
                } else {
                    sleepTime = minSleepTime;
                }
                rest(sleepTime);
            }catch(Exception e){
                logger.error("Expiry daemon failed to work. Retrying after 1 min.", e);
                rest(maxSleepTimeAtError);
            }
        }
        logger.info(" Stopping expiry daemon.");
    }

    protected void rest(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ignore) {
            ignore.printStackTrace();
        }
    }

    protected abstract boolean work() throws Exception;

}
