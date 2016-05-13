package com.example.wefit.applog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Kim Jisun on 2016-04-15.
 * StartReceiver, if boot and screen on than LogService start
 */
public class WefitReceiver extends BroadcastReceiver {
    private static Intent logServiceIntent;
    private static Intent wifiServiceIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        // 스마트폰 부트 시, LogService 시작
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            logServiceIntent = new Intent(context, LogService.class);
            context.startService(logServiceIntent);
        }

        // Wifi 연결 여부 변화 체크
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected()) {
                    // Wifi is connected
                    wifiServiceIntent = new Intent(context, WifiConnectedService.class);
                    context.startService(wifiServiceIntent);
                } else {
                    // Wifi is disconnected
                    context.stopService(wifiServiceIntent);
                }
            } else {
                // Wifi is disconnected
                context.stopService(wifiServiceIntent);
            }
        }
    }
}