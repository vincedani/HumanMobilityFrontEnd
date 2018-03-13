package hu.daniel.vince.humanmobility.model.handlers.location;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.joda.time.DateTime;

import hu.daniel.vince.humanmobility.model.converters.JodaTimeConverter;
import hu.daniel.vince.humanmobility.model.entities.Location;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.model.handlers.scheduling.SchedulingHandler;
import hu.daniel.vince.humanmobility.model.handlers.settings.SettingsHandler;
import hu.daniel.vince.humanmobility.model.typeHelpers.Constants;

/**
 * Created by Daniel Vince (vinced@inf.u-szeged.hu) on 2017-08-29.
 */

public class LocationSaverBroadcastReceiver extends WakefulBroadcastReceiver {

    // region Members

    public static final int REQUEST_CODE = 1995;

    private static final String LOG_TAG = "LocationSaverService";
    private static final String LOCATION_SERVICE_BUSY_KEY = "RECORD_SAVER_USE";

    private SchedulingHandler schedulingHandler;

    // endregion

    // region Overrides

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action == null)
            return;

        Log.i(LOG_TAG, "Action: " + action);
        schedulingHandler = SchedulingHandler.getInstance(context);
        long stepMillis = schedulingHandler.getSavePeriodMillis();

        if(schedulingHandler.isDeviceIdleMode())
            schedulingHandler.restartRecordSaver();

        else if(action.equals(Constants.ACTION_START_GPS))
            startGPS(context, stepMillis);

        else if(action.equals(Constants.ACTION_SAVE_RECORD))
            saveRecord(context, stepMillis);

    }

    // endregion

    // region Helpers

    private void startGPS(Context context, long stepMillis) {
        // Next time in millis
        long startGpsMillis = schedulingHandler.getStoredStartGpsWakeUpMillis() + stepMillis;
        DateTime now = DateTime.now();

        // If error -> calculate new next time in millis
        if (now.getMillis() > startGpsMillis) {
            schedulingHandler.stopRecordSaver();

            if (now.getSecondOfMinute() >= Constants.START_GPS_SECONDS_IN_MINUTES)
                now = now.plusMinutes(1);

            startGpsMillis = now.withSecondOfMinute(60 - Constants.START_GPS_TIME_IN_SECONDS)
                    .withMillisOfSecond(0).getMillis();
        }

        // Set next wake up
        schedulingHandler.setStartGpsAlarmManager(startGpsMillis);

        // Start services
        SettingsHandler settingsHandler = SettingsHandler.getInstance(context);
        LocationService service = LocationService
                .getInstance(context, settingsHandler);

        if(!service.isStarted())
            service.start();

        service.setBusyBoolean(LOCATION_SERVICE_BUSY_KEY, true);
    }

    private void saveRecord(Context context, long stepMillis) {
        SettingsHandler settingsHandler = SettingsHandler.getInstance(context);
        LocationService service = LocationService.getInstance(context, settingsHandler);

        service.setBusyBoolean(LOCATION_SERVICE_BUSY_KEY, false);

        if(!service.isBusy())
            service.stop();

        // Next time in millis
        long saveLocationMillis = schedulingHandler.getStoredSaveRecordWakeUpMillis() + stepMillis;
        DateTime now = DateTime.now();

        // If error -> calculate new next time in millis
        if (now.getMillis() > saveLocationMillis) {
            schedulingHandler.cancelSaveRecordAlarmManager();
            saveLocationMillis = now.plusMinutes(1).withSecondOfMinute(0)
                    .withMillisOfSecond(0).getMillis();
        }

        // Set next wake up
        schedulingHandler.setSaveRecordAlarmManager(saveLocationMillis);

        // Save to database
        Location location = convertToDbType(service.getLastLocation(),
                service.getLastLocationDetectTime());

        DatabaseHandler.getInstance(context).addLocation(location);
    }

    // endregion

    // region Covert android Location to hu.daniel.vince.humanmobility.models.Location

    private Location convertToDbType(android.location.Location aLocation, DateTime detectTime) {
        Location location;
        DateTime now = DateTime.now();
        JodaTimeConverter converter = new JodaTimeConverter();

        if(aLocation == null) {
            location = new Location(0, true, 0, 0,
                    converter.convertToDatabaseValue(now),
                    converter.convertToDatabaseValue(now));

        } else {
            double latitude = aLocation.getLatitude();
            double longitude = aLocation.getLongitude();
            float accuary = aLocation.getAccuracy();

            if(detectTime == null || detectTime.getMillis() > now.getMillis())
                detectTime = now;

            location = new Location(accuary, false, latitude, longitude,
                    converter.convertToDatabaseValue(detectTime),
                    converter.convertToDatabaseValue(now));
        }

        return location;
    }

    // endregion
}
