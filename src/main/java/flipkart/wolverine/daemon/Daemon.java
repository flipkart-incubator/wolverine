package flipkart.wolverine.daemon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
    An implementation of ControlledThread this manages sleep interval, exponential back off etc. User need to implement work method.
 */
public abstract class Daemon implements ControlledThread {
    private static final Logger logger = LoggerFactory.getLogger(Daemon.class);
    public static final int MINUTE = 60 * 1000;
    public static final int MAX_SLEEP_TIME = 30 * 60 * 1000;

    // time to wait after one successful invocation.
    private final int minSleepTime;

    // max time of exponential back off.
    private final int maxSleepTime;

    // time to retry after an error occurred.
    private final int maxSleepTimeAtError;
    private final String daemonName;

    public Daemon(int intervalOfJobs){
        this(intervalOfJobs, MAX_SLEEP_TIME);
    }

    public Daemon(int minSleepTime, int maxSleepTime){
        this(minSleepTime, maxSleepTime, MINUTE);
    }

    public Daemon(int minSleepTime, int maxSleepTime, int maxSleepTimeAtError){
        this.minSleepTime = minSleepTime;
        this.maxSleepTime = maxSleepTime;
        this.maxSleepTimeAtError = maxSleepTimeAtError;
        this.daemonName = this.getClass().getSimpleName();
    }

    private volatile boolean alive = true;

    public void shutdown(){
        alive = false;
    }

    protected boolean status(){
        return this.alive;
    }

    @Override
    public void run() {
        alive = true;
        long sleepTime = minSleepTime;
        logger.info(daemonName+" started.");
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
                logger.error(daemonName+" failed to work. Retrying after "+maxSleepTimeAtError/MINUTE+" minutes.", e);
                rest(maxSleepTimeAtError);
            }
        }
        logger.info("Stopping "+daemonName);
    }

    protected void rest(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ignore) {
            ignore.printStackTrace();
        }
    }

    /*
        This method should be one unit of work. On completing the work return true or false based on wheather the job was successfull or not.
        In case while executing the job, the process. Looses leadership, then we might have 2 parrallel jobs running. If the use case is critical on 2 jobs running, keep checking status() method to fail fast.
     */

    protected abstract boolean work() throws Exception;

}
