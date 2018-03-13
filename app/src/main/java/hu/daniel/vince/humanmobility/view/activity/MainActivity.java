package hu.daniel.vince.humanmobility.view.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import hu.daniel.vince.humanmobility.R;
import hu.daniel.vince.humanmobility.model.entities.ApplicationUser;
import hu.daniel.vince.humanmobility.model.handlers.connection.ConnectionHandler;
import hu.daniel.vince.humanmobility.model.handlers.connection.PlayServiceValidator;
import hu.daniel.vince.humanmobility.model.handlers.database.DatabaseHandler;
import hu.daniel.vince.humanmobility.model.handlers.scheduling.SchedulingHandler;
import hu.daniel.vince.humanmobility.model.handlers.settings.SettingsHandler;
import hu.daniel.vince.humanmobility.view.dialog.DialogBuilder;
import hu.daniel.vince.humanmobility.view.fragments.BugReportFragment;
import hu.daniel.vince.humanmobility.view.fragments.OverviewFragment;
import hu.daniel.vince.humanmobility.view.fragments.PlacesFragment;
import hu.daniel.vince.humanmobility.view.fragments.SettingsFragment;

public class MainActivity extends BaseCompactActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // region Members

    private static final String EXTRA_NAV_SELECTED = "extra_nav_selected";

    private TextView titleTextView;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    private String userName = " ";
    private int navSelected = R.id.nav_overview;

    // endregion

    // region Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!permissionsGranted()) {
            Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show();
            ConnectionHandler.getInstance(this).getAccountManager().logout(getApplicationContext());
            return;
        }
        checkPowerManagement();
        ApplicationUser user = DatabaseHandler.getInstance(this).getUser();

        if(user == null) {
            nextLoginActivity();
            finish();
        }

        userName = user.getUserName();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_24dp);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        titleTextView = (TextView) findViewById(R.id.toolbarTitle);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ViewGroup headerView = (ViewGroup) navigationView.getHeaderView(0);
        TextView userTextView = (TextView) headerView.findViewById(R.id.nav_user_tv);
        userTextView.setText(userName);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        SettingsHandler.getInstance(getApplicationContext());

        startMeasurement();

        if (savedInstanceState != null)
            navSelected = savedInstanceState.getInt(EXTRA_NAV_SELECTED, R.id.nav_overview);

        navigateTo(navSelected);
        PlayServiceValidator.checkGooglePlayServices(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else if(navSelected != R.id.nav_overview) {
            navigateTo(R.id.nav_overview);

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_NAV_SELECTED, navSelected);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            titleTextView.setText(R.string.navigation_settings);
            loadFragment(SettingsFragment.newInstance(MainActivity.this), id);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_overview) {
            titleTextView.setText(R.string.navigation_overview);
            loadFragment(OverviewFragment.newInstance(), id);

        } else if (id == R.id.nav_places) {
            titleTextView.setText(R.string.navigation_places);
            loadFragment(PlacesFragment.newInstance(), id);

        } else if (id == R.id.nav_licenses) {
            DialogBuilder.createWebViewDialog(
                    MainActivity.this,
                    getString(R.string.navigation_licenses),
                    "open_source_licenses",
                    true).show();


        } else if(id == R.id.nav_terms) {
            DialogBuilder.createWebViewDialog(
                    MainActivity.this,
                    getString(R.string.navigation_terms),
                    ConnectionHandler.getTermsUrl(),
                    false).show();

        } else if(id == R.id.nav_logout) {
            ConnectionHandler.getInstance(getApplicationContext())
                    .getAccountManager().logout(getApplicationContext());
            SchedulingHandler.getInstance(this).stopUploader();
            SchedulingHandler.getInstance(this).stopRecordSaver();
            nextLoginActivity();

        } else if (id == R.id.nav_send) {
            titleTextView.setText(R.string.bug_report_title);
            loadFragment(BugReportFragment.newInstance(), id);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // endregion

    // region Helpers

    private void navigateTo(int id) {
        navigationView.setCheckedItem(id);
        onNavigationItemSelected(navigationView.getMenu().findItem(id));
    }

    private void loadFragment(Fragment fragment, int id) {
        navSelected = id;
        invalidateOptionsMenu();
        getSupportFragmentManager()
                .beginTransaction().replace(R.id.main_container, fragment).commit();
    }

    private void nextLoginActivity() {
        final Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startMeasurement() {
        Intent intent = new Intent(getString(R.string.accelerometer_broadcast_name));
        sendBroadcast(intent);

        SchedulingHandler scheduler = SchedulingHandler.getInstance(this);

        if(!scheduler.isUploaderRunning())
            scheduler.restartUploader();

        scheduler.restartRecordSaver();
    }

    @SuppressLint("BatteryLife")
    private void checkPowerManagement() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }

            try {
                if ("huawei".equalsIgnoreCase(android.os.Build.MANUFACTURER) &&
                        !SettingsHandler.getInstance(this)
                                .getBoolean(getString(R.string.settings_huawei_protected))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.battery_opt_title))
                            .setMessage(getString(R.string.battery_opt_message))
                            .setPositiveButton(getString(R.string.battery_opt_button),
                                    (dialogInterface, i) -> {
                                        Intent intent = new Intent();
                                        intent.setComponent(
                                                new ComponentName(
                                                        getString(R.string.battery_opt_component),
                                                        getString(R.string.battery_opt_protectActivity)));
                                        startActivity(intent);
                                    }).create().show();
                }
            } catch (Exception e) { /* Ignored. */ }
        }
        SettingsHandler.getInstance(this).save(getString(R.string.settings_huawei_protected), true);
    }

    // endregion
}
