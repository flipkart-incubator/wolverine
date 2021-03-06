package flipkart.wolverine.core;

import flipkart.wolverine.daemon.ControlledThread;
import flipkart.wolverine.model.MasterElectorConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

/*
    Main starting point of the elector code base. The @MasterElector takes a job and config to connect.
 */

public class MasterElector {

    /*
        The block which executes once process gets leadership handle.
     */
    private final ControlledThread job;

    private final CuratorFramework curatorFramework;
    private final String leaderPath;
    private LeaderSelector leaderSelector;

    public MasterElector(ControlledThread job, MasterElectorConfig config) {
        this(job, getCuratorFramework(config), config);
    }

    public MasterElector(ControlledThread job, CuratorFramework curatorFramework, MasterElectorConfig config) {
        this.job = job;
        this.curatorFramework = curatorFramework;
        this.curatorFramework.start();
        this.leaderPath = config.getLeaderPath() ;
    }

    private static CuratorFramework getCuratorFramework(MasterElectorConfig config) {
        return  CuratorFrameworkFactory.newClient(
                config.zookeeperConnect(),
                new ExponentialBackoffRetry(
                        config.getBaseSleepTime(),
                        config.getNumberOfRetries()
                ));
    }


    /*
        Need to start for becoming part of leader group.
     */

    public void start() throws Exception {
        LeaderSelectorListener listener = new LeaderSelectorListenerJobExecutor(job, leaderSelector);
        leaderSelector = new LeaderSelector(curatorFramework, leaderPath, listener);
        leaderSelector.autoRequeue();
        leaderSelector.start();
    }


    /*
        Need to call this method, when ever the lifecycle of the process is coming to an end. Eg : Managed or ShutdownHooks.
     */

    public void stop() {
        job.shutdown();
        leaderSelector.close();
        curatorFramework.close();
    }

}