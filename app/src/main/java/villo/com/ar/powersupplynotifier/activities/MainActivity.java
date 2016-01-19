package villo.com.ar.powersupplynotifier.activities;

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
import android.widget.TextView;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import villo.com.ar.powersupplynotifier.Constants;
import villo.com.ar.powersupplynotifier.R;
import villo.com.ar.powersupplynotifier.helpers.ConnectivityHelper;
import villo.com.ar.powersupplynotifier.helpers.FetchNewValuesHelper;
import villo.com.ar.powersupplynotifier.model.UpsCallback;
import villo.com.ar.powersupplynotifier.model.UpsResponse;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MainActivity activity;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHandler = new Handler(Looper.getMainLooper());
        activity = this;

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

        try {
            update(fab);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(View view) throws IOException {
        if (!ConnectivityHelper.isOnline(this)) {
            ((TextView)findViewById(R.id.content_title)).setText("No hay conexion a internet, no se puede comprobar el estado.");
        }
        ((TextView)findViewById(R.id.content_title)).setText("Actualizando...");

        FetchNewValuesHelper.fetchNewValues(new UpsCallback() {
            @Override
            public void onFailure(final UpsResponse response, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.content_title)).setText(response.getErrorMessage());
                    }
                });
            }

            @Override
            public void onResponse(final UpsResponse response) throws IOException {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.content_title)).setText(response.getInfoMessage());
                    }
                });
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
            return true;
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

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

