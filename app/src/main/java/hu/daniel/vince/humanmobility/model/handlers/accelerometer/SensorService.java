package hu.daniel.vince.humanmobility.model.handlers.accelerometer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.joda.time.DateTime;

import hu.daniel.vince.humanmobility.R;
import hu.daniel.vince.humanmobility.model.entities.Time;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.model.handlers.scheduling.SchedulingHandler;
import hu.daniel.vince.humanmobility.view.activity.LoginActivity;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-09-10.
 */

public class SensorService extends Service implements SensorEventListener{

    // region Members

    private String LOG_TAG = "SensorService";

    private Sensor accelerometer;
    private SensorManager sensorManager;
    private DatabaseHandler databaseHandler;
    private PowerManager.WakeLock wakeLock;
    private SchedulingHandler scheduler;
    private int sampleRate = SensorManager.SENSOR_DELAY_NORMAL; // 5 Hz
    private final int notificationID = 20171008;
    private Time timeOfNotification;
    private int lastMinuteWhenFiredNotification = 0;

    // endregion

    // region Broadcast receiver for handling ACTION_SCREEN_OFF

    private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i(LOG_TAG, "Screen off!");
                sensorManager.unregisterListener(SensorService.this);
                sensorManager.registerListener(SensorService.this, accelerometer, sampleRate);

            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.i(LOG_TAG, "Screen on!");
                Intent i = new Intent(getString(R.string.accelerometer_broadcast_name));
                sendBroadcast(i);
            }
        }
    };

    // endregion

    // region Constructor

    public SensorService() {
        super();
    }

    // endregion

    // region Overrides -- Service

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG_TAG, "onCreate");

        databaseHandler = DatabaseHandler.getInstance(this);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, sampleRate);
        scheduler = SchedulingHandler.getInstance(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(screenOffReceiver, filter);

        timeOfNotification = new Time();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(LOG_TAG, "onStartCommand");

        PowerManager manager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ACTIGRAPHY");
        wakeLock.acquire();

        postNotification();

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(LOG_TAG, "onTaskRemoved");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "onDestroy");
        sensorManager.unregisterListener(this);
        unregisterReceiver(screenOffReceiver);

        Intent intent = new Intent(getString(R.string.accelerometer_broadcast_name));
        sendBroadcast(intent);

        wakeLock.release();
        wakeLock = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // endregion

    // region Overrides -- SensorEventListener

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER))
            return;

        DateTime now = DateTime.now();

        int minuteOfHour = now.getMinuteOfHour();
        if(minuteOfHour != lastMinuteWhenFiredNotification) {
            postNotification();
            lastMinuteWhenFiredNotification = minuteOfHour;
            timeOfNotification.incrementTime();
        }

        databaseHandler.addActivityLevel(now, event.values[0], event.values[1], event.values[2]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing.
    }

    // endregion

    // region Notification helper

    private void postNotification() {
        String contentText = String.format("Measurement is running [%sh:%sm]",
                Integer.toString(timeOfNotification.getHours()),
                Integer.toString(timeOfNotification.getMinutes()));

        Intent notificationIntent = new Intent(SensorService.this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                SensorService.this,
                notificationID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(SensorService.this)
                        .setSmallIcon(R.drawable.ic_logo_notification)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(contentText)
                        .setContentIntent(pendingIntent)
                        .setPriority(Notification.PRIORITY_MAX);
        startForeground(notificationID, mBuilder.build());
    }

    // endregion
}
