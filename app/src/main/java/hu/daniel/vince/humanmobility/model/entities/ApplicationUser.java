package hu.daniel.vince.humanmobility.model.entities;

import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;


/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-07-17.
 */

@Entity
public class ApplicationUser {

    // region Members

    @Id(autoincrement = true)
    private Long id;

    @Unique
    @SerializedName("UserName")
    private String userName;
    
    @SerializedName("Token")
    private String token;

    @SerializedName("IsLoggedIn")
    private boolean isLoggedIn;

    // endregion

    // region Constructors

    public ApplicationUser(String userName, String token) {
        setUserName(userName);
        setToken(token);
    }


    @Generated(hash = 329932531)
    public ApplicationUser(Long id, String userName, String token,
            boolean isLoggedIn) {
        this.id = id;
        this.userName = userName;
        this.token = token;
        this.isLoggedIn = isLoggedIn;
    }


    @Generated(hash = 1276351140)
    public ApplicationUser() {
    }


    // endregion

    // region Getters

    public Long getId() {
        return this.id;
    }

    public String getUserName() {
        return userName;
    }

    public String getToken() {
        return token;
    }

    public boolean getIsLoggedIn() {
        return this.isLoggedIn;
    }

    // endregion

    // region Setters

    public void setUserName(String userName) {
        this.userName = userName.trim();
    }

    public void setToken(String token) {
        this.token = token.trim();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIsLoggedIn(boolean isLoggedIn) {
        this.isLoggedIn = isLoggedIn;
    }

    // endregion
}
