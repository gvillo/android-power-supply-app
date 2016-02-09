package villo.com.ar.powersupplyapp.model;

/**
 * Created by villo on 18/1/16.
 */
public class UpsValues {
    private String name;
    private String status;
    private String charge;
    private String voltage;
    private String usagePercentage;
    private String temperature;
    private String remainingTime;
    private String lastUpdate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCharge() {
        return charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public String getVoltage() {
        return voltage;
    }

    public void setVoltage(String voltage) {
        this.voltage = voltage;
    }

    public String getUsagePercentage() {
        return usagePercentage;
    }

    public void setUsagePercentage(String usagePercentage) {
        this.usagePercentage = usagePercentage;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(String remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
