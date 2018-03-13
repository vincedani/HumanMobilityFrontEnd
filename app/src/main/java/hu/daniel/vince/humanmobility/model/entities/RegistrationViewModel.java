package hu.daniel.vince.humanmobility.model.entities;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-07-17.
 */

public class RegistrationViewModel {

    // region Members

    private String userName;
    private String password;
    private String confirmPassword;

    // endregion

    // region Constructors

    RegistrationViewModel(String userName, String password, String confirmPassword) {
        setUserName(userName);
        setPassword(password);
        setConfirmPassword(confirmPassword);
    }

    // endregion

    // region Instance creation

    public static RegistrationViewModel create(String userName,
                                               String password, String confirmPassword) {
        if(!password.equals(confirmPassword))
            return null;

        return new RegistrationViewModel(userName, password, confirmPassword);
    }

    public static RegistrationViewModel create(ApplicationUserViewModel user) {
        return create(user.getUserName(), user.getPassword(), user.getPassword());
    }

    // endregion

    // region Setters

    public void setUserName(String userName) {
        this.userName = userName.trim();
    }

    public void setPassword(String password) {
        this.password = password.trim();
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword.trim();
    }

    // endregion

    // region JSON

    public String toJson() {
        return
                "{" +
                        "UserName: " + "\"" + userName + "\"," +
                        "Password:" + "\"" + password + "\"," +
                        "ConfirmPassword:" + "\"" + confirmPassword + "\"," +
                "}";
    }

    // endregion
}
