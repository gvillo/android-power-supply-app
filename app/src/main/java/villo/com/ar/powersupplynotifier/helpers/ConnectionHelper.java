package villo.com.ar.powersupplynotifier.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import villo.com.ar.powersupplynotifier.services.ServiceCallback;

/**
 * Created by gvilloldo on 09/10/2014.
 */
public class ConnectionHelper {

    public static Boolean checkConnection(Context context) {

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null)
            return false;
        if (!info.isConnected())
            return false;
        return info.isAvailable();
    }

    /**
     * Check if there is any connectivity
     * @param context
     * @return
     */
    public static boolean isConnected(Context context){
        NetworkInfo info = ConnectionHelper.getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    /**
     * Check if there is any connectivity and if the host is reachable
     * @param context
     * @return
     */
    public static void isConnectedAndReachable(Context context, final ServiceCallback<Boolean> callback){
        isConnectedAndReachable(context, "http://google.com", callback);
    }

    /**
     * Check if there is any connectivity and if the host is reachable
     * @param context
     * @return
     */
    public static void isConnectedAndReachable(final Context context, final String host, final ServiceCallback<Boolean> callback){
        final NetworkInfo info = ConnectionHelper.getNetworkInfo(context);

        if (info != null && info.isConnected()) {
            final SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(context);

            Integer readTimeout = Integer.parseInt(sharedPreferences.getString("general_read_timeout", "15"));
            Integer connectTimeout = Integer.parseInt(sharedPreferences.getString("general_connect_timeout", "15"));

            OkHttpClient client = new OkHttpClient();
            client.setReadTimeout(readTimeout, TimeUnit.SECONDS);
            client.setConnectTimeout(connectTimeout, TimeUnit.SECONDS);

            Request request = new Request.Builder().url(host).build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    callback.execute(context, false);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (response.isSuccessful()) {
                        callback.execute(context, true);
                    } else {
                        callback.execute(context, false);
                    }
                }
            });
        } else {
            callback.execute(context, false);
        }
    }

    /**
     * Check if there is any connectivity to a Wifi network
     * @param context
     * @return
     */
    public static boolean isConnectedWifi(Context context){
        NetworkInfo info = ConnectionHelper.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    /**
     * Check if there is any connectivity to a mobile network
     * @param context
     * @return
     */
    public static boolean isConnectedMobile(Context context) {
        NetworkInfo info = ConnectionHelper.getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Get the network info
     * @param context
     * @return
     */
    private static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm == null ? null : cm.getActiveNetworkInfo();
    }
}
