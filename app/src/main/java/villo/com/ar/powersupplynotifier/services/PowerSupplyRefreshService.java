package villo.com.ar.powersupplynotifier.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;

import villo.com.ar.powersupplynotifier.R;
import villo.com.ar.powersupplynotifier.activities.MainActivity;
import villo.com.ar.powersupplynotifier.helpers.ConnectionHelper;
import villo.com.ar.powersupplynotifier.helpers.UpsDataHelper;
import villo.com.ar.powersupplynotifier.model.UpsCallback;
import villo.com.ar.powersupplynotifier.model.UpsResponse;
import villo.com.ar.powersupplynotifier.model.UpsValues;
import villo.com.ar.powersupplynotifier.receivers.PowerSupplyAlarmReceiver;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PowerSupplyRefreshService extends IntentService {

    // An ID used to post the notification.
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_FETCH_NEW_VALUES = "villo.com.ar.powersupplynotifier.action.ACTION_FETCH_NEW_VALUES";

    public PowerSupplyRefreshService() {
        super("PowerSupplyRefreshService");
    }

    /**
     * Starts this service to perform action FetchNewValues with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFetchNewValues(Context context) {
        Intent intent = new Intent(context, PowerSupplyRefreshService.class);
        intent.setAction(ACTION_FETCH_NEW_VALUES);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_NEW_VALUES.equals(action)) {
                handleActionFetchNewValues();
            }
            PowerSupplyAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchNewValues() {
        ConnectionHelper.isConnectedAndReachable(this, new ServiceCallback<Boolean>() {
            @Override
            public void execute(Context context, Boolean response) {
                if (response) {
                    final UpsValues oldValues = UpsDataHelper.retrieveValuesFromSharedPref(context);

                    UpsDataHelper.fetchNewValues(context, new UpsCallback() {
                        @Override
                        public void onFailure(UpsResponse response, IOException e) {
                            // ignore this
                            if (oldValues == null || !oldValues.getStatus().equalsIgnoreCase(response.getValues().getStatus())) {
                                // I need to notice the user this.
                                sendNotification(response.getValues());
                            }
                        }

                        @Override
                        public void onResponse(UpsResponse response) throws IOException {
                            LocalBroadcastManager.getInstance(PowerSupplyRefreshService.this).sendBroadcast(new Intent(MainActivity.ACTION_FETCH_NEW_VALUES));

                            if (oldValues == null || !oldValues.getStatus().equalsIgnoreCase(response.getValues().getStatus())) {
                                // I need to notice the user this.
                                sendNotification(response.getValues());
                            }
                        }
                    });
                }
            }
        });
    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(UpsValues values) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        int smallIcon;
        String title;
        String message;
        if (values.getStatus().contains("ONLINE")) {
            smallIcon = R.mipmap.ic_launcher;
            title = "Volvió la luz!";
            message = "Carga al " + values.getCharge() + " - AC: " + values.getVoltage();
        } else if (values.getStatus().contains("NO CONNECTION")) {
            smallIcon = R.drawable.ic_light_bulb_off;
            title = "No hay luz!";
            message = "Algo no está bien";
        } else { // Battery.
            smallIcon = R.drawable.ic_battery;
            title = "En bateria!";
            message = "Quedan " + values.getRemainingTime() + " de bateria.";
        }

        NotificationCompat.Builder mBuilder =
            new NotificationCompat.Builder(this)
                    .setSmallIcon(smallIcon)
                    .setLights(Color.YELLOW, 700, 1300)
                    .setContentTitle(title)
                    .setAutoCancel(true)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(message))
                    .setContentText(message);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
