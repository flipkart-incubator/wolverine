package flipkart.pricing.wolverine.expiry.core;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.framework.recipes.leader.LeaderSelector;
import com.netflix.curator.framework.recipes.leader.LeaderSelectorListener;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import flipkart.pricing.wolverine.expiry.daemon.ControlledThread;
import flipkart.pricing.wolverine.expiry.model.CuratorFrameworkConfig;

public class MasterElector {

    private final ControlledThread job;
    private final CuratorFramework curatorFramework;
    private final String leaderPath;

    private LeaderSelector leaderSelector;


    public MasterElector(ControlledThread job, CuratorFrameworkConfig config) {
        this.job = job;
        this.curatorFramework = CuratorFrameworkFactory.newClient(
                        config.zookeeperConnect(),
                        new ExponentialBackoffRetry(
                                config.getBaseSleepTime(),
                                config.getNumberOfRetries()
                        ));
        this.curatorFramework.start();
        this.leaderPath = config.getLeaderPath();
    }

    public void start() throws Exception {
        LeaderSelectorListener listener = new LeaderSelectorListenerJobExecutor(job, leaderSelector);
        leaderSelector = new LeaderSelector(curatorFramework, leaderPath, listener);
        leaderSelector.autoRequeue();
        leaderSelector.start();
    }


    public void stop() {
        job.shutdown();
        leaderSelector.close();
        curatorFramework.close();
    }

}