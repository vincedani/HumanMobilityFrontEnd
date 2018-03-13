package hu.daniel.vince.humanmobility.model.handlers.connection;

import java.util.List;

import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.entities.Location;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-15.
 */

public final class LocationConnectionManager  extends BaseConnectionManager {

    // region Members

    private final String LOCATION_TAG = "/api/Locations";

    // endregion

    // region Constructors

    LocationConnectionManager(OkHttpClient client) {
        super(client);
    }

    // endregion

    // region Send locations

    public Response sendLocations(ApplicationUser user, List<Location> locations) {
        String finalUrl = ConnectionHandler.URL.concat(LOCATION_TAG);

        try {
            String fullToken = "Bearer " + user.getToken();

            String json = getGson().toJson(locations);

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
