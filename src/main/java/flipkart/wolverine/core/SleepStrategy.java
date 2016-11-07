package flipkart.wolverine.core;

import flipkart.wolverine.daemon.WorkStatus;

public interface SleepStrategy {
    void sleep(WorkStatus workStatus);
}
