package hu.daniel.vince.humanmobility.model.handlers.connection;

import android.app.Activity;
import android.app.Dialog;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import hu.daniel.vince.humanmobility.R;

/**
 * Created by Ferenc Lakos.
 * Date: 2017. 03. 02.
 */

public class PlayServiceValidator {
    public static boolean checkGooglePlayServices(Activity activity) {
        final int googlePlayServicesCheck = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(activity);

        switch (googlePlayServicesCheck) {
            case ConnectionResult.SUCCESS:
                return true;
            case ConnectionResult.SERVICE_DISABLED:
            case ConnectionResult.SERVICE_INVALID:
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Dialog dialog = GoogleApiAvailability.getInstance()
                        .getErrorDialog(activity, googlePlayServicesCheck, 1000, d ->
                                Toast.makeText(activity,
                                        R.string.services_error,
                                        Toast.LENGTH_SHORT)
                                .show());
                dialog.show();
        }

        return false;
    }
}
