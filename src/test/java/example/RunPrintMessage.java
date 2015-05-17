package example;

import flipkart.pricing.wolverine.expiry.core.MasterElector;
import flipkart.pricing.wolverine.expiry.model.MasterElectorConfig;
import org.apache.curator.test.TestingCluster;

import java.io.IOException;

public class RunPrintMessage {

    private static final TestingCluster cluster = new TestingCluster(1);




    public static void main(String[] args) {

        try {
            cluster.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        MasterElector masterElector = new MasterElector(new PrintMessageInInterval(5000), new MasterElectorConfig(cluster.getConnectString(), "/tmp/foo"));

        try {
            masterElector.start();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            masterElector.stop();
            try {
                cluster.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
