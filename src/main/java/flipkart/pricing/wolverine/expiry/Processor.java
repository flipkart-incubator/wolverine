package flipkart.pricing.wolverine.expiry;

import java.io.IOException;

public interface Processor {

    void process(long marker) throws IOException;

}
