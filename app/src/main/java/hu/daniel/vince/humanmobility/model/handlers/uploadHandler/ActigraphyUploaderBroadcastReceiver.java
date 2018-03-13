package hu.daniel.vince.humanmobility.model.handlers.uploadHandler;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class ActigraphyUploaderBroadcastReceiver extends WakefulBroadcastReceiver {

    // region Members

    public static final int REQUEST_CODE = 20170930;

    // endregion

    // region Overrides

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, ActigraphyUploaderService.class);
        startWakefulService(context, i);
    }

    // endregion
}
