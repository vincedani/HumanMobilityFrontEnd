package hu.daniel.vince.humanmobility.model.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-07-17.
 */

@Entity
public class Location {

    // region Members

    @Expose
    @SerializedName("Accuary")
    private float accuracy;

    @Expose
    @SerializedName("Error")
    private boolean error;

    @Expose
    @SerializedName("Latitude")
    private double latitude;

    @Expose
    @SerializedName("Longitude")
    private double longitude;

    @Expose
    @SerializedName("DetectionTime")
    private Long detectionTime;

    @Id(autoincrement = false)
    @Expose
    @Unique
    @SerializedName("SaveTime")
    private Long saveTime;

    // endregion

    // region Constructors

    @Keep
    public Location(float accuracy,
                    boolean error,
                    double latitude,
                    double longitude,
                    Long detectionTime,
                    Long saveTime) {
        setAccuracy(accuracy);
        setError(error);
        setDetectionTime(detectionTime);
        setLatitude(latitude);
        setLongitude(longitude);
        setSaveTime(saveTime);
    }

    @Generated(hash = 375979639)
    public Location() {
    }

    // endregion

    // region Getters

    public float getAccuracy() {
        return accuracy;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Long getDetectionTime() {
        return detectionTime;
    }

    public Long getSaveTime() {
        return saveTime;
    }

    public boolean getError() {
        return this.error;
    }

    //endregion

    // region Setters

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void setLatitude(double latitude) throws IllegalArgumentException {
        if(latitude < 0 || latitude > 90)
            throw new IllegalArgumentException("The Latitude value should be between 0 and 90.");

        this.latitude = latitude;
    }

    public void setLongitude(double longitude) throws IllegalArgumentException {
        if(longitude < 0 || longitude > 90)
            throw new IllegalArgumentException("The Longitude value should be between 0 and 90.");

        this.longitude = longitude;
    }

    public void setDetectionTime(Long detectionTime) {
        this.detectionTime = detectionTime;
    }

    public void setSaveTime(Long saveTime) {
        this.saveTime = saveTime;
    }

    // endregion
}
