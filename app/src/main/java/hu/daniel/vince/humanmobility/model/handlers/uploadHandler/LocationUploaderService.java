package hu.daniel.vince.humanmobility.model.handlers.uploadHandler;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import hu.daniel.vince.humanmobility.R;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.entities.Location;
import hu.daniel.vince.humanmobility.model.handlers.connection.ConnectionHandler;
import hu.daniel.vince.humanmobility.model.handlers.connection.LocationConnectionManager;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.model.handlers.settings.SettingsHandler;
import hu.daniel.vince.humanmobility.model.typeHelpers.Constants;
import hu.daniel.vince.humanmobility.view.activity.LoginActivity;
import okhttp3.Response;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-21.
 */

public class LocationUploaderService extends IntentService {

    // region Members

    private static final String LOG_TAG = "LocationUploaderService";

    // endregion

    // region Constructor

    public LocationUploaderService() {
        super("LocationUploaderService");
    }

    // endregion

    // region Overrides

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(LOG_TAG, "Uploading");
        ConnectionHandler connectionHandler = ConnectionHandler.getInstance(this);
        boolean isOnlyWifiConnectionEnabled =
                SettingsHandler.getInstance(this).getBoolean(Constants.SETTINGS_ONLY_WIFI,
                true);

        // Wifi only -- No Wifi connection.
        if(isOnlyWifiConnectionEnabled && !connectionHandler.hasWifiConnection())
            return;

        // Mobile network allowed -- No connection.
        if(!connectionHandler.isConnected())
            return;

        LocationConnectionManager connectionManager = connectionHandler.getLocationManager();
        DatabaseHandler handler = DatabaseHandler.getInstance(this);
        List<Location> locations = handler.getLocations();
        ApplicationUser user = handler.getUser();

        Response response = connectionManager.sendLocations(user, locations);

        if(response != null) {
            if(response.isSuccessful())
                handler.deleteLocations(locations);
            else if(response.code() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                showNotification("Authentication problem. Please re-enter your credentials");

                user.setIsLoggedIn(false);
                handler.addOrUpdateUser(user);
            }
        }

        LocationUploadBroadcastReceiver.completeWakefulIntent(intent);
    }

    // endregion

    // region Push authentication notification

    private void showNotification(String message) {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(this, LoginActivity.class);
        int mNotificationId = (int) System.currentTimeMillis();
        PendingIntent pIntent =
                PendingIntent.getActivity(this,
                        mNotificationId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true);

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    // endregion
}
