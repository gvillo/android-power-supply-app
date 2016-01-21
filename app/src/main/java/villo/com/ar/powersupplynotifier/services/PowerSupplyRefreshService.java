package villo.com.ar.powersupplynotifier.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;

import villo.com.ar.powersupplynotifier.R;
import villo.com.ar.powersupplynotifier.activities.MainActivity;
import villo.com.ar.powersupplynotifier.helpers.UpsDataHelper;
import villo.com.ar.powersupplynotifier.model.UpsCallback;
import villo.com.ar.powersupplynotifier.model.UpsResponse;

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
    NotificationCompat.Builder builder;

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
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFetchNewValues() {
        UpsDataHelper.fetchNewValues(this, new UpsCallback() {
            @Override
            public void onFailure(UpsResponse response, IOException e) {
                // ignore this
            }

            @Override
            public void onResponse(UpsResponse response) throws IOException {
                sendNotification(response.getInfoMessage());
            }
        });
    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Algo paso...")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
