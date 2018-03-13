package hu.daniel.vince.humanmobility.model.handlers.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-15.
 */

public class ConnectionHandler {

    // region Nested interface

    public interface ConnectionCallback {
        void onFailure(String error);
        void onSuccess(Response response);
    }

    // endregion

    // region Members

    static final String URL = "http://humanmobility.azurewebsites.net";

    private static volatile ConnectionHandler instance;
    private static Context context;

    private AccountConnectionManager accountManager;
    private ActigraphyConnectionManager actigraphyManager;
    private LocationConnectionManager locationManager;
    private PlaceConnectionManager placeManager;
    private ReportConnectionManager reportManager;

    // endregion

    // region Constructors

    private ConnectionHandler(final Context context) {
        ConnectionHandler.context = context;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .build();

        accountManager = new AccountConnectionManager(client);
        actigraphyManager = new ActigraphyConnectionManager(client);
        locationManager = new LocationConnectionManager(client);
        placeManager = new PlaceConnectionManager(client);
        reportManager = new ReportConnectionManager(client);
    }

    // endregion

    // region Instance

    public static ConnectionHandler getInstance(final Context context) {
        if(instance == null)
            synchronized (ConnectionHandler.class) {
                if(instance == null)
                    instance = new ConnectionHandler(context);
            }
        return instance;
    }

    // endregion

    // region Getters

    public AccountConnectionManager getAccountManager() {
        return accountManager;
    }

    public ActigraphyConnectionManager getActigraphyManager() {
        return actigraphyManager;
    }

    public LocationConnectionManager getLocationManager() {
        return locationManager;
    }

    public PlaceConnectionManager getPlaceManager() {
        return placeManager;
    }

    public ReportConnectionManager getReportManager() {
        return reportManager;
    }

    public static String getTermsUrl() {
        return URL.concat("/Home/Terms");
    }

    static Context getContext() {
        return context;
    }

    // endregion

    // region Connection status

    public boolean isConnected() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        return  info != null && info.isConnected();
    }

    public boolean hasWifiConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if(info != null && info.isConnected()) {
            return info.getType() == ConnectivityManager.TYPE_WIFI;
        }

        return false;
    }

    // endregion
}
