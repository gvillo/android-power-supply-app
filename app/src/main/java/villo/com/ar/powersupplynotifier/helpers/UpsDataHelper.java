package villo.com.ar.powersupplynotifier.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import villo.com.ar.powersupplynotifier.Constants;
import villo.com.ar.powersupplynotifier.model.UpsCallback;
import villo.com.ar.powersupplynotifier.model.UpsResponse;
import villo.com.ar.powersupplynotifier.model.UpsValues;

/**
 * Created by villo on 18/1/16.
 */
public class UpsDataHelper {

    public static void fetchNewValues(final Context context, final UpsCallback upsCallback) {
        OkHttpClient client;

        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String url = sharedPreferences.getString("general_router_url", "");

        if (url.toLowerCase().startsWith("https")) {
            client = getUnsafeOkHttpClientForHttps();
        } else {
            client = getRegularOkHttpClient();
        }

        client.setAuthenticator(new Authenticator() {
            @Override
            public Request authenticate(Proxy proxy, Response response) throws IOException {
                String username = sharedPreferences.getString("general_username", "");
                String password = sharedPreferences.getString("general_password", "");

                String credential = Credentials.basic(username, password);
                return response.request().newBuilder().header("Authorization", credential).build();
            }

            @Override
            public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                return null;
            }
        });

        Request request = new Request.Builder().url(url).build();

        Integer readTimeout = Integer.parseInt(sharedPreferences.getString("general_read_timeout", "15"));
        Integer connectTimeout = Integer.parseInt(sharedPreferences.getString("general_connect_timeout", "15"));

        client.setReadTimeout(readTimeout, TimeUnit.SECONDS);
        client.setConnectTimeout(connectTimeout, TimeUnit.SECONDS);
        client.setFollowRedirects(true);
        Callback callback = new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                UpsResponse response = new UpsResponse();

                UpsValues values = new UpsValues();
                values.setStatus("NO CONNECTION");
                values.setLastUpdate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
                response.setValues(values);
                storeValuesSharedPref(context, values);

                upsCallback.onFailure(response, e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String bodyString = response.body().string();

                    Document doc = Jsoup.parse(bodyString);

                    final String upsName = doc.select(".even td").get(Constants.UPS_NAME_INDEX).text().trim();
                    final String status = doc.select(".even td").get(Constants.STATUS_INDEX).text().trim();
                    final String charge = doc.select(".even td").get(Constants.CHARGE_INDEX).text().trim();
                    final String voltage = doc.select(".even td").get(Constants.VOLTAGE_INDEX).text().trim();
                    final String usagePercentage = doc.select(".even td").get(Constants.USAGE_PERCENTAGE_INDEX).text().trim();
                    final String temperature = doc.select(".even td").get(Constants.TEMPERATURE_INDEX).text().trim();
                    final String remainingTime = doc.select(".even td").get(Constants.REMAINING_TIME_INDEX).text().trim();

                    UpsResponse upsResponse = new UpsResponse();

                    if (upsName.equalsIgnoreCase("Not Found")) {
                        upsResponse.setInfoMessage("UPS Not Found, disconnected USB Cable or not configured in router.");
                    } else {
                        UpsValues values = new UpsValues();

                        values.setName(upsName);
                        values.setStatus(status);
                        values.setCharge(charge);
                        values.setVoltage(voltage);
                        values.setUsagePercentage(usagePercentage);
                        values.setTemperature(temperature);
                        values.setRemainingTime(remainingTime);
                        values.setLastUpdate(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime()));
                        storeValuesSharedPref(context, values);
                        upsResponse.setValues(values);

                        if (status.contains("ONLINE")) {
                            upsResponse.setInfoMessage("Hay luz, todo ok.");
                        }
                        else {
                            upsResponse.setInfoMessage("En bateria, en breves no habrá luz.");
                        }
                    }
                    upsCallback.onResponse(upsResponse);
                }
            }
        };
        client.newCall(request).enqueue(callback);
    }

    private static void storeValuesSharedPref(Context context, UpsValues values) {
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("upsvalues_name", values.getName());
        editor.putString("upsvalues_status", values.getStatus());
        editor.putString("upsvalues_charge", values.getCharge());
        editor.putString("upsvalues_voltage", values.getVoltage());
        editor.putString("upsvalues_usagePercentage", values.getUsagePercentage());
        editor.putString("upsvalues_temperature", values.getTemperature());
        editor.putString("upsvalues_remainingTime", values.getRemainingTime());
        editor.putString("upsvalues_lastUpdate", values.getLastUpdate());
        editor.apply();
    }

    public static UpsValues retrieveValuesFromSharedPref(Context context) {
        UpsValues response = new UpsValues();
        final SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        response.setLastUpdate(sharedPreferences.getString("upsvalues_lastUpdate", ""));
        if (!response.getLastUpdate().isEmpty()) {
            response.setName(sharedPreferences.getString("upsvalues_name", ""));
            response.setStatus(sharedPreferences.getString("upsvalues_status", ""));
            response.setCharge(sharedPreferences.getString("upsvalues_charge", ""));
            response.setVoltage(sharedPreferences.getString("upsvalues_voltage", ""));
            response.setUsagePercentage(sharedPreferences.getString("upsvalues_usagePercentage", ""));
            response.setTemperature(sharedPreferences.getString("upsvalues_temperature", ""));
            response.setRemainingTime(sharedPreferences.getString("upsvalues_remainingTime", ""));
        } else {
            return null;
        }
        return response;
    }

    private static OkHttpClient getUnsafeOkHttpClientForHttps() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static OkHttpClient getRegularOkHttpClient() {
        return new OkHttpClient();
    }

}
