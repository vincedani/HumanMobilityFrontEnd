package hu.daniel.vince.humanmobility.model.handlers.connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.google.gson.LongSerializationPolicy;

import java.io.IOException;

import hu.daniel.vince.humanmobility.model.converters.JodaTimeConverter;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-15.
 */

class BaseConnectionManager {

    // region Members

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;
    private JodaTimeConverter converter;
    private Gson gson;

    // endregion

    // region Constructor

    BaseConnectionManager(OkHttpClient client) {
        this.client = client;
        this.converter = new JodaTimeConverter();

        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.excludeFieldsWithoutExposeAnnotation();
        gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
        gsonBuilder.registerTypeAdapter(Long.class,
                (JsonSerializer<Long>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(converter.convertToEntityProperty(src).toString()));

        this.gson = gsonBuilder.create();
    }

    // endregion

    // region HTTP Requests

    Response ExecuteHttpRequest(Request request) {
        Response response = null;

        try {
            response = client.newCall(request).execute();

        } catch (Exception e) {
            DatabaseHandler.getInstance(ConnectionHandler.getContext())
                    .addBugReport(e.getMessage());
        }
        return response;
    }

    void AsyncHttpRequest(Request request, ConnectionHandler.ConnectionCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(!response.isSuccessful())
                    callback.onFailure(response.message());

                callback.onSuccess(response);
            }
        });
    }

    // endregion


    // region Json parser

    Gson getGson() {
        return gson;
    }

    // endregion
}
