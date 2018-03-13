package hu.daniel.vince.humanmobility.model.entities;

import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;
import org.joda.time.DateTime;

import hu.daniel.vince.humanmobility.model.converters.JodaTimeConverter;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-25.
 */

@Entity
public class Overview {

    // region Members

    @Id(autoincrement = true)
    private Long id;

    private int success;

    private int error;

    @Convert(converter = JodaTimeConverter.class, columnType = Long.class)
    @Unique
    @SerializedName("SaveTime")
    private DateTime date;

    // endregion

    // region Constructors

    public Overview(int success, int error, DateTime date) {
        this.success = success;
        this.error = error;
        this.date = date;
    }

    public  Overview() { }

    @Generated(hash = 1740124992)
    public Overview(Long id, int success, int error, DateTime date) {
        this.id = id;
        this.success = success;
        this.error = error;
        this.date = date;
    }

    // endregion

    // region Getters

    public Long getId() {
        return id;
    }

    public int getSuccess() {
        return success;
    }

    public int getError() {
        return error;
    }

    // endregion

    // region Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public void setError(int error) {
        this.error = error;
    }

    public DateTime getDate() {
        return this.date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    // endregion
}
