package villo.com.ar.powersupplynotifier.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PowerSupplyBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerSupplyAlarmReceiver alarm = new PowerSupplyAlarmReceiver();

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            alarm.setAlarm(context);
        }
    }
}
