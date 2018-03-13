package hu.daniel.vince.humanmobility.model.handlers.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import hu.daniel.vince.humanmobility.R;
import hu.daniel.vince.humanmobility.model.handlers.scheduling.SchedulingHandler;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent accelerometer = new Intent(context.getString(R.string.accelerometer_broadcast_name));
        context.sendBroadcast(accelerometer);

        SchedulingHandler scheduler = SchedulingHandler.getInstance(context);

        scheduler.restartUploader();
        scheduler.restartRecordSaver();
    }
}
