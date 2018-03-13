package hu.daniel.vince.humanmobility.model.handlers.connection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.entities.Place;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-15.
 */

public final class PlaceConnectionManager  extends BaseConnectionManager {

    // region Members

    private final String PLACE_TAG = "/api/Place";

    // endregion

    // region Constructors

    PlaceConnectionManager(OkHttpClient client) {
        super(client);
    }

    // endregion

    // region Send place

    public void sendPlace(ApplicationUser user,
                             Place place,
                             ConnectionHandler.ConnectionCallback callback) {
        String finalUrl = ConnectionHandler.URL.concat(PLACE_TAG);
        String fullToken = "Bearer " + user.getToken();

        RequestBody body = RequestBody.create(JSON, place.toJson());
        Request request = new Request.Builder()
                .url(finalUrl)
                .header("Authorization", fullToken)
                .post(body)
                .build();

        AsyncHttpRequest(request, callback);
    }

    // endregion

    // region Get places

    public List<Place> getPlaces(ApplicationUser user) {
        String finalUrl = ConnectionHandler.URL.concat(PLACE_TAG);
        String fullToken = "Bearer " + user.getToken();
        List<Place> places = new ArrayList<>();

        Request request = new Request.Builder()
                .url(finalUrl)
                .header("Authorization", fullToken)
                .build();

        Response response = ExecuteHttpRequest(request);

        if(response != null) {
            try {
                if(response.code() == HttpsURLConnection.HTTP_OK) {
                    JSONArray responseArray = new JSONArray(response.body().string());

                    for(int i = 0; i < responseArray.length(); i++) {
                        JSONObject  json = responseArray.getJSONObject(i);
                        places.add(new Place(
                                json.getString("Type"),
                                json.getString("Title"),
                                json.getDouble("Latitude"),
                                json.getDouble("Longitude"),
                                json.getInt("Radius")
                        ));
                    }
                }

            } catch (Exception e) {
                DatabaseHandler.getInstance(ConnectionHandler.getContext())
                        .addBugReport(e.getMessage());
            }
        }
        return places;
    }

    // endregion

    // region Delete place

    public void deletePlace(ApplicationUser user, Place place) {
        String finalUrl = ConnectionHandler.URL.concat(PLACE_TAG);
        String fullToken = "Bearer " + user.getToken();

        RequestBody body = RequestBody.create(JSON, place.toJson());
        Request request = new Request.Builder()
                .url(finalUrl)
                .header("Authorization", fullToken)
                .delete(body)
                .build();

        ExecuteHttpRequest(request);
    }

    // endregion
}
