package hu.daniel.vince.humanmobility.model.typeHelpers;

/**
 * Created by Ferenc Lakos.
 * Date: 2015. 11. 01.
 */

public class Constants {

    /*
     * Actions
     */

    public static final String PACKAGE = "hu.daniel.vince.humanmobility";
    public static final String ACTION_START_GPS = PACKAGE + ".ACTION_START_GPS";
    public static final String ACTION_SAVE_RECORD = PACKAGE + ".ACTION_SAVE_RECORD";
    /*
     * LocationService
     */

    public static final int START_GPS_TIME_IN_SECONDS = 10;
    public static final int START_GPS_SECONDS_IN_MINUTES = 60 - START_GPS_TIME_IN_SECONDS;

    public static final long UPDATE_INTERVAL_IN_MILLIS = 40;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLIS = UPDATE_INTERVAL_IN_MILLIS / 2;

    /*
     * Settings
     */

    public static final String SETTINGS_UPLOAD_ENABLE = "actigraphy_settings_upload_enable";
    public static final String SETTINGS_ONLY_WIFI = "actigraphy_settings_wifi_only";
    public static final String SETTINGS_UPLOAD_INTERVAL_POSITION = "actigraphy_settings_upload_interval_position";
    public static final int SETTINGS_UPLOAD_INTERVAL_DEFAULT_POSITION = 1;
    public static final int[] SETTINGS_UPLOAD_INTERVAL_VALUES = {30, 60, 120, 240, 480};

    public static final String SETTINGS_PERIOD = "actigraphy_settings_period";
    public static final int SETTINGS_PERIOD_DEFAULT_VALUE = 1;

    /*
     * States
     */

    public static final String STATE_UPLOADER_RUNNING = "actigraphy_settings_upload_alarm_running";
    public static final String STATE_RECORD_SAVER_RUNNING = "actigraphy_settings_record_saver_running";


    public static final String DATA_START_GPS_NEXT_DATE_IN_MILLIS = "actigraphy_data_start_gps_next_date_in_millis";
    public static final String DATA_SAVE_RECORD_NEXT_DATE_IN_MILLIS = "actigraphy_data_save_record_next_date_in_millis";

}
