package villo.com.ar.powersupplyapp.services;

import android.content.Context;

/**
 * Created by gvilloldo on 15/08/2014.
 */
public interface ServiceCallback<T> {
    public void execute(Context context, T response);
}
