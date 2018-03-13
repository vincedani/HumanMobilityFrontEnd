package hu.daniel.vince.humanmobility.model.handlers.connection;

import java.util.List;

import hu.daniel.vince.humanmobility.model.entities.ActivityLevel;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-09-30.
 */

public class ActigraphyConnectionManager extends BaseConnectionManager {

    // region Members

    private final String ACTIGRAPHY_TAG = "/api/Actigraphy";

    // endregion

    // region Constructor

    public ActigraphyConnectionManager(OkHttpClient client) {
        super(client);
    }

    // endregion

    // region Send accelerometer data

    public Response sendActivities(ApplicationUser user, List<ActivityLevel> activities) {
        String finalUrl = ConnectionHandler.URL.concat(ACTIGRAPHY_TAG);

        try {
            String fullToken = "Bearer " + user.getToken();

            String json = getGson().toJson(activities);

            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(finalUrl)
                    .header("Authorization", fullToken)
                    .post(body)
                    .build();

            return ExecuteHttpRequest(request);

        } catch (Exception e) {
            DatabaseHandler.getInstance(ConnectionHandler.getContext())
                    .addBugReport(e.getMessage());
        }
        return null;
    }

    // endregion

}
