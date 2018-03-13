package hu.daniel.vince.humanmobility.view.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import hu.daniel.vince.humanmobility.BuildConfig;
import hu.daniel.vince.humanmobility.R;
import hu.daniel.vince.humanmobility.model.handlers.connection.ConnectionHandler;
import hu.daniel.vince.humanmobility.model.handlers.scheduling.SchedulingHandler;
import hu.daniel.vince.humanmobility.model.handlers.settings.SettingsHandler;
import hu.daniel.vince.humanmobility.model.handlers.uploadHandler.ActigraphyUploaderService;
import hu.daniel.vince.humanmobility.model.handlers.uploadHandler.BugReportUploaderService;
import hu.daniel.vince.humanmobility.model.handlers.uploadHandler.LocationUploaderService;
import hu.daniel.vince.humanmobility.model.typeHelpers.Constants;
import hu.daniel.vince.humanmobility.model.typeHelpers.ErrorType;
import hu.daniel.vince.humanmobility.view.dialog.DialogBuilder;

/**
 * Created by Ferenc Lakos.
 * Date: 2016. 02. 20.
 */

public class SettingsFragment extends Fragment {

    // region Members

    private View rootView;

    private ViewGroup measurementButton;
    private ViewGroup uploadButton;
    private ViewGroup syncButton;
    private CheckBox syncCheckBox;
    private ViewGroup frequencyButton;
    private TextView frequencyTextView;
    private ViewGroup onlyWifiButton;
    private CheckBox onlyWifiCheckBox;
    public Activity mainActivity;

    private boolean sync;
    private boolean onlyWifi;

    // endregion

    // region Instance

    public static SettingsFragment newInstance(Activity activity) {
        SettingsFragment fragment = new SettingsFragment();
        fragment.mainActivity = activity;

        return fragment;
    }

    // endregion

    // region Overrides

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, null);

        loadViews(rootView);
        init();
        setOnClickListeners();

        return rootView;
    }

    // endregion

    // region Initialization

    private void loadViews(View view) {
        measurementButton = (ViewGroup) view.findViewById(R.id.measurement_layout);
        uploadButton = (ViewGroup) view.findViewById(R.id.upload_layout);
        syncButton = (ViewGroup) view.findViewById(R.id.sync_layout);
        syncCheckBox = (CheckBox) view.findViewById(R.id.sync_chk);
        frequencyButton = (ViewGroup) view.findViewById(R.id.frequency_layout);
        frequencyTextView = (TextView) view.findViewById(R.id.frequency_desc_tv);
        onlyWifiButton = (ViewGroup) view.findViewById(R.id.wifi_layout);
        onlyWifiCheckBox = (CheckBox) view.findViewById(R.id.wifi_chk);

        final TextView versionTextView = (TextView) view.findViewById(R.id.version_desc_tv);
        versionTextView.setText(String.format(
                "v%s-%s",
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE));
    }

    private void init() {
        SettingsHandler settingsHandler = SettingsHandler.getInstance(getContext());
        sync = settingsHandler.getBoolean(Constants.SETTINGS_UPLOAD_ENABLE, true);
        syncCheckBox.setChecked(sync);

        frequencyButton.setEnabled(sync);
        frequencyButton.setAlpha(sync ? 1f : 0.5f);
        setFrequencyTextView(
                settingsHandler.getInt(
                        Constants.SETTINGS_UPLOAD_INTERVAL_POSITION,
                        Constants.SETTINGS_UPLOAD_INTERVAL_DEFAULT_POSITION));

        onlyWifi = settingsHandler.getBoolean(
                Constants.SETTINGS_ONLY_WIFI,
                true);

        onlyWifiCheckBox.setChecked(onlyWifi);
    }

    private void setOnClickListeners() {

        measurementButton.setOnClickListener(v -> {
            if (getActivity() == null) {
                return;
            }
            restartBackgroundMeasurement();
        });
        uploadButton.setOnClickListener(v -> {
            if (getActivity() == null) {
                return;
            }
            showUploadDialog();
        });

        syncButton.setOnClickListener(v -> {
            if (getActivity() == null) {
                return;
            }
            changeSyncState();
        });

        frequencyButton.setOnClickListener(v -> {
            if (getActivity() == null) {
                return;
            }
            showFrequencyDialog();
        });

        onlyWifiButton.setOnClickListener(v -> {
            if (getActivity() == null) {
                return;
            }
            changeOnlyWifiState();
        });

    }

    // endregion

    // region Uploading

    private void createErrorUploadSnackbar(String whichOne, ErrorType errorType) {
        final String errorMessage = String.format(
                getResources().getString(R.string.settings_upload_error),
                whichOne,
                errorType.name().toLowerCase());

        Snackbar.make(rootView, errorMessage, Snackbar.LENGTH_LONG).show();
    }

    private void showUploadDialog() {
        if(!ConnectionHandler.getInstance(getContext()).isConnected()) {
            createErrorUploadSnackbar("other", ErrorType.NETWORK_ERROR);
            return;
        }

        String message = getString(R.string.settings_upload_started);
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();

        Intent locationIntent = new Intent(getActivity(), LocationUploaderService.class);
        getActivity().startService(locationIntent);

        Intent bugReportIntent = new Intent(getActivity(), BugReportUploaderService.class);
        getActivity().startService(bugReportIntent);

        Intent actigraphyIntent = new Intent(getActivity(), ActigraphyUploaderService.class);
        getActivity().startService(actigraphyIntent);
    }

    // endregion

    // region Helpers

    private void restartBackgroundMeasurement() {
        Intent intent = new Intent(getString(R.string.accelerometer_broadcast_name));
        getContext().sendBroadcast(intent);
        SchedulingHandler.getInstance(getContext()).restartRecordSaver();
    }

    private void changeSyncState() {
        sync = !sync;

        SettingsHandler.getInstance(getContext()).save(Constants.SETTINGS_UPLOAD_ENABLE, sync);
        syncCheckBox.setChecked(sync);

        frequencyButton.setEnabled(sync);
        frequencyButton.setAlpha(sync ? 1f : 0.5f);

        if (sync)
            SchedulingHandler.getInstance(getContext()).startUploader();
        else
            SchedulingHandler.getInstance(getContext()).stopUploader();
    }

    private void setFrequencyTextView(int position) {
        final int resId;
        switch (position) {
            case 0:
                resId = R.string.settings_frequency_30;
                break;
            case 2:
                resId = R.string.settings_frequency_120;
                break;
            case 3:
                resId = R.string.settings_frequency_240;
                break;
            case 4:
                resId = R.string.settings_frequency_480;
                break;
            default:
                resId = R.string.settings_frequency_60;
                break;
        }
        frequencyTextView.setText(String.format("%s.", getResources().getString(resId)));
    }

    private void showFrequencyDialog() {

        DialogBuilder.createSingleChoiceDialog(
                getActivity(),
                R.string.settings_frequency,
                R.array.settings_frequency_array,
                SettingsHandler.getInstance(getContext()).getInt(
                        Constants.SETTINGS_UPLOAD_INTERVAL_POSITION,
                        Constants.SETTINGS_UPLOAD_INTERVAL_DEFAULT_POSITION),
                null,
                (dialog, which) -> {
                    SettingsHandler.getInstance(getContext()).
                            save(Constants.SETTINGS_UPLOAD_INTERVAL_POSITION, which);
                    setFrequencyTextView(which);
                    SchedulingHandler.getInstance(getContext()).restartUploader();
                }
        ).show();
    }

    private void changeOnlyWifiState() {
        onlyWifi = !onlyWifi;
        SettingsHandler.getInstance(getContext()).save(Constants.SETTINGS_ONLY_WIFI, onlyWifi);
        onlyWifiCheckBox.setChecked(onlyWifi);
    }

    // endregion
}
