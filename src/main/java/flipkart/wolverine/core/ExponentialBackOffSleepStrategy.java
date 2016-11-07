package flipkart.wolverine.core;

import flipkart.wolverine.daemon.WorkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExponentialBackOffSleepStrategy implements SleepStrategy {

    // time to wait after one successful invocation.
    private final long minSleepTime;

    // max time of exponential back off.
    private final long maxSleepTime;

    // time to retry after an error occurred.
    private final long sleepTimeAtException;

    private long currentSleepTime;

    //constants
    private static final Logger logger = LoggerFactory.getLogger(ExponentialBackOffSleepStrategy.class);
    private static final int MINUTE = 60 * 1000;
    private static final int MAX_SLEEP_TIME = 30 * 60 * 1000;

    public ExponentialBackOffSleepStrategy(long minSleepTime) {
        this(minSleepTime, MAX_SLEEP_TIME, MINUTE);
    }

    public ExponentialBackOffSleepStrategy(long minSleepTime, long maxSleepTime, long sleepTimeAtException) {
        this.minSleepTime = minSleepTime;
        this.maxSleepTime = maxSleepTime;
        this.sleepTimeAtException = sleepTimeAtException;
        this.currentSleepTime = minSleepTime;
    }

    @Override
    public void sleep(WorkStatus workStatus) {
        try {
            switch (workStatus) {
                case SUCCESS:
                    currentSleepTime = minSleepTime;
                    break;
                case FAILURE:
                    currentSleepTime <<= 1;
                    currentSleepTime = Math.min(currentSleepTime, maxSleepTime);
                    break;
                case EXCEPTION:
                    currentSleepTime = sleepTimeAtException;
                    break;
                default:
                    currentSleepTime = minSleepTime;
                    break;
            }
            logger.debug("Sleeping for {} ms", currentSleepTime);
            Thread.sleep(currentSleepTime);
        } catch (InterruptedException ignored) {
            logger.warn("Caught exception while sleeping ", ignored);
        }
    }

    public long getCurrentSleepTime() {
        return currentSleepTime;
    }
}
