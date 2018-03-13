package hu.daniel.vince.humanmobility.view.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Ferenc Lakos.
 * Date: 2017. 02. 27.
 */

public abstract class BaseCompactActivity extends AppCompatActivity {
    interface OnPermissionRequestListener {
        void onGranted();
        void onDenied();
    }

    private static final int REQUEST_PERMISSION = 1001;

    private OnPermissionRequestListener onPermissionRequestListener = null;
    private Map<String, Boolean> grantedPermissions = new HashMap<>();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        initPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
        initPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        initPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        super.onCreate(savedInstanceState);
    }

    private void initPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            grantedPermissions.put(
                    permission,
                    ContextCompat.checkSelfPermission(this, permission) ==
                            PackageManager.PERMISSION_GRANTED);
        }
    }

    public void requestPermissions(OnPermissionRequestListener onPermissionRequestListener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onPermissionRequestListener.onGranted();
            return;
        }

        final List<String> deniedPermissions = new ArrayList<>();

        for (Map.Entry<String, Boolean> entry : grantedPermissions.entrySet()) {
            final String permission = entry.getKey();
            final boolean granted = entry.getValue();
            if (!granted) {
                deniedPermissions.add(permission);
            }
        }

        if (deniedPermissions.size() > 0) {
            this.onPermissionRequestListener = onPermissionRequestListener;
            final String[] permissionsArray =
                    deniedPermissions.toArray(new String[deniedPermissions.size()]);

            ActivityCompat.requestPermissions(this, permissionsArray, REQUEST_PERMISSION);
        } else {
            onPermissionRequestListener.onGranted();
        }
    }

    public boolean permissionsGranted() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (Boolean granted : grantedPermissions.values()) {
            if (!granted) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION) {
            return;
        }

        for (int i = 0; i < permissions.length; i++) {
            grantedPermissions.put(permissions[i], grantResults[i] ==
                    PackageManager.PERMISSION_GRANTED);
        }

        if (onPermissionRequestListener != null) {
            if (permissionsGranted()) {
                onPermissionRequestListener.onGranted();
            } else {
                onPermissionRequestListener.onDenied();
            }

            onPermissionRequestListener = null;
        }
    }
}
