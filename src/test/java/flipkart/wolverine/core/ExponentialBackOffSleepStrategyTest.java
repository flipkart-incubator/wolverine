package flipkart.wolverine.core;

import flipkart.wolverine.daemon.WorkStatus;
import org.junit.Assert;
import org.junit.Test;

public class ExponentialBackOffSleepStrategyTest {

    @Test
    public void shouldSleepForMinTimeIfSuccess() {
        ExponentialBackOffSleepStrategy strategy = new ExponentialBackOffSleepStrategy(10);
        strategy.sleep(WorkStatus.SUCCESS);
        Assert.assertTrue(strategy.getCurrentSleepTime() == 10);
    }

    @Test
    public void shouldBackOffIfError() {
        ExponentialBackOffSleepStrategy strategy = new ExponentialBackOffSleepStrategy(10, 40, 100);
        strategy.sleep(WorkStatus.FAILURE);
        Assert.assertTrue(strategy.getCurrentSleepTime() == 20);
        strategy.sleep(WorkStatus.FAILURE);
        Assert.assertTrue(strategy.getCurrentSleepTime() == 40);
        strategy.sleep(WorkStatus.FAILURE);
        //since max sleep time is crossed, current sleep time won't change
        Assert.assertTrue(strategy.getCurrentSleepTime() == 40);
    }

    @Test
    public void shouldSleepIfException() {
        ExponentialBackOffSleepStrategy strategy = new ExponentialBackOffSleepStrategy(10, 40, 100);
        strategy.sleep(WorkStatus.EXCEPTION);
        Assert.assertTrue(strategy.getCurrentSleepTime() == 100);
    }

}