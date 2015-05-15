package flipkart.pricing.wolverine.expiry;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.recipes.leader.LeaderSelector;
import com.netflix.curator.framework.recipes.leader.LeaderSelectorListener;

public class MasterElector {

    private final ControlledThread job;
    private final CuratorFramework curatorFramework;
    private final String leaderPath;

    private LeaderSelector leaderSelector;


    public MasterElector(ControlledThread job, CuratorFramework curatorFramework, String leaderPath) {
        this.job = job;
        this.curatorFramework = curatorFramework;
        this.curatorFramework.start();
        this.leaderPath = leaderPath;
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