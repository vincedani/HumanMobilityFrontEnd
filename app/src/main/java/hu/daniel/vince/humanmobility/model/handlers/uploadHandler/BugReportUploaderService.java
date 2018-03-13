package hu.daniel.vince.humanmobility.model.handlers.uploadHandler;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;

import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.entities.BugReport;
import hu.daniel.vince.humanmobility.model.handlers.connection.ConnectionHandler;
import hu.daniel.vince.humanmobility.model.handlers.connection.ReportConnectionManager;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.model.handlers.settings.SettingsHandler;
import hu.daniel.vince.humanmobility.model.typeHelpers.Constants;
import okhttp3.Response;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-28.
 */

public class BugReportUploaderService extends IntentService {

    // region Members

    private static final String LOG_TAG = "ReportUploaderService";

    // endregion

    // region Constructor

    public BugReportUploaderService() {
        super("BugReportUploaderService");
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

        ReportConnectionManager connectionManager = connectionHandler.getReportManager();
        DatabaseHandler handler = DatabaseHandler.getInstance(this);
        List<BugReport> bugReports = handler.getBugReports();
        ApplicationUser user = handler.getUser();

        Response response = connectionManager.sendBugReports(user, bugReports);

        if(response != null && response.isSuccessful())
            handler.deleteBugReports(bugReports);

        BugReportUploadBroadcastReceiver.completeWakefulIntent(intent);
    }

    // endregion
}
