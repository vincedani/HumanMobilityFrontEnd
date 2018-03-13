package hu.daniel.vince.humanmobility.model.entities;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-07-17.
 */

@Entity
public class BugReport {

    // region Members

    @Id(autoincrement = true)
    private Long id;

    @Expose
    @SerializedName("Message")
    private String message;

    // endregion

    // region Constructors

    public BugReport(String  message) {
        setMessage(message);
    }

    @Generated(hash = 503339040)
    public BugReport(Long id, String message) {
        this.id = id;
        this.message = message;
    }

    @Generated(hash = 1647080932)
    public BugReport() {
    }

    // endregion

    // region Getters

    public Long getId() {
        return this.id;
    }

    public String getMessage() {
        return message;
    }

    // endregion

    // region Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setMessage(String message) {
        if(message.length() > 299)
            message = message.substring(0, 299);
        this.message = message;
    }

    // endregion

    // region JSON

    public String toJson() {
        return "{ Message: \"" + message + "\" }";
    }

    // endregion
}
