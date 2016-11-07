package flipkart.wolverine.daemon;

import flipkart.wolverine.core.SleepStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
  An implementation of ControlledThread with a specific sleep strategy
*/
public abstract class Daemon implements ControlledThread {

    private static final Logger logger = LoggerFactory.getLogger(Daemon.class);
    private final SleepStrategy sleepStrategy;
    private final String daemonName;

    public Daemon(SleepStrategy sleepStrategy) {
        this.sleepStrategy = sleepStrategy;
        this.daemonName = this.getClass().getSimpleName();
    }

    private volatile boolean alive = true;

    public void shutdown() {
        alive = false;
    }

    protected boolean status() {
        return this.alive;
    }

    @Override
    public void run() {
        alive = true;
        logger.info("Starting {}", daemonName);
        while (alive) {
            WorkStatus workStatus;
            try {
                boolean success = work();
                workStatus = success ? WorkStatus.SUCCESS : WorkStatus.FAILURE;
            } catch (Exception e) {
                logger.warn("Caught exception while running {}: ", daemonName, e);
                workStatus = WorkStatus.EXCEPTION;
            }
            sleepStrategy.sleep(workStatus);
        }
        logger.info("Stopping {}", daemonName);
    }

    /*
        This method should be one unit of work. On completing the work return true or false based on whether the job was successful or not.
        In case while executing the job, the process looses leadership, then we might have 2 parallel jobs running.
        If the use case is critical on 2 jobs running, keep checking status() method to fail fast.
     */
    protected abstract boolean work() throws Exception;

}
