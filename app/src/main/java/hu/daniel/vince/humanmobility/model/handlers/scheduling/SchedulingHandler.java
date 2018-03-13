package hu.daniel.vince.humanmobility.model.handlers.scheduling;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import org.joda.time.DateTime;

import hu.daniel.vince.humanmobility.model.handlers.location.LocationSaverBroadcastReceiver;
import hu.daniel.vince.humanmobility.model.handlers.settings.SettingsHandler;
import hu.daniel.vince.humanmobility.model.handlers.uploadHandler.ActigraphyUploaderBroadcastReceiver;
import hu.daniel.vince.humanmobility.model.handlers.uploadHandler.BugReportUploadBroadcastReceiver;
import hu.daniel.vince.humanmobility.model.handlers.uploadHandler.LocationUploadBroadcastReceiver;
import hu.daniel.vince.humanmobility.model.typeHelpers.Constants;

/**
 * Created by Ferenc Lakos on 2016-01-20.
 * Edited by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-29.
 */

public class SchedulingHandler {

    // region Members

    private static String LOG_TAG = "Scheduler";

    @SuppressLint("StaticFieldLeak")
    private static SchedulingHandler instance;

    private final AlarmManager alarmManager;
    private final PowerManager powerManager;
    private final Context context;
    private final SettingsHandler settingsHandler;

    // endregion

    // region Constructor

    private SchedulingHandler(Context context) {
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.settingsHandler = SettingsHandler.getInstance(context);
        this.context = context;
    }

    // endregion

    // region Instance

    public static SchedulingHandler getInstance(Context context) {
        if(instance == null)
            synchronized (SchedulingHandler.class) {
                if(instance == null)
                    instance = new SchedulingHandler(context);
            }
        return instance;
    }

    // endregion

    // region Getters

    public boolean isDeviceIdleMode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager.isDeviceIdleMode();
    }

    public boolean isUploaderRunning() {
        return settingsHandler.getBoolean(Constants.STATE_UPLOADER_RUNNING);
    }

    public long getUploadIntervalMillis() {
        return Constants.SETTINGS_UPLOAD_INTERVAL_VALUES[
                settingsHandler.getInt(
                        Constants.SETTINGS_UPLOAD_INTERVAL_POSITION,
                        Constants.SETTINGS_UPLOAD_INTERVAL_DEFAULT_POSITION)]
                * 60 * 1000;
    }

    public long getSavePeriodMillis() {
        return settingsHandler.getInt(Constants.SETTINGS_PERIOD,
                Constants.SETTINGS_PERIOD_DEFAULT_VALUE) * 60 * 1000;
    }

    public long getStoredStartGpsWakeUpMillis() {
        return settingsHandler.getLong(Constants.DATA_START_GPS_NEXT_DATE_IN_MILLIS);
    }

    public long getStoredSaveRecordWakeUpMillis() {
        return settingsHandler.getLong(Constants.DATA_SAVE_RECORD_NEXT_DATE_IN_MILLIS);
    }

    // endregion

    // region Uploader managing

    public void restartUploader() {
        stopUploader();
        startUploader();

    }

    public void startUploader() {
        if(settingsHandler.getBoolean(Constants.STATE_UPLOADER_RUNNING)) {

            final long intervalMillis = getUploadIntervalMillis();

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    DateTime.now().getMillis(),
                    intervalMillis,
                    getUploaderPendingIntent(context));

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    DateTime.now().getMillis(),
                    intervalMillis,
                    getBugReporterPendingIntent(context));

            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    DateTime.now().getMillis(),
                    intervalMillis,
                    getActigraphyPendingIntent(context));

            settingsHandler.save(Constants.STATE_UPLOADER_RUNNING, true);
        }
    }

    public void stopUploader() {
        alarmManager.cancel(getUploaderPendingIntent(context));
        alarmManager.cancel(getBugReporterPendingIntent(context));
        alarmManager.cancel(getActigraphyPendingIntent(context));

        settingsHandler.save(Constants.STATE_UPLOADER_RUNNING, false);
    }

    // endregion

    // region Location measurement managing

    public void restartRecordSaver() {
        AsyncTask.execute(() -> {
            stopRecordSaver();
            startRecordSaver();
        });
    }

    public void startRecordSaver() {
        AsyncTask.execute(() -> {
            DateTime now = DateTime.now();

            if (now.getSecondOfMinute() >= Constants.START_GPS_SECONDS_IN_MINUTES) {
                now = now.plusMinutes(1);
            }

            final long startGpsMillis = now.withSecondOfMinute(60 - Constants.START_GPS_TIME_IN_SECONDS)
                    .withMillisOfSecond(0).getMillis();
            final long startSaveLocationMillis = now.plusMinutes(1).withSecondOfMinute(0)
                    .withMillisOfSecond(0).getMillis();

            setGPSAlarmManager(startGpsMillis, getStartGpsPendingIntent(context));
            setSaveRecordAlarmManager(startSaveLocationMillis);

            settingsHandler.save(Constants.STATE_RECORD_SAVER_RUNNING, true);
        });
    }

    public void stopRecordSaver() {
        Log.d(LOG_TAG, "stopRecordSaver");

        alarmManager.cancel(getStartGpsPendingIntent(context));
        settingsHandler.save(Constants.STATE_RECORD_SAVER_RUNNING, false);

        settingsHandler.remove(Constants.STATE_RECORD_SAVER_RUNNING);
        settingsHandler.remove(Constants.DATA_START_GPS_NEXT_DATE_IN_MILLIS);
        settingsHandler.remove(Constants.DATA_SAVE_RECORD_NEXT_DATE_IN_MILLIS);
    }

    // endregion

    // region Measurement alarm manager helpers

    private void setGPSAlarmManager(long wakeUpMillis, PendingIntent pendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    wakeUpMillis,
                    pendingIntent);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpMillis, pendingIntent);

        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, wakeUpMillis, pendingIntent);
        }
    }

    public void setSaveRecordAlarmManager(long wakeUpMillis) {
        Log.d("Scheduler", "setSaveRecordAlarmManager");
        storeSaveRecordWakeUpMillis(wakeUpMillis);
        setGPSAlarmManager(wakeUpMillis, getSaveLocationPendingIntent(context));
    }

    public void cancelSaveRecordAlarmManager() {
        Log.d(LOG_TAG, "cancelSaveRecordAlarmManager");
        alarmManager.cancel(getSaveLocationPendingIntent(context));
    }

    public void setStartGpsAlarmManager(long wakeUpMillis) {
        Log.d(LOG_TAG, "setStartGpsAlarmManager");
        storeStartGpsWakeUpMillis(wakeUpMillis);
        setGPSAlarmManager(wakeUpMillis, getStartGpsPendingIntent(context));
    }

    // endregion

    // region SettingsHandler store methods

    private void storeStartGpsWakeUpMillis(long startGpsMillis) {
        settingsHandler.save(Constants.DATA_START_GPS_NEXT_DATE_IN_MILLIS, startGpsMillis);
    }

    private void storeSaveRecordWakeUpMillis(long saveLocationMillis) {
        settingsHandler.save(Constants.DATA_SAVE_RECORD_NEXT_DATE_IN_MILLIS, saveLocationMillis);
    }

    // endregion

    // region Pending intent creation

    private static PendingIntent getUploaderPendingIntent(Context context) {
        Intent intent = new Intent(context, LocationUploadBroadcastReceiver.class);

        return PendingIntent.getBroadcast(
                context,
                LocationUploadBroadcastReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

    }

    private static PendingIntent getBugReporterPendingIntent(Context context) {
        Intent intent = new Intent(context, BugReportUploadBroadcastReceiver.class);

        return PendingIntent.getBroadcast(
                context,
                BugReportUploadBroadcastReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getActigraphyPendingIntent(Context context) {
        Intent intent = new Intent(context, ActigraphyUploaderBroadcastReceiver.class);

        return PendingIntent.getBroadcast(
                context,
                ActigraphyUploaderBroadcastReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getStartGpsPendingIntent(Context context) {
        Intent intent = new Intent(context, LocationSaverBroadcastReceiver.class);
        intent.setAction(Constants.ACTION_START_GPS);

        return PendingIntent.getBroadcast(
                context,
                LocationSaverBroadcastReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getSaveLocationPendingIntent(Context context) {
        Intent intent = new Intent(context, LocationSaverBroadcastReceiver.class);
        intent.setAction(Constants.ACTION_SAVE_RECORD);

        return PendingIntent.getBroadcast(
                context,
                LocationSaverBroadcastReceiver.REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // endregion
}
