package hu.daniel.vince.humanmobility.model.handlers.accelerometer;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017.09.10.
 */

public class MeasurementRestartBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent sensorIntent = new Intent(context, SensorService.class);
        startWakefulService(context, sensorIntent);
    }
}
