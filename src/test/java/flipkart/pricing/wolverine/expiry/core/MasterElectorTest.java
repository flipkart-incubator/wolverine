package flipkart.pricing.wolverine.expiry.core;

import com.sun.corba.se.impl.orbutil.concurrent.Mutex;
import flipkart.pricing.wolverine.expiry.daemon.Daemon;
import flipkart.pricing.wolverine.expiry.model.MasterElectorConfig;
import org.apache.curator.test.TestingCluster;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.hamcrest.core.Is.is;

public class MasterElectorTest {


    private TestingCluster testingCluster;

    @Before
    public void setUp() throws Exception {
        testingCluster = new TestingCluster(3);
        testingCluster.start();
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
        }, new MasterElectorConfig(testingCluster.getConnectString(), "/root"));

        masterElector.start();
        synchronized (mutex){
            mutex.wait();
        }
        masterElector.stop();
        Assert.assertTrue(i[0] > 0);
    }
}