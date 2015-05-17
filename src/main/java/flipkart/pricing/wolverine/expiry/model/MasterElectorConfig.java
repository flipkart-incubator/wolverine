package flipkart.pricing.wolverine.expiry.model;

public class MasterElectorConfig {

    private final String connectPath;

    private int baseSleepTime = 10;

    private int numberOfRetries = 3;

    private final String leaderPath;

    public MasterElectorConfig(String connectPath, String leaderPath) {
        this.connectPath = connectPath;
        this.leaderPath = leaderPath;
    }

    public String zookeeperConnect() {
        return connectPath;
    }

    public int getBaseSleepTime() {
        return baseSleepTime;
    }

    public int getNumberOfRetries() {
        return numberOfRetries;
    }

    public String getLeaderPath() {
        return leaderPath;
    }

    public void setBaseSleepTime(int baseSleepTime) {
        this.baseSleepTime = baseSleepTime;
    }

    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterElectorConfig that = (MasterElectorConfig) o;

        if (baseSleepTime != that.baseSleepTime) return false;
        if (numberOfRetries != that.numberOfRetries) return false;
        if (connectPath != null ? !connectPath.equals(that.connectPath) : that.connectPath != null) return false;
        if (leaderPath != null ? !leaderPath.equals(that.leaderPath) : that.leaderPath != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = connectPath != null ? connectPath.hashCode() : 0;
        result = 31 * result + baseSleepTime;
        result = 31 * result + numberOfRetries;
        result = 31 * result + (leaderPath != null ? leaderPath.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CuratorFrameworkConfig{" +
                "connectPath='" + connectPath + '\'' +
                ", baseSleepTime=" + baseSleepTime +
                ", numberOfRetries=" + numberOfRetries +
                ", leaderPath='" + leaderPath + '\'' +
                '}';
    }
}
