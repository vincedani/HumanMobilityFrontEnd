package hu.daniel.vince.humanmobility.model.handlers.uploadHandler;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-21.
 */

public class LocationUploadBroadcastReceiver extends WakefulBroadcastReceiver {

    // region Members

    public static final int REQUEST_CODE = 20170823;

    // endregion

    // region Overrides

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, LocationUploaderService.class);
        startWakefulService(context, i);
    }

    // endregion
}
