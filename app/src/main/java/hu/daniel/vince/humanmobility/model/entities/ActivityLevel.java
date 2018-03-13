package hu.daniel.vince.humanmobility.model.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-09-18.
 */

@Entity
public class ActivityLevel {

    // region Members

    @Expose
    @Id(autoincrement = false)
    @SerializedName("SaveTime")
    private Long time;

    @Expose
    @SerializedName("X")
    private float x;

    @Expose
    @SerializedName("Y")
    private float y;

    @Expose
    @SerializedName("Z")
    private float z;

    private boolean isDeleted;

    // endregion

    // region Constructors

    public ActivityLevel(Long time, float x, float y, float z) {
        this.time = time;
        this.x = x;
        this.y = y;
        this.z = z;
        this.isDeleted = false;
    }

    @Generated(hash = 317289115)
    public ActivityLevel(Long time, float x, float y, float z, boolean isDeleted) {
        this.time = time;
        this.x = x;
        this.y = y;
        this.z = z;
        this.isDeleted = isDeleted;
    }

    @Generated(hash = 1071008281)
    public ActivityLevel() {
    }

    // endregion

    // region Getters

    public Long getTime() {
        return time;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public boolean getIsDeleted() {
        return this.isDeleted;
    }

    // endregion

    // region Setters

    public void setTime(Long time) {
        this.time = time;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    // endregion
}
