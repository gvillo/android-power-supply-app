package villo.com.ar.powersupplynotifier.model;

import java.io.IOException;

/**
 * Created by villo on 18/1/16.
 */
public interface UpsCallback {
    void onFailure(UpsResponse response, IOException e);

    void onResponse(UpsResponse response) throws IOException;
}
