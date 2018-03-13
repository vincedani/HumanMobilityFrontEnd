package hu.daniel.vince.humanmobility.model.handlers.connection;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.entities.BugReport;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-15.
 */

public final class ReportConnectionManager extends BaseConnectionManager {

    // region Members

    private final String REPORT_TAG = "/api/Bug";
    private static final String LOG_TAG = "BugReportCM";

    // endregion

    // region Constructors

    ReportConnectionManager(OkHttpClient client) {
        super(client);
    }

    // endregion

    // region Send bug report

    public Response sendBugReports(ApplicationUser user, List<BugReport> reports) {
        String finalUrl = ConnectionHandler.URL.concat(REPORT_TAG);

        try {
            String fullToken = "Bearer " + user.getToken();

            String json = getGson().toJson(reports);

            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(finalUrl)
                    .header("Authorization", fullToken)
                    .post(body)
                    .build();

            return ExecuteHttpRequest(request);

        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            DatabaseHandler.getInstance(ConnectionHandler.getContext())
                    .addBugReport(e.getMessage());
        }
        return null;
    }

    public void sendBugReport(ApplicationUser user,
                              BugReport report) {
        List<BugReport> reports = new ArrayList<>();
        reports.add(report);

        sendBugReports(user, reports);
    }

    // endregion
}
