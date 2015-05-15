package flipkart.pricing.wolverine.expiry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class Daemon implements ControlledThread {
    private static final Logger logger = LoggerFactory.getLogger(Daemon.class);
    public static final int MINUTE = 60 * 1000;
    private final int minSleepTime;


    public Daemon(int minSleepTime){
        this.minSleepTime = minSleepTime;
    }

    private volatile boolean alive = true;

    public void shutdown(){
        alive = false;
    }

    public void restart(){
        alive = true;
    }

    @Override
    public void run() {
        long sleepTime = minSleepTime;
        logger.info(" Expiry Daemon started.");
        while(alive){
            try {
                boolean success = work();
                if (!success) {
                    sleepTime *= 2;
                    sleepTime = Math.min(sleepTime, 30 * 60 * 1000);
                } else {
                    sleepTime = minSleepTime;
                }
                rest(sleepTime);
            }catch(Exception e){
                logger.error("Expiry daemon failed to work. Retrying after 1 min.", e);
                rest(MINUTE);
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
