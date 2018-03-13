package hu.daniel.vince.humanmobility.model.entities;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-07-17.
 */

public class UserInfo {

    // region Members

    private boolean hasAccelerometer;
    private boolean hasTemperatureSensor;
    private String deviceInfo;
    private String androidVersion;

    // endregion

    // region Constructors

    public UserInfo(boolean hasAccelerometer,
                    boolean hasTemperatureSensor,
                    String deviceInfo,
                    String androidVersion) {
        setAccelerometer(hasAccelerometer);
        setTemperatureSensor(hasTemperatureSensor);
        setDeviceInfo(deviceInfo);
        setAndroidVersion(androidVersion);
    }

    // endregion

    // region Getters

    public boolean hasAccelerometer() {
        return hasAccelerometer;
    }

    public boolean hasTemperatureSensor() {
        return hasTemperatureSensor;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    // endregion

    // region Setters

    public void setAccelerometer(boolean hasAccelerometer) {
        this.hasAccelerometer = hasAccelerometer;
    }

    public void setTemperatureSensor(boolean hasTemperatureSensor) {
        this.hasTemperatureSensor = hasTemperatureSensor;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    //endregion

    // region JSON

    public String toJson() {
        return
                "{" +
                        "HasAccelerometer: " + "\"" + hasAccelerometer + "\"," +
                        "HasTemperatureSensor:" + "\"" + hasTemperatureSensor + "\"," +
                        "DeviceInfo:" + "\"" + deviceInfo + "\"," +
                        "Version:" + "\"" + androidVersion + "\"," +
                "}";
    }

    // endregion
}
