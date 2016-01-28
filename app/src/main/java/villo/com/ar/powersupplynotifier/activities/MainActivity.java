package villo.com.ar.powersupplynotifier.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import villo.com.ar.powersupplynotifier.R;
import villo.com.ar.powersupplynotifier.helpers.ConnectionHelper;
import villo.com.ar.powersupplynotifier.helpers.UpsDataHelper;
import villo.com.ar.powersupplynotifier.model.UpsCallback;
import villo.com.ar.powersupplynotifier.model.UpsResponse;
import villo.com.ar.powersupplynotifier.model.UpsValues;
import villo.com.ar.powersupplynotifier.services.ServiceCallback;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Handler mHandler;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHandler = new Handler(Looper.getMainLooper());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    update(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUiWithLocalData();
    }

    private void updateUiWithLocalData() {
        UpsValues savedData = UpsDataHelper.retrieveValuesFromSharedPref(this);
        if (savedData == null) {
            findViewById(R.id.status_container).setVisibility(View.GONE);
            findViewById(R.id.no_data_container).setVisibility(View.VISIBLE);
        } else {
            populateDataInUi(savedData);
            findViewById(R.id.no_data_container).setVisibility(View.GONE);
            findViewById(R.id.status_container).setVisibility(View.VISIBLE);
        }
    }

    private void populateDataInUi(UpsValues values) {
        findViewById(R.id.no_data_container).setVisibility(View.GONE);
        findViewById(R.id.status_container).setVisibility(View.VISIBLE);

        TextView status = ((TextView)findViewById(R.id.status));
        if (values.getStatus().contains("ONLINE")) {
            ((ImageView) findViewById(R.id.status_image)).setImageResource(R.drawable.ic_light_bulb_on_fullsize);
            status.setTextColor(getResources().getColor(R.color.green));
        } else if (values.getStatus().contains("NO CONNECTION")) {
            ((ImageView)findViewById(R.id.status_image)).setImageResource(R.drawable.ic_light_bulb_off_fullsize);
            status.setTextColor(getResources().getColor(R.color.red));
        } else { // Battery.
            ((ImageView)findViewById(R.id.status_image)).setImageResource(R.drawable.ic_battery_fullsize);
            status.setTextColor(getResources().getColor(R.color.orange));
        }
        ((TextView)findViewById(R.id.status)).setText(values.getStatus());

        ((TextView)findViewById(R.id.ups_name_value)).setText(values.getName());
        ((TextView)findViewById(R.id.ups_charge_value)).setText(values.getCharge());
        ((TextView)findViewById(R.id.ups_voltage_value)).setText(values.getVoltage());
        ((TextView)findViewById(R.id.ups_temperature_value)).setText(values.getTemperature());
        ((TextView)findViewById(R.id.ups_lastupdate_value)).setText(values.getLastUpdate());
        ((TextView)findViewById(R.id.ups_remaining_value)).setText(values.getRemainingTime());
        ((TextView)findViewById(R.id.ups_usage_value)).setText(values.getUsagePercentage());
    }

    private void update(View view) throws IOException {
        showSnackbarForIndefinite("Actualizando...");

        ConnectionHelper.isConnectedAndReachable(this, new ServiceCallback<Boolean>() {
            @Override
            public void execute(Context context, Boolean response) {
                if (response) {
                    UpsDataHelper.fetchNewValues(context, new UpsCallback() {
                        @Override
                        public void onFailure(final UpsResponse response, IOException e) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    snackbar.dismiss();
                                    populateDataInUi(response.getValues());
                                }
                            });
                        }

                        @Override
                        public void onResponse(final UpsResponse response) throws IOException {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    snackbar.dismiss();
                                    populateDataInUi(response.getValues());
                                }
                            });
                        }
                    });
                } else {
                    showSnackbarForLong("No hay conexion a internet, no se puede comprobar el estado.");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            // Handle the camera action
        } else if (id == R.id.nav_config) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSnackbarForIndefinite(String message) {
        if (snackbar != null && snackbar.isShown())
            snackbar.dismiss();

        snackbar = Snackbar.make(findViewById(R.id.fab), message, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
    }

    private void showSnackbarForLong(String message) {
        if (snackbar != null && snackbar.isShown())
            snackbar.dismiss();

        snackbar = Snackbar.make(findViewById(R.id.fab), message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}

