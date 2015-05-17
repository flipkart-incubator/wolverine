package example;

import flipkart.pricing.wolverine.expiry.daemon.Daemon;

public class PrintMessageInInterval extends Daemon {
    public PrintMessageInInterval(int minSleepTime) {
        super(minSleepTime);
    }

    @Override
    protected boolean work() throws Exception {
        int count = 0;

        while(count++ > 5 && status()){

            System.out.println("I am elected leader.");
        }
        return true;
    }
}
