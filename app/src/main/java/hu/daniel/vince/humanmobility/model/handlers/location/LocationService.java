package hu.daniel.vince.humanmobility.model.handlers.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

import hu.daniel.vince.humanmobility.model.handlers.settings.SettingsHandler;
import hu.daniel.vince.humanmobility.model.typeHelpers.Constants;
import hu.daniel.vince.humanmobility.model.typeHelpers.ErrorType;

/**
 * Created by Ferenc Lakos.
 * Date: 2015. 11. 27.
 */

public class LocationService implements LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // region Members

    private static final String GOOGLE_API_CONNECT = "actigraphy_google_api_connected";
    private static final String LOCATION_REQUEST_START = "actigraphy_location_request_started";
    private static LocationService instance;

    private final Context context;
    private final SettingsHandler settingsHandler;

    private LocationManager locationManager;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private Location lastLocation = null;
    private DateTime lastLocationDetectTime = null;

    private OnServiceComplete onServiceComplete;
    private Map<String, OnLocationChangedListener> onLocationChangedListenerMap = new HashMap<>();
    private boolean started;

    private Map<String, Boolean> busyMap = new HashMap<>();

    // endregion

    // region Nested interfaces

    public interface OnServiceComplete {
        void onConnected();

        void onError(ErrorType errorType);
    }

    public interface OnLocationChangedListener {
        void onChanged(Location location);
    }

    // endregion

    // region Constructor

    private LocationService(Context context, SettingsHandler settingsHandler) {
        this.context = context;
        this.settingsHandler = settingsHandler;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        initGoogleApiClient();
        initLocationRequest();
        checkConnectionState();
    }

    public static LocationService getInstance(Context context, SettingsHandler settingsHandler) {
        if(instance == null)
            instance = new LocationService(context, settingsHandler);

        return instance;
    }

    // endregion

    // region Initialization

    private void initGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void initLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(Constants.UPDATE_INTERVAL_IN_MILLIS);
        locationRequest.setFastestInterval(Constants.FASTEST_UPDATE_INTERVAL_IN_MILLIS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(1f);
    }

    // endregion

    // region Getters

    public boolean hasGps() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean isStarted() {
        return started;
    }

    public Location getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        final Location fusedLastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (fusedLastLocation != null && (lastLocation == null || lastLocation.getAccuracy() > fusedLastLocation.getAccuracy())) {
            lastLocationDetectTime = null;
            return fusedLastLocation;
        }

        return lastLocation;
    }

    DateTime getLastLocationDetectTime() {
        return lastLocationDetectTime;
    }

    public boolean isBusy() {
        boolean busy = false;

        for (Boolean busyMapValue : busyMap.values()) {
            if (busyMapValue) {
                busy = true;
            }
        }

        return busy;
    }

    // endregion

    // region Setters

    public void addOnLocationChangedListener(String key,
                                             OnLocationChangedListener onLocationChangedListener) {
        onLocationChangedListenerMap.put(key, onLocationChangedListener);
    }

    public void removeOnLocationChangedListener(String key) {
        if (onLocationChangedListenerMap.containsKey(key)) {
            onLocationChangedListenerMap.remove(key);
        }
    }

    public void setBusyBoolean(String key, boolean value) {
        busyMap.put(key, value);
    }

    // endregion

    // region Helpers

    private void checkConnectionState() {
        boolean connected = settingsHandler.getBoolean(GOOGLE_API_CONNECT);
        if (connected) {
            connect();
        }
    }

    private void startLocationRequest() {
        if (!started) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                return;
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    locationRequest,
                    this);
            started = true;
        }
    }

    private void stopLocationRequest() {
        if (started) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            started = false;
        }
    }


    public void connect() {
        settingsHandler.save(GOOGLE_API_CONNECT, true);
        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    public void start() {
        lastLocation = null;
        lastLocationDetectTime = null;
        settingsHandler.save(LOCATION_REQUEST_START, true);

        if (googleApiClient.isConnected()) {
            startLocationRequest();

        } else if (!googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    public void stop() {
        settingsHandler.remove(LOCATION_REQUEST_START);
        stopLocationRequest();
    }

    // endregion

    // region Implement interfaces

    @Override
    public void onConnected(Bundle bundle) {
        // Checking location request status
        final boolean isStarted = settingsHandler.getBoolean(LOCATION_REQUEST_START);
        if (isStarted && !started) {
            startLocationRequest();
        }
        // Callback
        if (onServiceComplete != null) {
            onServiceComplete.onConnected();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Reconnect
        if (started) {
            stop();
            settingsHandler.save(LOCATION_REQUEST_START, true);
        }
        connect();
        // Callback
        if (onServiceComplete != null) {
            onServiceComplete.onError(ErrorType.SUSPENDED_ERROR);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (lastLocation == null || lastLocation.getAccuracy() > location.getAccuracy()) {
            lastLocationDetectTime = DateTime.now();
            lastLocation = location;
        }

        for (OnLocationChangedListener locationListener : onLocationChangedListenerMap.values()) {
            if (locationListener != null) {
                locationListener.onChanged(location);
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Failed
        started = false;
        settingsHandler.remove(LOCATION_REQUEST_START);
        settingsHandler.remove(GOOGLE_API_CONNECT);
        // Callback
        if (onServiceComplete != null) {
            onServiceComplete.onError(ErrorType.UNKNOWN_ERROR);
        }
    }

    // endregion
}
