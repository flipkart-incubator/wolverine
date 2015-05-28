package flipkart.wolverine.core;

import flipkart.wolverine.daemon.Daemon;
import flipkart.wolverine.model.MasterElectorConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Semaphore;

public class MasterElectorTest {


    private TestingCluster testingCluster;
    private CuratorFramework curatorFramework;
    private MasterElectorConfig config;

    @Before
    public void setUp() throws Exception {
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        this.config = new MasterElectorConfig(testingCluster.getConnectString(), "/root");
        this.curatorFramework = getCuratorFramework(config);

    }

    private CuratorFramework getCuratorFramework(MasterElectorConfig config) {
        return  CuratorFrameworkFactory.newClient(
                config.zookeeperConnect(),
                new ExponentialBackoffRetry(
                        config.getBaseSleepTime(),
                        config.getNumberOfRetries()
                ));
    }

    @After
    public void tearDown() throws Exception {
        testingCluster.stop();

    }


    @Test
    public void testShouldCheckIfAJobExecutes() throws Exception {
        final int[] i = new int[1];

        final Object mutex = new Object();

        MasterElector masterElector = new MasterElector(new Daemon(500) {
            @Override
            protected boolean work() throws Exception {
                i[0]++;
                int count = 0;
                while (count++ < 5 && status()) {

                    System.out.println("I am elected leader.");
                }
                synchronized (mutex){
                    mutex.notifyAll();
                }
                return true;
            }
        }, curatorFramework, config);

        masterElector.start();
        synchronized (mutex){
            mutex.wait();
        }
        masterElector.stop();
        Assert.assertTrue(i[0] > 0);
    }

    @Test
    public void testShouldEnsureInCaseOneProcessDiesOtherTakesUp() throws Exception {

        final int[] i = new int[2];

        final Semaphore s = new Semaphore(2);
        s.acquire(2);

        MasterElector masterElector1 = new MasterElector(new Daemon(500) {
            @Override
            protected boolean work() throws Exception {
                i[0]++;
                int count = 0;
                while (count++ < 5 && status()) {

                    System.out.println("I am elected leader-1.");
                }
                s.release();
                shutdown();
                return true;
            }
        }, curatorFramework, config);


        MasterElector masterElector2 = new MasterElector(new Daemon(500) {
            @Override
            protected boolean work() throws Exception {
                i[1]++;
                int count = 0;
                while (count++ < 5 && status()) {

                    System.out.println("I am elected leader-2.");
                }
                s.release();
                shutdown();
                return true;
            }
        }, getCuratorFramework(config), config);

        masterElector1.start();
        masterElector2.start();

        s.acquire(2);

        masterElector1.stop();
        masterElector2.stop();

        s.release(2);


        Assert.assertTrue(i[0] > 0);
        Assert.assertTrue(i[1] > 0);

    }
}