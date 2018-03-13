package hu.daniel.vince.humanmobility.model.handlers.uploadHandler;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-28.
 */

public class BugReportUploadBroadcastReceiver extends WakefulBroadcastReceiver {

    // region Members

    public static final int REQUEST_CODE = 20170828;

    // endregion

    // region Overrides

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, BugReportUploaderService.class);
        startWakefulService(context, i);
    }

    // endregion
}
