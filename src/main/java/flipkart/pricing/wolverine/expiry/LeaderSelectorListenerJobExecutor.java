package flipkart.pricing.wolverine.expiry;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.recipes.leader.LeaderSelector;
import com.netflix.curator.framework.recipes.leader.LeaderSelectorListener;
import com.netflix.curator.framework.state.ConnectionState;
import com.sun.corba.se.impl.orbutil.concurrent.ReentrantMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LeaderSelectorListenerJobExecutor implements LeaderSelectorListener {

    private static final Logger logger = LoggerFactory.getLogger(LeaderSelectorListenerJobExecutor.class);

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
        job.restart();
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
