package hu.daniel.vince.humanmobility.model.handlers.connection;

import android.content.Context;
import android.support.annotation.Nullable;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

import hu.daniel.vince.humanmobility.model.entities.ApplicationUserViewModel;
import hu.daniel.vince.humanmobility.model.entities.RegistrationViewModel;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.entities.UserInfo;
import hu.daniel.vince.humanmobility.model.handlers.scheduling.SchedulingHandler;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-15.
 */

public final class AccountConnectionManager extends BaseConnectionManager {

    // region Members

    private final String REGISTRATION_TAG = "/api/Account/Register";
    private final String USER_INFO_TAG = "/api/UserInfo";
    private final String TOKEN_TAG = "/Token";

    // endregion

    // region Constructors

    AccountConnectionManager(OkHttpClient client) {
        super(client);
    }

    // endregion

    // region Token

    private Request getTokenRequest(ApplicationUserViewModel user) {
        String finalUrl = ConnectionHandler.URL.concat(TOKEN_TAG);

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "password")
                .add("username", user.getUserName())
                .add("password", user.getPassword())
                .build();

        return new Request.Builder()
                .url(finalUrl)
                .post(formBody)
                .build();
    }

    private void getTokenAsync(ApplicationUserViewModel user,
                               ConnectionHandler.ConnectionCallback callback) {
        AsyncHttpRequest(getTokenRequest(user), callback);
    }

    // endregion

    // region RegistrationViewModel

    public void register(RegistrationViewModel registrationViewModelEntity,
                            ConnectionHandler.ConnectionCallback callback) {
        String finalUrl = ConnectionHandler.URL.concat(REGISTRATION_TAG);

        RequestBody body = RequestBody.create(JSON, registrationViewModelEntity.toJson());
        Request request = new Request.Builder()
                .url(finalUrl)
                .post(body)
                .build();

        AsyncHttpRequest(request, callback);
    }

    // endregion

    // region User Info

    @Nullable
    public UserInfo getUserInfo(ApplicationUser user) {
        String finalUrl = ConnectionHandler.URL.concat(USER_INFO_TAG);
        String fullToken = "Bearer " + user.getToken();

        Request request = new Request.Builder()
                .url(finalUrl)
                .header("Authorization", fullToken)
                .build();

        Response response = ExecuteHttpRequest(request);

        if(response != null) {
            try {
                if(response.code() == HttpsURLConnection.HTTP_OK) {
                    JSONObject responseObj = new JSONObject(response.body().string());

                    return new UserInfo(
                            responseObj.getBoolean("HasAccelerometer"),
                            responseObj.getBoolean("HasTemperatureSensor"),
                            responseObj.getString("DeviceInfo"),
                            responseObj.getString("Version")
                    );
                }

            } catch (Exception e) { /* No information in central database. */ }
        }

        return null;
    }

    public boolean sendUserInfo(ApplicationUser user, UserInfo info) {
        String finalUrl = ConnectionHandler.URL.concat(USER_INFO_TAG);
        String fullToken = "Bearer " + user.getToken();

        try {
            JSONObject json = new JSONObject()
                    .put("HasAccelerometer", info.hasAccelerometer())
                    .put("HasTemperatureSensor", info.hasTemperatureSensor())
                    .put("DeviceInfo", info.getDeviceInfo())
                    .put("Version", info.getAndroidVersion());

            RequestBody body = RequestBody.create(JSON, json.toString());
            Request request = new Request.Builder()
                    .url(finalUrl)
                    .header("Authorization", fullToken)
                    .post(body)
                    .build();

            Response response = ExecuteHttpRequest(request);

            if(response != null)
                if(response.code() == HttpsURLConnection.HTTP_OK)
                    return true;

        } catch (Exception e) {
            DatabaseHandler.getInstance(ConnectionHandler.getContext())
                    .addBugReport(e.getMessage());
        }

        return false;
    }

    // endregion

    // region Authentication

    public void authenticateUserAsync(ApplicationUserViewModel user,
                                      ConnectionHandler.ConnectionCallback callback) {

        getTokenAsync(user, callback);

    }

    public void logout(Context context) {
        DatabaseHandler.getInstance(ConnectionHandler.getContext()).cleanDatabase();
        SchedulingHandler.getInstance(context).stopUploader();
        SchedulingHandler.getInstance(context).stopRecordSaver();
    }

    // endregion
}
