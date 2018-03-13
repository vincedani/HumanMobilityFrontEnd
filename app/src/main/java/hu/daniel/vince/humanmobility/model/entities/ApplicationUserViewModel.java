package hu.daniel.vince.humanmobility.model.entities;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-09-29.
 */

public class ApplicationUserViewModel {

    // region Members

    private String userName;

    private String password;

    // endregion

    // region Constructors

    public ApplicationUserViewModel(String userName, String password) {
        setUserName(userName);
        setPassword(password);
    }

    // endregion

    // region Getters

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    // endregion

    // region Setters

    public void setUserName(String userName) {
        this.userName = userName.trim();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // endregion
}
