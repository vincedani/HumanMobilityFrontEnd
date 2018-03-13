package hu.daniel.vince.humanmobility.view.fragments;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import hu.daniel.vince.humanmobility.R;
import hu.daniel.vince.humanmobility.model.handlers.connection.ConnectionHandler;
import hu.daniel.vince.humanmobility.model.converters.PlaceTypeConverter;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.entities.Place;
import hu.daniel.vince.humanmobility.model.handlers.location.LocationService;
import hu.daniel.vince.humanmobility.model.handlers.settings.SettingsHandler;
import hu.daniel.vince.humanmobility.model.typeHelpers.PlaceType;
import hu.daniel.vince.humanmobility.view.adapter.PlacesAdapter;
import hu.daniel.vince.humanmobility.view.dialog.DialogBuilder;
import okhttp3.Response;

/**
 * Created by Ferenc Lakos.
 * Date: 2016. 02. 20.
 */

public class PlacesFragment extends Fragment {

    // region Members

    private static final float MIN_VALID_ACCURACY_IN_METER = 40f;
    private static final int MAX_DETECTING_TIME_SECONDS = 10;
    private static final int MIN_RADIUS_IN_METER = 10;
    private static final int MAX_RADIUS_IN_METER = 100;

    private View rootView;
    private ListView listView;

    private ViewGroup homeLayout;
    private TextView homeDescTextView;
    private ImageView homeRightIcon;

    private ViewGroup workplaceLayout;
    private TextView workplaceDescTextView;
    private ImageView workplaceRightIcon;

    private ViewGroup schoolLayout;
    private TextView schoolDescTextView;
    private ImageView schoolRightIcon;

    private ViewGroup addNewPlaceLayout;
    private InputMethodManager inputMethodManager;

    private Map<PlaceType, Place> defaultPlaceInfoMap = new HashMap<>();
    private PlacesAdapter adapter;

    private boolean locationDetecting = false;
    private static final String LOCATION_SERVICE_BUSY_KEY = "PLACES_FRAGMENT_USE";
    private SettingsHandler settingsHandler;

    // endregion

    // region Constructor

    public PlacesFragment() {
        super();
        settingsHandler = SettingsHandler.getInstance(getContext());
    }

    // endregion

    // region Instance

    public static PlacesFragment newInstance() {
        return new PlacesFragment();
    }

    // endregion

    // region Overrides

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_places, null);

        if (getActivity() != null) {
            inputMethodManager = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            loadViews(rootView, inflater);
            initDefaultPlaceInfoMap();
            initDefaultPlaceInfoMapViews();
            initListView();
            setOnClickListeners();
        }

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopLocationService();
    }

    // endregion

    // region Initializer

    private void loadViews(View view, LayoutInflater inflater) {
        final ViewGroup headerLayout = (ViewGroup) inflater.inflate(R.layout.layout_places_header, null);
        homeLayout = (ViewGroup) headerLayout.findViewById(R.id.home_layout);
        homeDescTextView = (TextView) headerLayout.findViewById(R.id.home_desc_tv);
        homeRightIcon = (ImageView) headerLayout.findViewById(R.id.home_right_icon);
        workplaceLayout = (ViewGroup) headerLayout.findViewById(R.id.workplace_layout);
        workplaceDescTextView = (TextView) headerLayout.findViewById(R.id.workplace_desc_tv);
        workplaceRightIcon = (ImageView) headerLayout.findViewById(R.id.workplace_right_icon);
        schoolLayout = (ViewGroup) headerLayout.findViewById(R.id.school_layout);
        schoolDescTextView = (TextView) headerLayout.findViewById(R.id.school_desc_tv);
        schoolRightIcon = (ImageView) headerLayout.findViewById(R.id.school_right_icon);
        addNewPlaceLayout = (ViewGroup) inflater.inflate(R.layout.layout_places_footer, null);

        listView = (ListView) view.findViewById(R.id.places_listView);
        listView.addHeaderView(headerLayout);
        listView.addFooterView(addNewPlaceLayout);
    }

    private void initDefaultPlaceInfoMap() {
        final List<Place> places = DatabaseHandler.getInstance(getContext()).getPlaces();
        defaultPlaceInfoMap.clear();

        for (Place placeInfo : places) {
            defaultPlaceInfoMap.put(
                    PlaceTypeConverter.convertToPlaceType(placeInfo.getType()), placeInfo);
        }
    }

    private void initDefaultPlaceInfoMapViews() {
        for (Place placeInfo : defaultPlaceInfoMap.values()) {
            PlaceType type = PlaceTypeConverter.convertToPlaceType(placeInfo.getType());

            if (type.equals(PlaceType.HOME)) {
                hideAddViews(homeDescTextView, homeRightIcon);
            } else if (type.equals(PlaceType.WORKPLACE)) {
                hideAddViews(workplaceDescTextView, workplaceRightIcon);
            } else if (type.equals(PlaceType.SCHOOL)) {
                hideAddViews(schoolDescTextView, schoolRightIcon);
            }
        }
    }

    private void initListView() {
        final List<Place> placeInfoList = DatabaseHandler.getInstance(getContext()).getPlaces();

        if(adapter == null)
            adapter = new PlacesAdapter(getActivity(), placeInfoList);
        else
            adapter.setItems(placeInfoList);

        listView.setAdapter(adapter);
    }

    private void setOnClickListeners() {

        homeLayout.setOnClickListener(v -> {
            if (getActivity() == null) {
                return;
            }
            if (defaultPlaceInfoMap.containsKey(PlaceType.HOME)) {
                showQuestionDialog(defaultPlaceInfoMap.get(PlaceType.HOME));
            } else {
                addHome();
            }
        });

        workplaceLayout.setOnClickListener(v -> {
            if (getActivity() == null) {
                return;
            }
            if (defaultPlaceInfoMap.containsKey(PlaceType.WORKPLACE)) {
                showQuestionDialog(defaultPlaceInfoMap.get(PlaceType.WORKPLACE));
            } else {
                addWorkplace();
            }
        });

        schoolLayout.setOnClickListener(v -> {
            if (getActivity() == null) {
                return;
            }
            if (defaultPlaceInfoMap.containsKey(PlaceType.SCHOOL)) {
                showQuestionDialog(defaultPlaceInfoMap.get(PlaceType.SCHOOL));
            } else {
                addSchool();
            }
        });

        addNewPlaceLayout.setOnClickListener(v -> {
            if (getActivity() == null) {
                return;
            }
            getLocation(PlaceType.OTHER);
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Place placeInfo = (Place) adapter.getItem((int) id);
            showQuestionDialog(placeInfo);
        });

    }

    // endregion

    // region Default place view methods

    private void hideAddViews(TextView descView, ImageView rightIcon) {
        descView.setVisibility(View.GONE);
        rightIcon.setImageResource(R.drawable.ic_remove_24dp);
    }

    private void showAddViews(TextView descView, ImageView rightIcon) {
        descView.setVisibility(View.VISIBLE);
        rightIcon.setImageResource(R.drawable.ic_detect_position_24dp);
    }

    // endregion

    // region Home place

    private void removeHome(Place placeInfo) {
        removeDefaultPlaceInfo(placeInfo);
        showAddViews(homeDescTextView, homeRightIcon);
    }

    private void addHome() {
        getLocation(PlaceType.HOME);
    }

    // endregion

    // region Workplace

    private void removeWorkplace(Place placeInfo) {
        removeDefaultPlaceInfo(placeInfo);
        showAddViews(workplaceDescTextView, workplaceRightIcon);
    }

    private void addWorkplace() {
        getLocation(PlaceType.WORKPLACE);
    }

    // endregion

    // region School

    private void removeSchool(Place placeInfo) {
        removeDefaultPlaceInfo(placeInfo);
        showAddViews(schoolDescTextView, schoolRightIcon);
    }

    private void addSchool() {
        getLocation(PlaceType.SCHOOL);
    }

    // endregion

    // region Remove

    private void selectRemovePlaceInfo(Place placeInfo) {
        PlaceType type = PlaceTypeConverter.convertToPlaceType(placeInfo.getType());

        if (type.equals(PlaceType.HOME)) {
            removeHome(placeInfo);

        } else if (type.equals(PlaceType.WORKPLACE)) {

            removeWorkplace(placeInfo);
        } else if (type.equals(PlaceType.SCHOOL)) {

            removeSchool(placeInfo);
        } else {

            removePlaceInfo(placeInfo);
        }
    }

    private void removeDefaultPlaceInfo(Place placeInfo) {
        defaultPlaceInfoMap.remove(placeInfo.getType());
        removePlaceInfo(placeInfo);
    }

    private void removePlaceInfo(Place placeInfo) {
        DatabaseHandler databaseHandler = DatabaseHandler.getInstance(getContext());
        ApplicationUser user = databaseHandler.getUser();

        ConnectionHandler.getInstance(getContext()).getPlaceManager().deletePlace(user, placeInfo);
        databaseHandler.deletePlace(placeInfo.getId());

        Snackbar.make(rootView, R.string.place_remove_success, Snackbar.LENGTH_LONG).show();

        initDefaultPlaceInfoMap();
        initListView();
    }

    // endregion

    // region Add

    private void checkLocation(PlaceType type, Location location) {
        if (getActivity() == null) {
            return;
        }

        if (location == null) {
            Snackbar.make(rootView, R.string.place_add_error, Snackbar.LENGTH_LONG).show();
            return;
        }

        final Place placeInfo = new Place(
                type.name(),
                "",
                location.getLatitude(),
                location.getLongitude(),
                30);

        createSavePlaceDialog(getActivity(), placeInfo).show();
    }

    private void addPlaceInfo(Place placeInfo) {
        if (getActivity() == null) {
            return;
        }

        DatabaseHandler databaseHandler = DatabaseHandler.getInstance(getContext());
        ApplicationUser user = databaseHandler.getUser();

        if(databaseHandler.addPlace(placeInfo)) {
            ConnectionHandler.getInstance(getContext()).getPlaceManager().sendPlace(user,
                    placeInfo, new ConnectionHandler.ConnectionCallback() {
                        @Override
                        public void onFailure(String error) { /* Leave empty. */}

                        @Override
                        public void onSuccess(Response response) { /* Leave empty. */}
                    });
        }

        boolean defaultPlace = true;
        if (placeInfo.getType().equals(PlaceType.HOME)) {
            hideAddViews(homeDescTextView, homeRightIcon);
        } else if (placeInfo.getType().equals(PlaceType.WORKPLACE)) {
            hideAddViews(workplaceDescTextView, workplaceRightIcon);
        } else if (placeInfo.getType().equals(PlaceType.SCHOOL)) {
            hideAddViews(schoolDescTextView, schoolRightIcon);
        } else {
            defaultPlace = false;
        }

        Snackbar.make(rootView, R.string.place_add_success, Snackbar.LENGTH_LONG).show();

        if (defaultPlace) {
            initDefaultPlaceInfoMap();
        } else {
            initListView();
        }
    }

    // endregion

    // region Location service

    private boolean hasGPS() {
        if(LocationService.getInstance(getContext(), settingsHandler).hasGps()) {
            return true;
        }

        final Snackbar snackbar = Snackbar.make(
                rootView, R.string.notification_waiting_for_gps,
                Snackbar.LENGTH_LONG).setAction(R.string.application_show, v -> {
            if (getActivity() != null) {
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivity(intent);
            }
        });
        snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.color_accent));
        snackbar.show();

        return false;
    }

    private void getLocation(final PlaceType type) {
        if (!hasGPS()) {
            return;
        }

        final AlertDialog progressDialog = DialogBuilder.createLoadingDialog(getActivity());
        progressDialog.show();

        LocationService locationService = LocationService.getInstance(getContext(), settingsHandler);

        if (!locationService.isStarted()) {
            locationService.start();
        }

        locationDetecting = true;
        locationService.setBusyBoolean(LOCATION_SERVICE_BUSY_KEY, true);

        // Start detecting
        locationService.addOnLocationChangedListener(LOCATION_SERVICE_BUSY_KEY, location -> {
            if (getActivity() == null) {
                stopLocationService();
            } else if (location.getAccuracy() <= MIN_VALID_ACCURACY_IN_METER) {
                stopLocationService();

                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    checkLocation(type, location);
                });
            }
        });

        // Kill detecting
        new Handler().postDelayed(() -> {
            if (locationDetecting) {
                stopLocationService();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        checkLocation(type, null);
                    });
                }
            }
        }, TimeUnit.SECONDS.toMillis(MAX_DETECTING_TIME_SECONDS));
    }

    private void stopLocationService() {
        locationDetecting = false;
        LocationService locationService = LocationService.getInstance(getContext(), settingsHandler);

        locationService.removeOnLocationChangedListener(LOCATION_SERVICE_BUSY_KEY);
        locationService.setBusyBoolean(LOCATION_SERVICE_BUSY_KEY, false);

        if (!locationService.isBusy()) {
            locationService.stop();
        }
    }

    // endregion

    // region Dialogs

    private void showQuestionDialog(final Place placeInfo) {
        DialogBuilder.createQuestionDialog(
                getActivity(),
                R.string.place_remove_question,
                R.string.place_remove_action,
                (dialog, which) -> {
                    if (getActivity() != null) {
                        selectRemovePlaceInfo(placeInfo);
                    }
                }
        ).show();
    }

    private AlertDialog createSavePlaceDialog(Context context, final Place placeInfo) {
        final ScrollView scrollView =
                (ScrollView) View.inflate(getActivity(), R.layout.dialog_add_place, null);
        final EditText titleEditText = (EditText) scrollView.findViewById(R.id.place_title_input);
        final EditText radiusEditText = (EditText) scrollView.findViewById(R.id.place_radius_input);

        if (!placeInfo.getType().equals(PlaceType.OTHER)) {
            titleEditText.setText(placeInfo.getType());
            titleEditText.setVisibility(View.GONE);
        }

        final AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.AlertDialogStyle);

        builder.setTitle(R.string.place_add_new);
        builder.setView(scrollView);
        builder.setNegativeButton(R.string.application_cancel, (dialog, which) -> dialog.dismiss());

        builder.setPositiveButton(R.string.application_add, null);

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            b.setOnClickListener(v -> {
                if (getActivity() == null) {
                    return;
                }

                hideSoftInput(titleEditText, radiusEditText);

                int radius;
                if (!isValidName(getActivity(), titleEditText) ||
                        (radius = isValidRadius(getActivity(), radiusEditText)) == -1) {
                    return;
                }

                dialog.dismiss();

                placeInfo.setTitle(titleEditText.getText().toString());
                placeInfo.setRadius(radius);
                addPlaceInfo(placeInfo);
            });
        });

        return dialog;
    }

    private boolean isValidName(Context context, EditText editText) {
        boolean result = !TextUtils.isEmpty(editText.getText().toString()) &&
                editText.getText().toString().length() < 65;

        if (result) {
            editText.setBackgroundResource(R.drawable.selector_edittext_bg);
        } else {
            editText.setBackgroundResource(R.drawable.selector_edittext_error_bg);
            Toast.makeText(context, R.string.place_get_title_empty, Toast.LENGTH_SHORT).show();
        }

        return result;
    }

    private int isValidRadius(Context context, EditText editText) {
        int result = -1;

        try {
            int value = Integer.parseInt(editText.getText().toString());
            if (value >= MIN_RADIUS_IN_METER && value <= MAX_RADIUS_IN_METER) {
                result = value;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        if (result != -1) {
            editText.setBackgroundResource(R.drawable.selector_edittext_bg);
        } else {
            editText.setBackgroundResource(R.drawable.selector_edittext_error_bg);
            Toast.makeText(context,
                    R.string.place_get_radius_error,
                    Toast.LENGTH_SHORT)
                .show();
        }

        return result;
    }

    private void hideSoftInput(EditText... editTexts) {
        if (inputMethodManager == null) {
            return;
        }

        if (editTexts != null && editTexts.length > 0) {
            for (EditText editText : editTexts) {
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }
    }

    // endregion
}
