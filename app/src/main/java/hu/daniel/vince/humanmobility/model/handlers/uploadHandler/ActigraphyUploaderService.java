package hu.daniel.vince.humanmobility.model.handlers.uploadHandler;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.HttpURLConnection;
import java.util.List;

import hu.daniel.vince.humanmobility.model.entities.ActivityLevel;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.handlers.connection.ActigraphyConnectionManager;
import hu.daniel.vince.humanmobility.model.handlers.connection.ConnectionHandler;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.model.handlers.settings.SettingsHandler;
import hu.daniel.vince.humanmobility.model.typeHelpers.Constants;
import okhttp3.Response;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-09-30.
 */

public class ActigraphyUploaderService extends IntentService {

    // region Members

    private static final String LOG_TAG = "Actigra.UploaderService";

    // endregion

    // region Constructor

    public ActigraphyUploaderService() {
        super("ActigraphyUploaderService");
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

        DatabaseHandler handler = DatabaseHandler.getInstance(this);
        ApplicationUser user = handler.getUser();
        ActigraphyConnectionManager manager = connectionHandler.getActigraphyManager();

        for(short i = 0; i < 10; i++) {
            List<ActivityLevel> activities = handler.getActivityLevels();

            if(activities.size() == 0)
                break;

            Response response = manager.sendActivities(user, activities);

            if(response != null && response.isSuccessful()) {
                handler.deleteActivityLevels(activities);
                handler.clearCache();

            } else
                break;
        }
        ActigraphyUploaderBroadcastReceiver.completeWakefulIntent(intent);
    }

    // endregion
}
