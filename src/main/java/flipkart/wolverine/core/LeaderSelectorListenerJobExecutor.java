package flipkart.wolverine.core;

import com.sun.corba.se.impl.orbutil.concurrent.ReentrantMutex;
import flipkart.wolverine.daemon.ControlledThread;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LeaderSelectorListenerJobExecutor implements LeaderSelectorListener {

    private static final Logger logger = LoggerFactory.getLogger(LeaderSelectorListenerJobExecutor.class);

    /*
        The execution which gets handle once the process gets leadership.
     */
    private final ControlledThread job;

    private final LeaderSelector leaderSelector;

    private final ReentrantMutex mutex = new ReentrantMutex();

    public LeaderSelectorListenerJobExecutor(ControlledThread job, LeaderSelector leaderSelector) {
        this.job = job;
        this.leaderSelector = leaderSelector;
    }

    public void takeLeadership(CuratorFramework client) throws Exception {
        logger.info(" got elected. ");
        mutex.acquire();
        job.run();
        mutex.release();
        logger.info(" leaving leadership. ");
    }

    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        logger.debug("newState = " + newState);
        if(newState == ConnectionState.LOST || newState == ConnectionState.SUSPENDED ){
            job.shutdown();
            try {
                mutex.acquire();
            } catch (InterruptedException ignored) {
            }finally {
                mutex.release();
            }
        }
        if(newState == ConnectionState.RECONNECTED){
            leaderSelector.requeue();
        }
    }
}
